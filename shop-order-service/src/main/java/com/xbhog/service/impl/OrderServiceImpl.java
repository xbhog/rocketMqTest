package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.xbhog.api.IGoodsService;
import com.xbhog.api.IUserService;
import com.xbhog.api.IorderService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeGoods;
import com.xbhog.shop.pojo.TradeOrder;
import com.xbhog.shop.pojo.TradeUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/29
 */
@Slf4j
@Component
@Service(interfaceClass = IorderService.class)
public class OrderServiceImpl implements IorderService {

    @Reference
    private IGoodsService goodsService;
    @Reference
    private IUserService userService;

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
        TradeGoods goods = goodsService.findOne(order.getGoodsId());
        if(goods == null){
            CastException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }
        //3.校验下单用户是否存在
        TradeUser tradeUser = userService.findOne(order.getUserId());
        if(tradeUser == null){
            CastException.cast(ShopCode.SHOP_USER_IS_NULL);
        }
        //4.校验订单金额是否合法
        if(order.getOrderAmount().compareTo(goods.getGoodsPrice().multiply(BigDecimal.valueOf(order.getGoodsNumber()))) != 0){
            CastException.cast(ShopCode.SHOP_GOODS_PRICE_INVALID);
        }
        //5.校验订单商品数量是否合法(是否超过库存总量)
        if(order.getGoodsNumber() <= goods.getGoodsNumber()){
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        log.info("校验订单通过");
    }
}
