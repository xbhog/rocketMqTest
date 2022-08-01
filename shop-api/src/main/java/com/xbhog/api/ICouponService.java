package com.xbhog.api;

import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeCoupon;

/**
 * @author xbhog
 * @describe:优惠卷接口
 * @date 2022/7/31
 */
public interface ICouponService {
    /**
     * 查找优惠卷
     * @param couponId
     * @return
     */
    TradeCoupon findOne(Long couponId);

    /**
     * 使用优惠卷
     * @param coupon
     * @return
     */
    Result updateCouponStatus(TradeCoupon coupon);
}
