package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.xbhog.api.ICouponService;
import com.xbhog.api.IGoodsService;
import com.xbhog.api.IUserService;
import com.xbhog.api.IorderService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeOrderMapper;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.*;
import com.xbhog.utils.IDWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Date;

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

    @Reference
    private IDWorker idWorker;

    @Reference
    private ICouponService couponService;

    @Autowired
    private TradeOrderMapper orderMapper;

    @Override
    public Result confirmOrder(TradeOrder order) {
        //1.校验订单
        checkOrder(order);
        //2.生成预订单
        Long orderId = savePreOrder(order);
        //使用try保证3-7过程的原子性
        try {
            //3.扣减库存
            reduceGoodNum(order);
            //4.扣减优惠券
            updateCoupStatus(order);
            //5.扣减余额
            updateMoneyPaid(order);
            //6.确认订单
            updateOrderStatus(order);
            //7.返回成功状态

        } catch (Exception e) {
            //1.确认订单失败,发送消息

            //2.返回失败状态
        }
        return null;
    }

    /**
     * 确认订单：修改订单状态(不可见变为可见)
     * @param order
     */
    private void updateOrderStatus(TradeOrder order) {
        order.setOrderStatus(ShopCode.SHOP_ORDER_CONFIRM.getCode());
        order.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());
        order.setConfirmTime(new Date());
        int r = orderMapper.updateByPrimaryKey(order);
        if(r <= 0){
            //订单确认失败
            CastException.cast(ShopCode.SHOP_ORDER_CONFIRM_FAIL);
        }
        log.info("订单{}状态修改成功",order.getOrderId());
    }

    /**
     * 扣减余额
     * @param order
     */
    private void updateMoneyPaid(TradeOrder order) {
        if(order.getMoneyPaid()!=null && order.getMoneyPaid().compareTo(BigDecimal.ZERO)==1){
            //用户余额日志
            TradeUserMoneyLog userMoneyLog = new TradeUserMoneyLog();
            BeanUtils.copyProperties(order,userMoneyLog);
            //付款状态
            userMoneyLog.setMoneyLogType(ShopCode.SHOP_USER_MONEY_PAID.getCode());
            //扣除余额
            Result result = userService.changeUserMoney(userMoneyLog);
            if (result.getSuccess().equals(ShopCode.SHOP_FAIL.getSuccess())) {
                CastException.cast(ShopCode.SHOP_USER_MONEY_REDUCE_FAIL);
            }
            log.info("订单:["+order.getOrderId()+"扣减余额["+order.getMoneyPaid()+"元]成功]");
        }
    }

    /**
     * 扣减优惠卷
     * @param order
     */
    private void updateCoupStatus(TradeOrder order) {
        if(order.getCouponId()!=null){
            TradeCoupon coupon = couponService.findOne(order.getCouponId());
            coupon.setOrderId(order.getOrderId());
            coupon.setIsUsed(ShopCode.SHOP_COUPON_ISUSED.getCode());
            coupon.setUsedTime(new Date());

            //更新优惠券状态
            Result result =  couponService.updateCouponStatus(coupon);
            if(result.getSuccess().equals(ShopCode.SHOP_FAIL.getSuccess())){
                CastException.cast(ShopCode.SHOP_COUPON_USE_FAIL);
            }
            log.info("订单:{},使用优惠券",order.getOrderId());
        }

    }

    /**
     * 扣减库存
     * @param order
     */
    private void reduceGoodNum(TradeOrder order) {
        //商品数量日志
        TradeGoodsNumberLog numberLog = new TradeGoodsNumberLog();
        BeanUtils.copyProperties(order,numberLog);
        Result result = goodsService.reduceGoodsNum(numberLog);
        if(result.getSuccess().equals(ShopCode.SHOP_FAIL.getSuccess())){
            CastException.cast(ShopCode.SHOP_USER_MONEY_REDUCE_FAIL);
        }
        log.info("订单：{}扣减库存成功",order.getOrderId());
    }

    /**
     * 生成预订单
     * @param order
     * @return
     */
    private Long savePreOrder(TradeOrder order) {
        //设置订单状态不可见(订单未确认)
        order.setOrderStatus(ShopCode.SHOP_ORDER_NO_CONFIRM.getCode());
        //设置订单Id
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        //核算运费是否正确
        BigDecimal shippingFee = calculateShippingFee(order.getOrderAmount());
        if(order.getShippingFee().compareTo(shippingFee) != 0){
            //订单运费不正确
            CastException.cast(ShopCode.SHOP_ORDER_SHIPPINGFEE_INVALID);
        }
        //核算订单总价是否正确
        BigDecimal orderAmount = order.getGoodsPrice().multiply(BigDecimal.valueOf(order.getGoodsNumber()));
        orderAmount.add(shippingFee);
        if(order.getOrderAmount().compareTo(orderAmount) != 0){
            //订单总金额与实际不匹配，报订单无效
            CastException.cast(ShopCode.SHOP_ORDER_INVALID);
        }
        //判断优惠卷是否合法
        Long couponId = order.getCouponId();
        if(couponId != null){
            TradeCoupon coupon = couponService.findOne(couponId);
            //判断优惠卷存不存在
            if(coupon == null){
                CastException.cast(ShopCode.SHOP_COUPON_NO_EXIST);
            }
            //是否过期
            if(coupon.getIsUsed().intValue() ==ShopCode.SHOP_COUPON_ISUSED.getCode().intValue()){
                CastException.cast(ShopCode.SHOP_COUPON_ISUSED);
            }
            //设置优惠卷
            order.setCouponPaid(coupon.getCouponPrice());
        }else{
            order.setCouponPaid(BigDecimal.ZERO);
        }
        //判断余额是否合法
        BigDecimal moneyPaid = order.getMoneyPaid();
        if(moneyPaid != null){
            int r = moneyPaid.compareTo(BigDecimal.ZERO);
            //余额小于0
            if(r == -1){
                CastException.cast(ShopCode.SHOP_MONEY_PAID_LESS_ZERO);
            }
            //余额大于0
            if(r == 1){
                TradeUser user = userService.findOne(order.getUserId());
                if(moneyPaid.compareTo(new BigDecimal(user.getUserMoney())) ==1){
                    CastException.cast(ShopCode.SHOP_MONEY_PAID_INVALID);
                }
            }
        }else{
            order.setMoneyPaid(BigDecimal.ZERO);
        }
        //核算订单总价
        BigDecimal payAmount = order.getOrderAmount().subtract(order.getMoneyPaid()).subtract(order.getCouponPaid());
        order.setPayAmount(payAmount);
        //设置订单时间
        order.setAddTime(new Date());
        //保存订单到数据库
        orderMapper.insert(order);
        //返回订单ID
        return orderId;
    }
    //核算订单运费
    private BigDecimal calculateShippingFee(BigDecimal orderAmount) {
        //如果订单金额大于100，则免运费，否则运费为10
        if(orderAmount.compareTo(
                new BigDecimal(100)
        ) ==1){
            return BigDecimal.ZERO;
        }else {
            return new BigDecimal(10);
        }

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
        //4.校验商品单价是否合法
        if(order.getGoodsPrice().compareTo(goods.getGoodsPrice()) != 0){
            CastException.cast(ShopCode.SHOP_GOODS_PRICE_INVALID);
        }
        //5.校验订单商品数量是否合法(是否超过库存总量)
        if(order.getGoodsNumber() <= goods.getGoodsNumber()){
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        log.info("校验订单通过");
    }
}
