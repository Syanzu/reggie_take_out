package com.syanzu.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syanzu.reggie.entity.OrderDetail;
import com.syanzu.reggie.mapper.OrderDetailMapper;
import com.syanzu.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;


@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>
        implements OrderDetailService {
}
