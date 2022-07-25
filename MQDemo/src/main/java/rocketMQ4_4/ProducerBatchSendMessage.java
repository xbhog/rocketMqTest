package rocketMQ4_4;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xbhog
 * @describe:发送批量消息
 * @date 2022/7/21
 */
public class ProducerBatchSendMessage {
    private static final String adder = "81.69.251.88:9876";
    public static void main(String[] args) throws MQClientException, MQBrokerException, RemotingException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer("BatchMsg");
        producer.setNamesrvAddr(adder);
        producer.start();
        System.out.println("创建批量消息");
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("BatchMessage", "BitchSend1".getBytes()));
        messages.add(new Message("BatchMessage", "BitchSend2".getBytes()));
        messages.add(new Message("BatchMessage", "BitchSend3".getBytes()));
        SendResult sendResult = producer.send(messages);
        System.out.println("消息回调结果："+sendResult);
        producer.shutdown();
    }
}
