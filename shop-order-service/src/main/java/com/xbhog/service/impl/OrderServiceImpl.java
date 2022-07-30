package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xbhog.api.IGoodsService;
import com.xbhog.api.IorderService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/29
 */
public class OrderServiceImpl implements IorderService {

    @Reference
    private IGoodsService iGoodsService;

    @Override
    public Result confirmOrder(TradeOrder order) {
        //1.校验订单
        checkOrder(order);
        //2.生成预订单
        //使用try保证3-7过程的原子性
        try {
            //3.扣减库存

            //4.扣减优惠券

            //5.使用余额

            //6.确认订单

            //7.返回成功状态

        } catch (Exception e) {
            //1.确认订单失败,发送消息

            //2.返回失败状态
        }
        return null;
    }

    /**
     * 校验订单
     * @param order
     */
    private void checkOrder(TradeOrder order) {
        //1. 校验订单是否存在
        if(order == null){
            CastException.cast(ShopCode.SHOP_ORDER_INVALID);
        }
        //2.校验订单中的商品是否存在

        //3.校验下单用户是否存在

        //4.校验商品单价是否合法

        //5.校验订单商品数量是否合法
    }
}
