package com.syanzu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syanzu.reggie.common.R;
import com.syanzu.reggie.dto.SetmealDto;
import com.syanzu.reggie.entity.Setmeal;

import com.syanzu.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService service;


    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){

        /*
            同时操作两张表Setmeal和Setmeal_dish，需要在service层自定义方法，并实现
         */

        service.saveWithDish(setmealDto);


        return R.success("新增套餐成功！");
    }


    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        /*
            注：在操作Setmael表同时，还要根据categoryId查询菜品分类名称，要在service层自定义方法
         *//*
        // 1 构建分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        // 2 构建条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Setmeal::getName, name);
        // 排序
        lqw.orderByDesc(Setmeal::getUpdateTime);

        // 3 执行sql查询
        Page<Setmeal> setmealPage = service.page(pageInfo, lqw);*/

        Page<SetmealDto> setmealDtoPage = service.pageWithCategory(page, pageSize, name);

        return R.success(setmealDtoPage);

    }


    /**
     * 单个套餐停售功能
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> sellStop(Long ids){
        // 获取setmeal套餐对象
        Setmeal setmeal = service.getById(ids);
        // 修改套餐对象的status属性值
        setmeal.setStatus(0);
        // 执行sql，修改数据库中status字段的值
        service.updateById(setmeal);

        return R.success("套餐停售成功！");
    }


    /**
     * 单个套餐启售功能
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> sellStart(Long ids){
        // 1 获取setmeal套餐对象
        Setmeal setmeal = service.getById(ids);
        // 2 修改套餐对象的status属性值
        setmeal.setStatus(1);
        // 3 执行sql，修改数据库中的status字段的值
        service.updateById(setmeal);

        return R.success("套餐启售成功！");
    }


    /**
     * 修改套餐的信息回显功能(同时操作多张表，在service层定义新功能)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = service.getSetmealDto(id);

        return R.success(setmealDto);
    }


    /**
     * 需改套餐功能：同时修改setmeal表和setmealDsih表，在service层自定义方法
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        service.updateWithDish(setmealDto);
        return R.success("套餐修改成功！");
    }


    /**
     * 套餐删除功能：同时操作setmeal表和setmealDish表，在service层自定义方法
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除套餐功能，要删除的id为{}", ids);

        service.deleteWithDish(ids);

        return R.success("套餐删除成功！");
    }


    @GetMapping("/list")
    //public R<List<Setmeal>> list(@RequestBody Setmeal setmeal){
    public R<List<Setmeal>> list(Setmeal setmeal){
        // 构建查询条件
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());

        // 构建排序
        lqw.orderByDesc(Setmeal::getUpdateTime);

        // 执行查询
        List<Setmeal> list = service.list(lqw);



        return R.success(list);
    }

}
