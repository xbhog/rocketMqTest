package orderMessage4_3;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import pojo.OrderStep;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author xbhog
 * @describe: 顺序生产者
 * @date 2022/7/19
 */
public class Producer {
    private static final String adder = "81.69.251.88:9876";
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("orderGroup");
        producer.setNamesrvAddr(adder);
        producer.start();


        //订单列表
        List<OrderStep> orderSteps = BudileData.buildOrders();
        for (int i = 0; i < orderSteps.size(); i++) {
            String body = orderSteps.get(i) + "";
            Message msg = new Message("orderTopic", "tagA", "KEY" + i, body.getBytes());
            /**
             * 1. 参数：消息对象
             * 2. 消息队列选择器,使用的是业务表示选择的队列选择器
             * 3.选择队列的业务标识(订单ID)
             */
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                /**
                 *
                 * @param list 消息队列
                 * @param message 消息
                 * @param o，传入的参数(业务表示的参数)
                 * @return
                 */
                @Override
                public MessageQueue select(List<MessageQueue> list, Message message, Object o) {
                    long orderId = (long) o;
                    //取模，目的：相同的消息表示取模是一样的，区分是否在一个队列中
                    long l = orderId % list.size();
                    return list.get((int) l);
                }
            }, orderSteps.get(i).getOrderId());
            System.out.println("发送结果"+sendResult);
        }



    }
}
