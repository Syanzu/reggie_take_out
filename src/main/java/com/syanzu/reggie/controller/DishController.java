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
import org.springframework.web.bind.annotation.*;

import java.util.List;
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


    // 菜品信息回显功能：因为同时操作多张表，所以需要在service层自定义方法
    /*@GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        // 1 执行sql查询dish对象
        Dish dish = service.getById(id);

        // 属性拷贝
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish, dishDto);

        // 2 通过dish对象获取category对象的name
        Long categoryId = dish.getCategoryId();
        Category category = categoryService.getById(categoryId);
        if(category != null){
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
        }

        // 3 获取dishDto中flavor集合的值
        LambdaQueryWrapper<DishFlavor> lwq = new LambdaQueryWrapper<>();
        lwq.eq(id != null, DishFlavor::getDishId, id);
        List<DishFlavor> flavorList = dishFlavorService.list(lwq);

        // 4 将 flavorList的值set进dishDto对象中
        dishDto.setFlavors(flavorList);

        return R.success(dishDto);
    }*/

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

        return R.success("菜品修改成功！");
    }


    /**
     * 菜品停售：单个
     * @param
     * @return
     */
    /*不可行
    @PostMapping("/status/0")
    public R<String> sellStop(Dish dish){
        dish.setStatus(0);
        service.updateById(dish);   // 忘记这步，导致没修该成功
        return R.success("停售成功！");
    }*/
    @PostMapping("/status/0")
    public R<String> sellStop(Long ids){
        Dish dish = service.getById(ids);
        dish.setStatus(0);
        service.updateById(dish);   // 忘记这步，导致没修该成功
        return R.success("停售成功！");
    }

    /**
     * 批量停售
     * @return
     *//*
    @PostMapping("/status/0")
    public R<String> sellStopList(List<Long> listId){

        *//*
            在service中定义新方法
         *//*
        service.sellStopList(listId);


        return R.success("批量停售成功！");
    }*/

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
    /*@GetMapping("/list")
    *//*public R<List> list(Long categoryId){*//*
    //public R<List> list(Dish dish) {       错误！List应该有泛型
    public R<List<Dish>> list(Dish dish) {    // 优化，使用dish来接收参数，而不是dish的属性categoryId
        // 构建条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // a 根据getCategoryId来查询
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // b 筛选status为1
        lqw.eq(Dish::getStatus, 1);
        // c 添加排序条件
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = service.list(lqw);

        return R.success(dishList);
    }*/

    /**
     * 添加套餐中：添加菜品功能
     * @param dish
     * @return dishList菜品集合
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        // 构建条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // a 根据getCategoryId来查询
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // b 筛选status为1
        lqw.eq(Dish::getStatus, 1);
        // c 添加排序条件
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = service.list(lqw);



        List<DishDto> dishDtoList = list.stream().map((item)->{  // item代表List中的每个元素：即Dish对象

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
