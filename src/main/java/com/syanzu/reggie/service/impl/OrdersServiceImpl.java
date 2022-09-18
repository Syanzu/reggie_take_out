package com.syanzu.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syanzu.reggie.common.BaseContext;
import com.syanzu.reggie.common.CustomException;
import com.syanzu.reggie.entity.*;
import com.syanzu.reggie.mapper.OrdersMapper;
import com.syanzu.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;



    /**
     * 用户下单
     * @param orders
     */
    @Transactional // 开启事务
    public void submit(Orders orders) {
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();


        // 查询当前用户购物车中的菜品或套餐信息（从sql中查）
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        // 如果购物车信息为空，则直接抛出自定义异常
        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单。");
        }


        // 查询User表中的：用户数据
        User user = userService.getById(userId);

        // 查询Address表中的：地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null){
            throw new CustomException("用户地址有误，不能下单！");
        }


        long id = IdWorker.getId();  // 使用IdWorker生成ID作为订单号


        AtomicInteger amount = new AtomicInteger(0);  // 原子操作，保证在多线程情况下计算不会出错
        // 遍历购物车数据
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(id);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            // 单份的金额 * 份数
            // getNumber（份数），封装成BigDecimal类型，然后再进行乘法运算、
            // 最后的数据转换成intValue类型的数据
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());  // 累加操作：每次遍历出来的数字相加
            return orderDetail;
        }).collect(Collectors.toList());



        // 向订单表插入数据，一条
        // 将Orders的其他信息补充完整
        orders.setNumber(String.valueOf(id));
        orders.setId(id);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));  //总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(id));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);


        // 向订单明细表插入数据，多条
        orderDetailService.saveBatch(orderDetails);


        // 清空购物车数据
        shoppingCartService.remove(wrapper);
    }
}
