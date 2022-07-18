package producer;

import org.apache.commons.validator.Msg;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;

/**
 * @author xbhog
 * @date 2022/7/9
 * @describe:同步发送代码:这种可靠性同步地发送方式使用的比较广泛，
 * 比如：重要的消息通知，短信通知
 */
public class SynchronizeCode {
    private static final String adder = "81.69.251.88:9876";
    //private static final String adder = "120.48.57.151:9876";
    public static void main(String[] args) throws Exception {
        //demo();
        // 声明并初始化一个producer
        // 需要一个producer group名字作为构造方法的参数，这里为producer1
        DefaultMQProducer producer = new DefaultMQProducer("producer1");
        producer.setVipChannelEnabled(false);
        producer.setSendMsgTimeout(10000);
        // 设置NameServer地址,此处应改为实际NameServer地址，多个地址之间用；分隔
        // NameServer的地址必须有
        // producer.setClientIP("119.23.211.22");
        // producer.setInstanceName("Producer");
        producer.setNamesrvAddr(adder);

        // 调用start()方法启动一个producer实例
        producer.start();

        // 发送1条消息到Topic为TopicTest，tag为TagA，消息内容为“Hello RocketMQ”拼接上i的值
        try {
            // 封装消息
            Message msg = new Message("TopicXbhog",// topic
                    "TagAA",// tag
                    ("终于完成了").getBytes()// body
            );
            // 调用producer的send()方法发送消息
            // 这里调用的是同步的方式，所以会有返回结果
            SendResult sendResult = producer.send(msg);
            // 打印返回结果
            System.out.println(sendResult);
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        }
        //发送完消息之后，调用shutdown()方法关闭producer
        System.out.println("send success");
        //producer.shutdown();
    }

    private static void demo() throws MQClientException, UnsupportedEncodingException, RemotingException, MQBrokerException, InterruptedException {
        //1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
        producer.setSendMsgTimeout(10000);
        producer.setVipChannelEnabled(false);
        //2.指定Nameserver地址
        producer.setNamesrvAddr(adder);
        //3.启动producer
        producer.start();
        //4.创建消息对象，指定主题Topic、Tag和消息体
        for (int i = 0; i < 100; i++) {
            Message msg = new Message("TopicTest" /* Topic */,
                    "TagA" /* Tag */,
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
            );
            // 发送消息到一个Broker
            SendResult sendResult = producer.send(msg);
            // 通过sendResult返回消息是否成功送达
            System.out.printf("%s%n", sendResult);
        }
        //6.关闭生产者producer
        producer.shutdown();
    }
}
