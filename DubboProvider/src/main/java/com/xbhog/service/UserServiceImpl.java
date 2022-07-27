package com.xbhog.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

@Component
@Service(interfaceClass = IUserService.class)
public class UserServiceImpl implements IUserService{
    @Override
    public String sayHello(String name) {
        return "hello:"+name;
    }
}