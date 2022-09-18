package com.syanzu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syanzu.reggie.common.CustomException;
import com.syanzu.reggie.dto.SetmealDto;
import com.syanzu.reggie.entity.Category;
import com.syanzu.reggie.entity.Setmeal;
import com.syanzu.reggie.entity.SetmealDish;
import com.syanzu.reggie.mapper.SetmealMapper;
import com.syanzu.reggie.service.CategoryService;
import com.syanzu.reggie.service.SetmealDishService;
import com.syanzu.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;


    /**
     * 新增套餐，同时操作Setmeal表、Setmeal_dish表
     * @param setmealDto
     */
    @Override
    @Transactional // 开启事务
    public void saveWithDish(SetmealDto setmealDto) {


        // 好像并不需要categoryName这个参数值
        /*// 1 操作category表，通过categoryId获取categoryName
        Category category = categoryService.getById(setmealDto.getCategoryId());
        String categoryName = category.getName();
        setmealDto.setCategoryName(categoryName);*/

        // 2 操作Setmeal表
        this.save(setmealDto);


        // 3 操作Setmeal_dish表
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();  // 获取套餐内的dish菜品对象集合
        // 遍历dish对象，设置他们的setmeal的id属性值
        setmealDishList = setmealDishList.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 将处理后的dish对象集合批量保存
        setmealDishService.saveBatch(setmealDishList);

    }



    /**
     * 套餐分页查询，同时操作Setmeal表和Category表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<SetmealDto> pageWithCategory(int page, int pageSize, String name) {

        //  创建 SetmealDto类型的page对象
        Page<SetmealDto> setmealDtoPage = new Page<>();


        // 1 操作Setmeal表
        // a 分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        // b 条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Setmeal::getName, name);
        // c 排序
        lqw.orderByDesc(Setmeal::getUpdateTime);
        // d 执行sql查询
        this.page(pageInfo, lqw);    // 不需要返回结果，结果会自动赋值给pageInfo对象


        // 将pageInfo对象的属性值拷贝到setmealDtoPage中，注意records这个属性值禁止拷贝，否则会出异常
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records"); // records代表Setmeal对象集合



        // 完成records拷贝工作，并操作Category表，完成CategoryName的赋值
        // a 遍历records中每个Setmeal对象，将每个Setmeal对象的属性值拷贝给setmealDto对象
        List<SetmealDto> setmealDtoList= pageInfo.getRecords().stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            // 2 操作Category表
            Category category = categoryService.getById(setmealDto.getCategoryId());
            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList()); // b 返回的集合赋值给setmealDtoList

        // 设置setmealDtoPage中records属性的值
        setmealDtoPage.setRecords(setmealDtoList);

        /*// 创建SetmealDtoPage对象
        Page<SetmealDto> setmealDtoPage = new Page<>();


        // 1 操作setMeal表
        // 创建分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        // 创建条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Setmeal::getName, name);
        // 执行sql查询分页数据
        // Page<Setmeal> setmealPage = this.page(pageInfo, lqw);               错！！！！！！！！！！
        this.page(pageInfo, lqw);


        // 2 使用BeanUtils将setmealPage属性值拷贝到setmealDtoPage中
        // BeanUtils.copyProperties(setmealPage, setmealDtoPage);               错！！！！！！！！！！
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");
        // 切记不要把records属性值给拷贝过去，会出 类转化异常


        // 3 操作Category表，通过Id获取categoryName
        // 从Page对象的records属性中获取SetmealDto对象集合
        List<SetmealDto> setmeaDtolList = setmealDtoPage.getRecords();
        // 使用stream流，将categoryName设置进去每个SetmealDto对象中
        setmeaDtolList = setmeaDtolList.stream().map((item)->{
            Category category = categoryService.getById(item.getCategoryId());
            String categoryName = category.getName();
            item.setCategoryName(categoryName);
            return item;
        }).collect(Collectors.toList());

        // 将setmeaDtolList设置到Page对象中
        setmealDtoPage.setRecords(setmeaDtolList);*/

        return setmealDtoPage;
    }


    /**
     * 修改套餐时候的套餐信息回显功能，同时操作setmeal表和dish表
     * @param id
     * @return
     */
    @Override
    public SetmealDto getSetmealDto(Long id) {
        SetmealDto setmealDto = new SetmealDto();

        // 1 操作setmeal表
        // 获取setmeal对象
        Setmeal setmeal = this.getById(id);
        // 将setmael对象的信息拷贝到setmealDto对象中
        BeanUtils.copyProperties(setmeal, setmealDto);


        // 获取categoryName，并set到SetmealDto对象中
        Category category = categoryService.getById(setmeal.getCategoryId());
        String categoryName = category.getName();
        setmealDto.setCategoryName(categoryName);


        // 2 操作dish表
        // 通过setmealId获取dish对象的集合
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getId() != null, SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);
        // 将dish集合set到setmealDto对象中
        setmealDto.setSetmealDishes(setmealDishList);


        return setmealDto;
    }


    /**
     * 更新套餐的功能，同时修改setmeal表和setmealDish表
     * @param setmealDto
     */
    @Override
    @Transactional // 开启事务管理
    public void updateWithDish(SetmealDto setmealDto) {
        // 1 修改setmeal表
        this.updateById(setmealDto);


        // 2 修改setmealDish表
        // 将数据库中setmealDto绑定的setmealDishList全部删除
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmealDto.getId() != null, SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lqw);

        // 获取setmealDish集合对象
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        setmealDishList = setmealDishList.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());  // 为集合对象中的每个setmealDish元素都设置setmealId的值
            return item;
        }).collect(Collectors.toList());
        // 将新获取的setmealDishList保存
        setmealDishService.saveBatch(setmealDishList);
    }



    /**
     * 删除套餐功能，同时操作setmeal表和setmealDish表
     * @param ids
     */
    @Override
    @Transactional // 开启事务管理
    public void deleteWithDish(List<Long> ids) {
        // select count(*) from setmeal where id in (1, 2, 3) and status = 1;

        // 查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId, ids);
        lqw.eq(Setmeal::getStatus, 1);
        int count = this.count(lqw);
        if(count > 0){
            // 不能删除，有正在启售的套餐
            throw new CustomException("套餐正在售卖中，不能删除！");    // 抛出自定义异常，并给出提示信息
        }


        // 如果可以删除，删除setmeal表中的数据
        this.removeByIds(ids);


        // 删除关系表setmealDish中的数据：不可以使用removeByIds，因为传入的ids参数并不是setmealDish表中的id
        LambdaQueryWrapper<SetmealDish> lwq1 = new LambdaQueryWrapper<>();
        lwq1.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lwq1);

    }


}
