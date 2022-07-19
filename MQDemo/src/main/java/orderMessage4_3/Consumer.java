package orderMessage4_3;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author xbhog
 * @describe:顺序消息的消费
 * @date 2022/7/19
 */
public class Consumer {
    private static final String adder = "81.69.251.88:9876";
    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group1");
        consumer.setNamesrvAddr(adder);
        //消费TopicTest下的所有tag
        consumer.subscribe("orderTopic","*");
        //注册消息监听器:MessageListenerOrderly保证顺序的
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
                for(MessageExt msg : list){
                    System.out.println("【线程名称】:"+Thread.currentThread().getName()+"【队列】："+msg.getQueueId()+"消费消息："+new String(msg.getBody()));
                }
                return  ConsumeOrderlyStatus.SUCCESS;
            }
        });
        consumer.start();
        System.out.println("消费者成功");
    }
}
