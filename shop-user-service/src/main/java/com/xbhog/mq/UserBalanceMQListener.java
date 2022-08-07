package com.xbhog.mq;

import com.alibaba.fastjson.JSON;
import com.xbhog.api.IUserService;
import com.xbhog.constant.ShopCode;
import com.xbhog.shop.entity.MQEntity;
import com.xbhog.shop.pojo.TradeUserMoneyLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author xbhog
 * @describe:
 * @date 2022/8/6
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group.name}",messageModel = MessageModel.BROADCASTING)
public class UserBalanceMQListener implements RocketMQListener<MessageExt> {
    @Autowired
    private IUserService userService;

    @Override
    public void onMessage(MessageExt message) {
        try {
            String body = new String(message.getBody(), "UTF-8");
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            log.info("接收到消息");
            //余额不能为空或者0
            if(mqEntity.getUserMoney()!=null && mqEntity.getUserMoney().compareTo(BigDecimal.ZERO)>0){
                //2.调用业务层,进行余额修改
                TradeUserMoneyLog userMoneyLog = new TradeUserMoneyLog();
                userMoneyLog.setUseMoney(mqEntity.getUserMoney());
                userMoneyLog.setMoneyLogType(ShopCode.SHOP_USER_MONEY_REFUND.getCode());
                userMoneyLog.setUserId(mqEntity.getUserId());
                userMoneyLog.setOrderId(mqEntity.getOrderId());
                userService.changeUserMoney(userMoneyLog);
                log.info("余额回退成功");
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("余额回退失败");
        }

    }
}
