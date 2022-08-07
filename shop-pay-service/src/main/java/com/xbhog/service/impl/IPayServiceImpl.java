package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xbhog.api.IPayService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeMqProducerTempMapper;
import com.xbhog.mapper.TradePayMapper;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeMqProducerTemp;
import com.xbhog.shop.pojo.TradePay;
import com.xbhog.shop.pojo.TradePayExample;
import com.xbhog.utils.IDWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @author xbhog
 * @describe:
 * @date 2022/8/7
 */
@Slf4j
@Component
@Service(interfaceClass = IPayService.class)
public class IPayServiceImpl implements IPayService {
    @Autowired
    private TradePayMapper tradePayMapper;
    @Autowired
    private TradeMqProducerTempMapper mqProducerTempMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private IDWorker idWorker;

    @Value("${rocketmq.producer.group}")
    private String groupName;

    @Value("${mq.topic}")
    private String topic;

    @Value("${mq.pay.tag}")
    private String tag;

    @Override
    public Result createPayment(TradePay tradePay) {
        try {
            TradePayExample tradePayExample = new TradePayExample();
            TradePayExample.Criteria criteria = tradePayExample.createCriteria();
            criteria.andOrderIdEqualTo(tradePay.getOrderId());
            criteria.andIsPaidEqualTo(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
            int count = tradePayMapper.countByExample(tradePayExample);
            //订单已支付
            if(count > 0){
                CastException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY);
            }
            long payId = idWorker.nextId();
            tradePay.setPayId(payId);
            tradePay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());
            tradePayMapper.insert(tradePay);
            log.info("创建支付订单成功:" + payId);
        }catch (Exception e){
            return new Result(ShopCode.SHOP_FAIL.getSuccess(), ShopCode.SHOP_FAIL.getMessage());
        }
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(), ShopCode.SHOP_SUCCESS.getMessage());
    }

    /**
     * 支付回调
     * @param tradePay
     * @return
     */
    @Override
    public Result callbackPayMent(TradePay tradePay) {
        log.info("======>支付回调开始");
        //判断用户支付状态
        if(tradePay.getPayId().equals(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode())){
            TradePay pay = tradePayMapper.selectByPrimaryKey(tradePay.getPayId());
            if(pay == null){
                CastException.cast(ShopCode.SHOP_PAYMENT_NOT_FOUND);
            }
            tradePay.setPayId(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode().longValue());
            //更新本地支付状态
            int i = tradePayMapper.updateByPrimaryKeySelective(tradePay);
            if(i == 1){
                TradeMqProducerTemp mqProducerTemp = new TradeMqProducerTemp();
                mqProducerTemp.setId(String.valueOf(idWorker.nextId()));
                mqProducerTemp.setGroupName("payProducerGroup");
                mqProducerTemp.setMsgKey(String.valueOf(tradePay.getPayId()));
                mqProducerTemp.setMsgTag(topic);
                mqProducerTemp.setMsgBody(JSON.toJSONString(tradePay));
                mqProducerTemp.setCreateTime(new Date());
                mqProducerTempMapper.insert(mqProducerTemp);
                log.info("将支付成功消息持久化到数据库");
                //在线程池中进行处理
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        //发送MQ消息给订单服务
                        SendResult result = null;
                        try {
                            result = sendMessage(topic,tag,String.valueOf(tradePay.getPayId()), JSON.toJSONString(tradePay));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(SendStatus.SEND_OK.equals(result.getSendStatus())){
                            log.info("消息发送成功");
                            //6. 等待发送结果,如果MQ接受到消息,删除发送成功的消息
                            mqProducerTempMapper.deleteByPrimaryKey(mqProducerTemp.getId());
                            log.info("持久化到数据库的消息删除");
                        }
                    }
                });
            }
            return new Result(ShopCode.SHOP_SUCCESS.getSuccess(),ShopCode.SHOP_SUCCESS.getMessage());
        }else{
            CastException.cast(ShopCode.SHOP_PAYMENT_PAY_ERROR);
            return new Result(ShopCode.SHOP_FAIL.getSuccess(),ShopCode.SHOP_FAIL.getMessage());
        }
    }

    /**
     * 发送MQ:支付成功消息
     * @param topic
     * @param tag
     * @param key
     * @param body
     * @return
     */
    private SendResult sendMessage(String topic, String tag, String key, String body) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        if(StringUtils.isEmpty(topic)){
            CastException.cast(ShopCode.SHOP_MQ_TOPIC_IS_EMPTY);
        }
        if(StringUtils.isEmpty(body)){
            CastException.cast(ShopCode.SHOP_MQ_MESSAGE_BODY_IS_EMPTY);
        }
        Message message = new Message(topic,tag,key,body.getBytes());
        SendResult sendResult = rocketMQTemplate.getProducer().send(message);
        return sendResult;
    }
}
