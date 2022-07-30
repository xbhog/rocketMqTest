package com.xbhog.api;

import com.xbhog.shop.pojo.TradeGoods;
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
}
