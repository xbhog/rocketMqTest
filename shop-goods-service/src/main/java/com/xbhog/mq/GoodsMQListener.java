package com.xbhog.mq;

import com.alibaba.fastjson.JSON;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeGoodsMapper;
import com.xbhog.mapper.TradeMqConsumerLogMapper;
import com.xbhog.shop.entity.MQEntity;
import com.xbhog.shop.pojo.TradeGoods;
import com.xbhog.shop.pojo.TradeMqConsumerLog;
import com.xbhog.shop.pojo.TradeMqConsumerLogExample;
import com.xbhog.shop.pojo.TradeMqConsumerLogKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @author xbhog
 * @describe:
 * @date 2022/8/6
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group.name}",messageModel = MessageModel.BROADCASTING)
public class GoodsMQListener implements RocketMQListener<MessageExt> {
    @Value("${rocketmq.producer.group}")
    private String groupName;

    @Autowired
    private TradeMqConsumerLogMapper mqConsumerLogMapper;
    @Autowired
    private TradeGoodsMapper goodsMapper;

    @Override
    public void onMessage(MessageExt message) {
        //解析消息内容
        String msgId = null;
        String tags =  null;
        String keys =  null;
        String body = null;
        try {
            msgId = message.getMsgId();
            tags = message.getTags();
            keys = message.getKeys();
            body = new String(message.getBody(), "UTF-8");
            log.info("接受消息成功");
            //2. 查询消息消费记录
            TradeMqConsumerLogKey primaryKey  = new TradeMqConsumerLogKey();
            primaryKey.setMsgKey(keys);
            primaryKey.setGroupName(groupName);
            primaryKey.setMsgTag(tags);
            TradeMqConsumerLog mqConsumerLog = mqConsumerLogMapper.selectByPrimaryKey(primaryKey);
            if(mqConsumerLog != null){
                Integer status = mqConsumerLog.getConsumerStatus();
                //消息处理过了
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode().intValue() == status){
                    log.info("消息:"+msgId+",已经处理过");
                    return;
                }else if(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode().intValue() == status){
                    log.info("消息:"+msgId+",正在处理");
                    return;
                }else if(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode().intValue() == status) {
                    //处理失败
                    Integer times = mqConsumerLog.getConsumerTimes();
                    if(times > 3){
                        log.info("消息：{}，消息处理超过三次，不能再进行处理了");
                        return;
                    }
                    mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                    //使用数据库乐观锁更新
                    TradeMqConsumerLogExample example = new TradeMqConsumerLogExample();
                    TradeMqConsumerLogExample.Criteria criteria = example.createCriteria();
                    criteria.andMsgTagEqualTo(mqConsumerLog.getMsgTag());
                    criteria.andMsgKeyEqualTo(mqConsumerLog.getMsgKey());
                    criteria.andGroupNameEqualTo(groupName);
                    criteria.andConsumerTimesEqualTo(mqConsumerLog.getConsumerTimes());
                    int r = mqConsumerLogMapper.updateByExampleSelective(mqConsumerLog, example);
                    if(r<=0){
                        //未修改成功,其他线程并发修改
                        log.info("并发修改,稍后处理");
                    }
                }
            }else{
                //4. 判断如果没有消费过...
                mqConsumerLog = new TradeMqConsumerLog();
                mqConsumerLog.setMsgTag(tags);
                mqConsumerLog.setMsgKey(keys);
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                mqConsumerLog.setMsgBody(body);
                mqConsumerLog.setMsgId(msgId);
                mqConsumerLog.setGroupName(groupName);
                mqConsumerLog.setConsumerTimes(0);

                //将消息处理信息添加到数据库
                mqConsumerLogMapper.insert(mqConsumerLog);
            }
            //获得订单信息
            MQEntity mq = JSON.parseObject(body, MQEntity.class);
            Long goodsId = mq.getGoodsId();
            TradeGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            log.info("库存数量{}，购买数量{}",goods.getGoodsNumber(),mq.getGoodsNum());
            if(goods.getGoodsNumber() == null || mq.getGoodsNum() == null){
                CastException.cast(ShopCode.SHOP_GOODS_NUM_IS_DIFFERENT);
            }
            goods.setGoodsNumber(goods.getGoodsNumber()+mq.getGoodsNum());
            goodsMapper.updateByPrimaryKey(goods);
            //将消息的处理状态改为成功
            mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode());
            mqConsumerLog.setConsumerTimestamp(new Date());
            mqConsumerLogMapper.updateByPrimaryKey(mqConsumerLog);
            log.info("回退库存成功");
        } catch (Exception e) {
            e.printStackTrace();
            TradeMqConsumerLogKey primaryKey = new TradeMqConsumerLogKey();
            primaryKey.setMsgTag(tags);
            primaryKey.setMsgKey(keys);
            primaryKey.setGroupName(groupName);
            TradeMqConsumerLog mqConsumerLog = mqConsumerLogMapper.selectByPrimaryKey(primaryKey);
            if(mqConsumerLog==null){
                //数据库未有记录
                mqConsumerLog = new TradeMqConsumerLog();
                mqConsumerLog.setMsgTag(tags);
                mqConsumerLog.setMsgKey(keys);
                mqConsumerLog.setGroupName(groupName);
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode());
                mqConsumerLog.setMsgBody(body);
                mqConsumerLog.setMsgId(msgId);
                mqConsumerLog.setConsumerTimes(1);
                mqConsumerLogMapper.insert(mqConsumerLog);
            }else{
                //设置失败次数
                mqConsumerLog.setConsumerTimes(mqConsumerLog.getConsumerTimes()+1);
                mqConsumerLogMapper.updateByPrimaryKeySelective(mqConsumerLog);
            }
        }

    }
}
