package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.xbhog.api.ICouponService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeCouponMapper;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeCoupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/31
 */
@Component
@Service(interfaceClass = ICouponService.class)
public class CouponServiceImpl implements ICouponService {
    @Autowired
    private TradeCouponMapper tradeCouponMapper;
    /**
     * 查询优惠卷
     * @param couponId
     * @return
     */
    @Override
    public TradeCoupon findOne(Long couponId) {
        if(couponId == null){
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeCouponMapper.selectByPrimaryKey(couponId);
    }

    @Override
    public Result updateCouponStatus(TradeCoupon coupon) {
        if(coupon == null || coupon.getCouponId() == null){
            //请求参数有问题
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        tradeCouponMapper.updateByPrimaryKey(coupon);
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(),ShopCode.SHOP_SUCCESS.getMessage());
    }
}
