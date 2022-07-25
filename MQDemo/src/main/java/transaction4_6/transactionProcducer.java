package transaction4_6;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.concurrent.TimeUnit;

/**
 * @author xbhog
 * @describe:事务学习之生产者
 * @date 2022/7/23
 */
public class transactionProcducer {
    private static final String adder = "81.69.251.88:9876";
    public static void main(String[] args) throws MQClientException, MQBrokerException, RemotingException, InterruptedException {
        //以事务的方式创建生产者
        TransactionMQProducer producer = new TransactionMQProducer("transactionGroup");
        producer.setNamesrvAddr(adder);
        producer.setVipChannelEnabled(false);
        producer.setSendMsgTimeout(10000);
        producer.setTransactionListener(new TransactionListener() {
            //执行本地事务
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                System.out.println("开始执行本地事务");
                if (StringUtils.equals("TagA", msg.getTags())) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else if (StringUtils.equals("TagB", msg.getTags())) {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                } else {
                    return LocalTransactionState.UNKNOW;
                }
            }
            //MQ进行消息事务的回查
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                System.out.println("MQ检查消息Tag【"+msg.getTags()+"】的本地事务执行结果");
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        //启动生产者
        producer.start();
        String[] tags = new String[]{"TagA","TagB","TagC"};
        for (int i = 0; i < tags.length; i++) {
            String body = String.valueOf("发送消息标签：" + tags[i]);
            Message message = new Message("transactionTopic",tags[i], body.getBytes());
            SendResult sendResult = producer.sendMessageInTransaction(message,null);
            System.out.println("发送的结果："+sendResult);
            //TimeUnit.SECONDS.sleep(1);
        }
        //关闭生产者
        //producer.shutdown();


    }
}
