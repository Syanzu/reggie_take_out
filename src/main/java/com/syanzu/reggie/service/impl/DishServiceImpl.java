package com.syanzu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syanzu.reggie.common.R;
import com.syanzu.reggie.dto.DishDto;
import com.syanzu.reggie.entity.Category;
import com.syanzu.reggie.entity.Dish;
import com.syanzu.reggie.entity.DishFlavor;
import com.syanzu.reggie.mapper.DishMapper;
import com.syanzu.reggie.service.CategoryService;
import com.syanzu.reggie.service.DishFlavorService;
import com.syanzu.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    // 注入 flavorService
    @Autowired
    private DishFlavorService flavorService;


    @Autowired
    private CategoryService categoryService;


    /**
     * 根据保存dish和对应的dish_flavor
     * @param dishDto
     */
    //@Override  将Override注释改成 事务管理的注释
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {

        // 1 操作存储dish表
        this.save(dishDto);   // 因为DishDto继承了Dish，里面封装了dish的所有参数，所以直接使用DishDto实体类做参数，可以操作sql

        // 2 操作存储dishFlavor表
        // a 操作1执行sql之后，dishDto对象会带有dish的ID
        Long dishId = dishDto.getId();

        // b 遍历dishFlavor列表，将dish_id设置进去，然后将返回的数据又赋值回list对象自己
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;  // 此处需要将每次操作的元素item给return回去
        }).collect(Collectors.toList());

        // c 执行sql操作dishFlavor表
        flavorService.saveBatch(flavors);  // 批量存储saveBatch，存储list对象

    }


    /**
     * 根据id查询dish和对应的flavor
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();

        // 1 查询菜品信息，从dish表查询
        Dish dish = this.getById(id);
        // 将dish对象中属性的值拷贝到dishDto中
        BeanUtils.copyProperties(dish, dishDto);


        /*
              老师那里没有进行这步操作！！！！！！！
         */
        // 2 获取菜品分类信息，从category表查询
        Long categoryId = dish.getCategoryId();
        Category category = categoryService.getById(categoryId);
        String name = category.getName();
        // 将菜品分类的名字set进dishDto中
        dishDto.setCategoryName(name);


        // 3 查询口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        //lqw.eq(id != null, DishFlavor::getDishId, id);
        lqw.eq(id != null, DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavorList = flavorService.list(lqw);
        // 将flavor口味信息保存在DishDto中
        dishDto.setFlavors(flavorList);


        return dishDto;
    }


    /**
     * 更新菜品，操作dish和dish_flavor两张表
     * @param dishDto
     */
    @Transactional // 开启事务管理
    public void updateWhitFlavor(DishDto dishDto) {
        // 1 修改dish表中的信息
        this.updateById(dishDto);
        // service.updateById(dishDto);  错的

        // 2 修改dish_flavor表中的信息
        //  2.1 先清理当前菜品对应的口味数据  ---  dish_flavor表delete操作
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDto.getId());
        // flavorService.removeById(lqw); 错的
        flavorService.remove(lqw);

        //  2.2 添加当前提交过来的口味数据  ---  dish_flavor表insert操作
        List<DishFlavor> flavorList = dishDto.getFlavors();
        flavorList = flavorList.stream().map((item)->{
            item.setDishId(dishDto.getId());  // 设置每个flavor对象的dish_Id值
            return item;
        }).collect(Collectors.toList());
        flavorService.saveBatch(flavorList);


        /*// 获取flavor集合，遍历每个元素
        List<DishFlavor> flavorList = dishDto.getFlavors();
        flavorList.stream().map((item)->{
            item.setDishId(dishDto.getId());  // 修改每个flavor口味元素的值
            return item;
        });*/

    }


    /**
     *  菜品删除，同时操作dish表和dishFlavor表
     * @param ids
     */
    @Override
    @Transactional // 开启事务管理
    public void deleteWithFlavor(Long ids) {
        // 1 操作dish表
        this.removeById(ids);


        // 2 操作dishFlavor表
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ids != null, DishFlavor::getDishId, ids);
        flavorService.remove(lqw);
    }



}
