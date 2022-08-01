package com.xbhog.api;

import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeGoods;
import com.xbhog.shop.pojo.TradeGoodsNumberLog;
import com.xbhog.shop.pojo.TradeOrder;

/**
 * @author xbhog
 * @describe:商品接口
 * @date 2022/7/30
 */
public interface IGoodsService {
    /**
     * 查找商品
     * @param goodsId
     * @return
     */
    TradeGoods findOne(Long goodsId);

    /**
     * 更新库存数量
     * @param numberLog
     * @return
     */
    Result reduceGoodsNum(TradeGoodsNumberLog numberLog);
}
