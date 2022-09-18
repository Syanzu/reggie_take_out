package com.syanzu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syanzu.reggie.common.BaseContext;
import com.syanzu.reggie.common.R;
import com.syanzu.reggie.entity.ShoppingCart;
import com.syanzu.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 *    购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

/*
    *//**
     * 添加购物车
     * @param shoppingCart
     * @return
     *//*
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}", shoppingCart);

        // 设置用户id，指定当前哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);



        // 查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            // 添加到购物车的是dish菜品
            lqw.eq(ShoppingCart::getDishId, dishId);

        }else {
            // 添加到购物车的是setmeal套餐
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(lqw);



        if(shoppingCartServiceOne != null){
            // 如果已经存在，在原来的数量基础山➕1
            Integer number = shoppingCartServiceOne.getNumber();
            shoppingCart.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {
            // 如果不存在，则添加到购物车，数量默认就是1
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCartServiceOne = shoppingCart; // 此时ID已经赋上值了
        }


        return R.success(shoppingCartServiceOne);
    }*/


    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> save(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车信息是：{}", shoppingCart);

        // 获取当前用户ID
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);


        // 判断当前商品在购物车是否存在
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            // 添加进购物车的是dish菜品
            lqw.eq(ShoppingCart::getDishId, dishId);
        }else {
            // 添加进购物车的是setmeal套餐
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(lqw);



        if(shoppingCartServiceOne != null){
            // 如果存在，则在数量上+1
            Integer number = shoppingCartServiceOne.getNumber();
            shoppingCartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {
            // 如果不存在，则新增
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());  // 设置添加购物车的时间
            shoppingCartService.save(shoppingCart);
            shoppingCartServiceOne = shoppingCart;
        }


        return R.success(shoppingCartServiceOne);
    }


    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getShoppingCart(){

        log.info("查看购物车。。。");

        // 获取当前客户的ID
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);
        lqw.orderByAsc(ShoppingCart::getCreateTime);


        // 执行查询操作
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lqw);

        return R.success(shoppingCartList);
    }


    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){

        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);

        shoppingCartService.remove(lqw);

        return R.success("购物车清空成功！");
    }
}
