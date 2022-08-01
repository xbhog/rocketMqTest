package com.xbhog.api;

import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeUser;
import com.xbhog.shop.pojo.TradeUserMoneyLog;

/**
 * @author xbhog
 * @describe:用户接口
 * @date 2022/7/30
 */
public interface IUserService {
    TradeUser findOne(Long userId);

    Result changeUserMoney(TradeUserMoneyLog userMoneyLog);

}
