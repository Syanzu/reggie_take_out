package com.syanzu.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syanzu.reggie.entity.ShoppingCart;
import com.syanzu.reggie.mapper.ShoppingCartMapper;
import com.syanzu.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;


@Service
public class ShoppingCartServiceImpl extends
        ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
