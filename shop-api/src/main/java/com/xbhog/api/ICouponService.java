package com.xbhog.api;

import com.xbhog.shop.pojo.TradeCoupon;

/**
 * @author xbhog
 * @describe:优惠卷接口
 * @date 2022/7/31
 */
public interface ICouponService {

    TradeCoupon findOne(Long couponId);
}
