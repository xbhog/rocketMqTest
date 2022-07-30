package com.xbhog.api;

import com.xbhog.shop.pojo.TradeUser;

/**
 * @author xbhog
 * @describe:用户接口
 * @date 2022/7/30
 */
public interface IUserService {
    TradeUser findOne(Long userId);
}
