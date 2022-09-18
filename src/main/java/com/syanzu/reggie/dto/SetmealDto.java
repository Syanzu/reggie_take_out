package com.syanzu.reggie.dto;



import com.syanzu.reggie.entity.Setmeal;
import com.syanzu.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;


@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
