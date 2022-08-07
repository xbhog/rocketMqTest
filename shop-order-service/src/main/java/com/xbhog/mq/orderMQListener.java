package com.xbhog.mq;

import com.alibaba.fastjson.JSON;
import com.xbhog.constant.ShopCode;
import com.xbhog.mapper.TradeOrderMapper;
import com.xbhog.shop.entity.MQEntity;
import com.xbhog.shop.pojo.TradeOrder;
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
 * @date 2022/8/6
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group.name}",messageModel = MessageModel.BROADCASTING)
public class orderMQListener implements RocketMQListener<MessageExt> {
    @Autowired
    private TradeOrderMapper orderMapper;

    @Override
    public void onMessage(MessageExt message) {
        try {
            String body = new String(message.getBody(), "UTF-8");
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            TradeOrder order = orderMapper.selectByPrimaryKey(mqEntity.getOrderId());
            //3.更新订单状态为取消
            order.setOrderStatus(ShopCode.SHOP_ORDER_CANCEL.getCode());
            orderMapper.updateByPrimaryKey(order);
            log.info("订单状态设置为取消");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.info("订单取消失败");
        }
    }
}
