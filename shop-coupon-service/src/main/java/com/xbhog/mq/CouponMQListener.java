package com.xbhog.mq;

import com.alibaba.fastjson.JSON;
import com.xbhog.constant.ShopCode;
import com.xbhog.mapper.TradeCouponMapper;
import com.xbhog.shop.entity.MQEntity;
import com.xbhog.shop.pojo.TradeCoupon;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @author xbhog
 * @describe:
 * @date 2022/8/4
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group.name}",messageModel = MessageModel.BROADCASTING)
public class CouponMQListener implements RocketMQListener<MessageExt> {
    @Autowired
    private TradeCouponMapper couponMapper;

    @Override
    public void onMessage(MessageExt message) {
        try {
            String msgBody = new String(message.getBody(), "UTF-8");
            MQEntity mqEntity = JSON.parseObject(msgBody, MQEntity.class);
            log.info("接收到信息：{}",mqEntity);
            //查询优惠卷信息
            TradeCoupon coupon = couponMapper.selectByPrimaryKey(mqEntity.getCouponId());
            coupon.setUsedTime(null);
            coupon.setIsUsed(ShopCode.SHOP_COUPON_UNUSED.getCode());
            coupon.setOrderId(mqEntity.getOrderId());
            couponMapper.updateByPrimaryKey(coupon);
            log.info("优惠卷回退成功");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("回退优惠券失败");
        }
    }
}
