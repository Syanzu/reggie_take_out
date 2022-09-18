package com.syanzu.reggie.dto;


import com.syanzu.reggie.entity.Dish;

import com.syanzu.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * Data Transfer Object：数据传输对象，用于表现层和数据层之间的数据传输
 */
@Data
public class DishDto extends Dish {

    // 菜品对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
