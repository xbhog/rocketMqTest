package com.xbhog.mq;

import com.alibaba.fastjson.JSON;
import com.xbhog.constant.ShopCode;
import com.xbhog.mapper.TradeOrderMapper;
import com.xbhog.shop.pojo.TradeOrder;
import com.xbhog.shop.pojo.TradePay;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @author xbhog
 * @describe:
 * @date 2022/8/7
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.pay.topic}",consumerGroup = "${mq.pay.consumer.group.name}",messageModel = MessageModel.BROADCASTING)
public class PayMentListener implements RocketMQListener<MessageExt> {
    @Autowired
    private TradeOrderMapper orderMapper;
    @Override
    public void onMessage(MessageExt message) {
        log.info("接收到支付成功消息");
        try {
            //1.解析消息内容
            String body = new String(message.getBody(),"UTF-8");
            TradePay tradePay = JSON.parseObject(body,TradePay.class);
            //2.根据订单ID查询订单对象
            TradeOrder tradeOrder = orderMapper.selectByPrimaryKey(tradePay.getOrderId());
            //3.更改订单支付状态为已支付
            tradeOrder.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
            //4.更新订单数据到数据库
            orderMapper.updateByPrimaryKey(tradeOrder);
            log.info("更改订单支付状态为已支付");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
