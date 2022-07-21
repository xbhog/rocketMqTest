package orderMessage4_3;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;

/**
 * 延迟消息：消费消费者
 * @author xbhog
 */
public class ScheduledMessageConsumer {
    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("delayMessgae");
        //订阅主题
        consumer.subscribe("DelayMsg","*");

    }
}
