package com.xbhog.api;

import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeOrder;

/**
 * @author xbhog
 * @describe:订单接口
 * @date 2022/7/29
 */
public interface IOrderService {
    /**
     * 确认订单
     * @param order
     * Trade:贸易，confirm:确认
     * @return
     */
    Result confirmOrder(TradeOrder order);
}
