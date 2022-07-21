package orderMessage4_3;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/21
 */
public class ScheduledMessageProducer {
    private static final String adder = "81.69.251.88:9876";
    public static void main(String[] args) throws MQClientException, MQBrokerException, RemotingException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer("producerDelay");
        producer.setNamesrvAddr(adder);
        producer.setVipChannelEnabled(false);
        producer.setSendMsgTimeout(10000);
        producer.start();

        for (int i = 0; i < 10; i++) {
            String msgBody = "delayMessage" + i;
            Message msg = new Message("DelayMsg", msgBody.getBytes());
            //3是延时等级
            msg.setDelayTimeLevel(3);
            SendResult send = producer.send(msg);
            System.out.println(send);
        }
        //producer.shutdown();

    }
}
