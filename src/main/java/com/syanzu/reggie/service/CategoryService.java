package com.syanzu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syanzu.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    // 自己拓展的方法
    void remove(Long id);
}
