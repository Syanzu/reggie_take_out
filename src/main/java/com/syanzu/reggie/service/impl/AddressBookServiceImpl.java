package com.syanzu.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syanzu.reggie.entity.AddressBook;
import com.syanzu.reggie.mapper.AddressBookMapper;
import com.syanzu.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;


@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
