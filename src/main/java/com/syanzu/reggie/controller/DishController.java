package com.syanzu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syanzu.reggie.common.R;
import com.syanzu.reggie.dto.DishDto;
import com.syanzu.reggie.entity.Category;
import com.syanzu.reggie.entity.Dish;
import com.syanzu.reggie.entity.DishFlavor;
import com.syanzu.reggie.service.CategoryService;
import com.syanzu.reggie.service.DishFlavorService;
import com.syanzu.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService service;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 新增菜品功能
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        // 1 第一步先去CategoryController中增加方法，在dish增加菜品中回显菜品分类数据

        /*
             注：因为前端传来的数据包含两张表，所以要在service层新增自定义方法，
                    并在serviceImpl中实现，且要在方法加入@Trans开启事务，
                        启动类也要加上注解，开启事务管理的支持。
         */

        service.saveWithFlavor(dishDto);

        // 清理所有菜品的缓存数据
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);

        // 精确清理某个分类下面的菜品缓存数据
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_" + categoryId + "_1";
        Set keys = redisTemplate.keys(key);
        redisTemplate.delete(keys);


        return R.success("菜品增加成功！");
    }


    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        // 1 构造 分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);

        // 创建 DishDto类型的Page对象
        Page<DishDto> dtoPage = new Page<>();  // 里面的参数没有值，需要复制

        // 2 构造 条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Dish::getName, name);  // 模糊查询
        // 排序条件
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getCreateTime);

        // 3 执行sql
        service.page(pageInfo, lqw);


        // 对象拷贝，将DishPage中的属性值拷贝到DishDtoPage中
        // 需要忽略records这个属性的值，不做拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records"); // records代表查询出来的Dish对象集合

        /*
            对象拷贝，单独处理records：将Dish对象集合，转为DishDto对象集合，并set到DishDtoPage对象中去
         */
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> dishDtoList = records.stream().map((item)->{  // item代表List中的每个元素：即Dish对象

            DishDto dishDto = new DishDto(); // 创建dishDto对象
            BeanUtils.copyProperties(item, dishDto);  // 将item（Dish）对象中的属性拷贝给dishDto对象中

            Long categoryId = item.getCategoryId();  // 获取categoryId菜品分类的ID
            Category category = categoryService.getById(categoryId);// 通过Id获取category对象

            if(category != null){
                String categoryName = category.getName();// 获取category对象的名称，即菜品分类名称

                dishDto.setCategoryName(categoryName); // 将菜品分类名称set进dishDto对象的"categoryName"属性中
            }
            return dishDto;   // 将处理后的dishDto对象返回给流

        }).collect(Collectors.toList());  // 将全部处理的后的dishDto对象集合返回，并赋值给 DishDtoList


        // 将处理后的records赋值给dtoPage对象的records属性
        dtoPage.setRecords(dishDtoList);

        return R.success(dtoPage);
    }



    /**
     * dish菜品信息回显功能（在service中进行逻辑封装）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = service.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> putWithFlavor(@RequestBody DishDto dishDto){
        service.updateWhitFlavor(dishDto);

        // 清理所有菜品的缓存数据
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);

        // 精确清理某个分类下面的菜品缓存数据
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_" + categoryId + "_1";
        Set keys = redisTemplate.keys(key);
        redisTemplate.delete(keys);


        return R.success("菜品修改成功！");
    }


    /**
     * 菜品停售：单个
     * @param
     * @return
     */
    @PostMapping("/status/0")
    public R<String> sellStop(Long ids){
        Dish dish = service.getById(ids);
        dish.setStatus(0);
        service.updateById(dish);   // 忘记这步，导致没修该成功
        return R.success("停售成功！");
    }



    /**
     * 菜品启售：单个
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> sellStart(Long ids){
        Dish dish = service.getById(ids);
        dish.setStatus(1);
        service.updateById(dish);
        return R.success("启售成功！");
    }




    /**
     * 添加套餐中：添加菜品功能
     * @param dish
     * @return dishList菜品集合
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

        // 动态地构造key：dish分类的key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        // 1 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);


        if(dishDtoList != null ){
            // 2 如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }


        // 构建条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // a 根据getCategoryId来查询
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // b 筛选status为1
        lqw.eq(Dish::getStatus, 1);
        // c 添加排序条件
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = service.list(lqw);



        dishDtoList = list.stream().map((item)->{  // item代表List中的每个元素：即Dish对象

            DishDto dishDto = new DishDto(); // 创建dishDto对象
            BeanUtils.copyProperties(item, dishDto);  // 将item（Dish）对象中的属性拷贝给dishDto对象中

            Long categoryId = item.getCategoryId();  // 获取categoryId菜品分类的ID
            Category category = categoryService.getById(categoryId);// 通过Id获取category对象

            if(category != null){
                String categoryName = category.getName();// 获取category对象的名称，即菜品分类名称

                dishDto.setCategoryName(categoryName); // 将菜品分类名称set进dishDto对象的"categoryName"属性中
            }


            // 查询口味数据flavor
            Long dishId = item.getId(); // 当前菜品的id
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);

            dishDto.setFlavors(dishFlavorList);
            return dishDto;   // 将处理后的dishDto对象返回给流

        }).collect(Collectors.toList());  // 将全部处理的后的dishDto对象集合返回，并赋值给 DishDtoList


        // 3 如果不存在，需要查询数据库，将数据库中的dish数据缓存到redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }



    /**
     * 菜品删除功能，同时操作dish表和dishFlavor表
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        service.deleteWithFlavor(ids);
        return R.success("菜品删除成功！");
    }
}
