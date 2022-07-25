package com.xbhog;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/25
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "springboot-mq",consumerGroup = "${rocketmq.producer.group}")
public class consumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String s) {
        log.info("消费的信息："+s);
    }
}
