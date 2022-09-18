package com.syanzu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syanzu.reggie.common.R;
import com.syanzu.reggie.entity.Category;
import com.syanzu.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService service;


    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}", category.toString());
        service.save(category);
        return R.success("新增分类成功！");
    }


    /**
     * 分页功能
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page = {}, pageSize = {}", page, pageSize);

        // 1 创建 分页查询条件（分页构造器对象）
        Page<Category> pageInfo = new Page(page, pageSize);

        // 2 创建 条件查询条件（条件构造器对象）
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();

        // 3 添加 排序条件
        lqw.orderByDesc(Category::getSort);

        // 分页查询
        service.page(pageInfo, lqw);
        return R.success(pageInfo);
    }


    /**
     * 根据id来删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类，id为：{}", ids);

        //service.removeById(ids);
        service.remove(ids);

       return R.success("分类信息删除成功！");
    }


    /**
     * 根据id修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}", category);
        service.updateById(category);

        return R.success("修改分类信息成功！");
    }


    /**
     * dish新增菜品中，菜品分类下拉框的数据回显功能
     * @param category
     * @return
     */
    @GetMapping("/list")
    // 虽然网页端传回的信息是type，但是选择category类作为参数，封装type进实体类，比用type更好
    // 此处选择Category做参数，网页传来的type会自动装进category实体类中

    //public R<List> list(Category category){       错误
    public R<List<Category>> list(Category category){
        // 1 构建查询条件
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // 先判断type是否为空，再添加查询条件
        lqw.eq(category.getType() != null, Category::getType, category.getType());
        // 排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        // 2 执行sql
        List<Category> categoryList = service.list(lqw);

        return R.success(categoryList);
    }




}
