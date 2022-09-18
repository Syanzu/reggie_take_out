package com.syanzu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syanzu.reggie.dto.DishDto;
import com.syanzu.reggie.entity.Dish;


public interface DishService extends IService<Dish> {

    // 新增菜品，需要同时插入菜品对应的口味数据，需要同时操作两张表：dish、dish_flavor
    void saveWithFlavor(DishDto dishDto);

    // 根据id查询dish菜品信息和flavor口味信息
    DishDto getByIdWithFlavor(Long id);


    // 修改菜品，同时修改dish表和dish_flavor表
    void updateWhitFlavor(DishDto dishDto);

    /*// 菜品批量停售
    void sellStopList(List<Long> list);*/


    // 菜品删除，同时操作dish表和dishFlavor表
    void deleteWithFlavor(Long ids);
}
