package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.xbhog.api.IGoodsService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeGoodsMapper;
import com.xbhog.shop.pojo.TradeGoods;
import com.xbhog.shop.pojo.TradeOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    public TradeGoods findOne(Long goodsId) {
        if(goodsId == null){
            //商品ID不存在，请求参数有问题
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeGoodsMapper.selectByPrimaryKey(goodsId);
    }


}
