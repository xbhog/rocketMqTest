package com.xbhog.api;

import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradePay;
import org.springframework.stereotype.Component;

/**
 * @author xbhog
 * @describe:
 * @date 2022/8/7
 */
public interface IPayService {

    Result createPayment(TradePay tradePay);

    Result callbackPayMent(TradePay tradePay);
}
