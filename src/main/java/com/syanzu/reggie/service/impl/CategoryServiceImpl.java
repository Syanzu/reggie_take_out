package com.syanzu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syanzu.reggie.common.CustomException;
import com.syanzu.reggie.entity.Category;
import com.syanzu.reggie.entity.Dish;
import com.syanzu.reggie.entity.Setmeal;
import com.syanzu.reggie.mapper.CategoryMapper;
import com.syanzu.reggie.service.CategoryService;
import com.syanzu.reggie.service.DishService;
import com.syanzu.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 自定义方法：根据id来删除分类，删除之前进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishQw = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类Id进行查询
        dishQw.eq(Dish::getCategoryId, id);
        int countDish = dishService.count(dishQw);

        // 查询当前分类是否关联了菜品dish，如果已经关联，直接抛出一个业务异常
        if(countDish > 0){
            // 已经关联了菜品，抛出一个业务异常（自定义的Exception）
            throw new CustomException("当前分类下关联了菜品，不能删除！");
        }


        LambdaQueryWrapper<Setmeal> setmealQw = new LambdaQueryWrapper<>();
        // 创建查询条件，根据分类Id进行查询
        setmealQw.eq(Setmeal::getCategoryId, id);
        int countSet = setmealService.count(setmealQw);
        // 查询当前分类是否关联了套餐setmeal，如果已经关联，直接抛出一个业务异常
        if(countSet > 0){
            // 已经关联了套餐，抛出一个业务异常（自定义的Exception）
            throw new CustomException("当下分类已经关联了套餐，不能删除！");
        }


        // 正常删除分类（调用IService的方法进行删除）
        super.removeById(id);

    }
}
