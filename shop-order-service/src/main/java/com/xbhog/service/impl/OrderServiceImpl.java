package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xbhog.api.ICouponService;
import com.xbhog.api.IGoodsService;
import com.xbhog.api.IUserService;
import com.xbhog.api.IOrderService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeOrderMapper;
import com.xbhog.shop.entity.MQEntity;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.*;
import com.xbhog.utils.IDWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/29
 */
@Slf4j
@Component
@Service(interfaceClass = IOrderService.class)
public class OrderServiceImpl implements IOrderService {

    @Reference
    private IGoodsService goodsService;
    @Reference
    private IUserService userService;

    @Autowired
    private IDWorker idWorker;

    @Reference
    private ICouponService couponService;

    @Autowired
    private TradeOrderMapper orderMapper;

    @Value("${mq.order.topic}")
    private String topic;

    @Value("${mq.order.tag.cancel}")
    private String tag;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

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
            CastException.cast(ShopCode.SHOP_MONEY_PAID_INVALID);
            //6.确认订单
            updateOrderStatus(order);
            //7.返回成功状态
            log.info("购买成功");

        } catch (Exception e) {
            log.info("======》进入异常");
            //1.确认订单失败,发送消息
            MQEntity mq = new MQEntity();
            BeanUtils.copyProperties(order,mq);
            mq.setUserMoney(order.getMoneyPaid());
            mq.setGoodsNum(order.getGoodsNumber());
            //2.返回失败状态
            try {
                sendMessages(topic,tag,order.getOrderId().toString(), JSON.toJSONString(mq));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return new Result(ShopCode.SHOP_FAIL.getSuccess(), ShopCode.SHOP_FAIL.getMessage());
        }
        return  new Result(ShopCode.SHOP_SUCCESS.getSuccess(),ShopCode.SHOP_SUCCESS.getMessage());
    }

    /**
     * 发送MQ消息
     * @param topic 消息主题
     * @param tag 消息标签
     * @param key 标识
     * @param mqBody 内容
     */
    private void sendMessages(String topic, String tag, String key, String mqBody) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        log.info("====>进入发送消息{}",topic);
        if(StringUtils.isBlank(topic)){
            CastException.cast(ShopCode.SHOP_MQ_TOPIC_IS_EMPTY);
        }
        if(StringUtils.isBlank(mqBody)){
            CastException.cast(ShopCode.SHOP_MQ_MESSAGE_BODY_IS_EMPTY);
        }
        Message message = new Message(topic, tag, key, mqBody.getBytes());
        rocketMQTemplate.getProducer().send(message);
        log.info("发送成功{}",topic);
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
            //BeanUtils.copyProperties(order,userMoneyLog);
            userMoneyLog.setOrderId(order.getOrderId());
            userMoneyLog.setUserId(order.getUserId());
            userMoneyLog.setUseMoney(order.getMoneyPaid());
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
        if(order.getGoodsNumber() >= goods.getGoodsNumber()){
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        log.info("校验订单通过");
    }
}
