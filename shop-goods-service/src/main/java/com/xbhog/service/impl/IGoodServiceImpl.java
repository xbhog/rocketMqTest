package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.xbhog.api.IGoodsService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeGoodsMapper;
import com.xbhog.mapper.TradeGoodsNumberLogMapper;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeGoods;
import com.xbhog.shop.pojo.TradeGoodsNumberLog;
import com.xbhog.shop.pojo.TradeOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/30
 */
@Component
@Service(interfaceClass = IGoodsService.class)
public class IGoodServiceImpl implements IGoodsService {
    @Autowired
    private TradeGoodsMapper tradeGoodsMapper;
    @Autowired
    private TradeGoodsNumberLogMapper goodsNumberLogMapper;

    @Override
    public TradeGoods findOne(Long goodsId) {
        if(goodsId == null){
            //商品ID不存在，请求参数有问题
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeGoodsMapper.selectByPrimaryKey(goodsId);
    }

    @Override
    public Result reduceGoodsNum(TradeGoodsNumberLog numberLog) {
        if(numberLog == null || numberLog.getGoodsNumber() == null
            || numberLog.getGoodsId() == null || numberLog.getOrderId() == null
            || numberLog.getGoodsNumber().intValue() <= 0){
            //请求参数有无
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        TradeGoods goods = tradeGoodsMapper.selectByPrimaryKey(numberLog.getGoodsId());
        if(goods.getGoodsNumber() < numberLog.getGoodsNumber()){
            //库存不足
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        tradeGoodsMapper.updateByPrimaryKey(goods);
        //记录库存操作日志
        numberLog.setGoodsNumber(-(numberLog.getGoodsNumber()));
        numberLog.setLogTime(new Date());
        goodsNumberLogMapper.insert(numberLog);
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(),ShopCode.SHOP_SUCCESS.getMessage());
    }


}
