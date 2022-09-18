package com.syanzu.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.syanzu.reggie.dto.SetmealDto;
import com.syanzu.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    // 新增套餐，同时操作Setmeal表和Setmeal_dish表
    void saveWithDish(SetmealDto setmealDto);


    // 套餐分页查询，同时操作Setmeal表和Category表
    Page<SetmealDto> pageWithCategory(int page, int pageSize, String name);


    // 修改套餐时候的套餐信息回显功能
    public SetmealDto getSetmealDto(Long id);


    // 修改套餐功能,同时操作setmeal表和setmealDish表
    public void updateWithDish(SetmealDto setmealDto);


    // 删除套餐功能，同时操作setmeal表和setmealDish表
    void deleteWithDish(List<Long> ids);

}
