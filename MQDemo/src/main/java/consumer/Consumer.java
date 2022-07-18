package consumer;

import org.apache.commons.validator.Msg;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.sql.SQLOutput;
import java.util.List;

/**
 * @author xbhog
 * @describe:消费者基本使用
 * @date 2022/7/17
 */
public class Consumer {
    private static final String adder = "81.69.251.88:9876";
    public static void main(String[] args) throws MQClientException {
        //baseOperate();
        //测试MQ默认模式为：负载均衡，三个消费者的消费总和，可消费mq中的10个消息
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group2");
        consumer.setNamesrvAddr(adder);
        consumer.subscribe("TopicTest","TagA");
        //设置为广播模式：每个消费者都会消费全部的消息
        consumer.setMessageModel(MessageModel.BROADCASTING);
        //设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                list.stream().map(msg -> new String(msg.getBody())).forEach(System.out::println);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        //启动消费者consumer
        consumer.start();
    }

    private static void baseOperate() throws MQClientException {
        //创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group");
        //指定Nameserver地址
        consumer.setNamesrvAddr(adder);
        //订阅主题Topic和Tag
        consumer.subscribe("TopicXbhog","TagAA");
        //设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                list.stream().map(Message::getBody).forEach(System.out::println);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        //启动消费者consumer
        consumer.start();
    }
}
