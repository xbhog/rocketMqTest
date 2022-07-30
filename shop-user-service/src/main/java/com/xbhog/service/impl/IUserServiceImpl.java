package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.xbhog.api.IUserService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeUserMapper;
import com.xbhog.shop.pojo.TradeUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/30
 */
@Component
@Service(interfaceClass = IUserService.class)
public class IUserServiceImpl implements IUserService {
    @Autowired
    private TradeUserMapper tradeUserMapper;

    @Override
    public TradeUser findOne(Long userId) {
        if(userId == null){
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeUserMapper.selectByPrimaryKey(userId);
    }
}
