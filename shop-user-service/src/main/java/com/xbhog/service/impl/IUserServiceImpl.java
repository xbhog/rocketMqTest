package com.xbhog.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.xbhog.api.IUserService;
import com.xbhog.constant.ShopCode;
import com.xbhog.exception.CastException;
import com.xbhog.mapper.TradeUserMapper;
import com.xbhog.mapper.TradeUserMoneyLogMapper;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradeUser;
import com.xbhog.shop.pojo.TradeUserMoneyLog;
import com.xbhog.shop.pojo.TradeUserMoneyLogExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/30
 */
@Component
@Service(interfaceClass = IUserService.class)
public class IUserServiceImpl implements IUserService {
    @Autowired
    private TradeUserMapper tradeUserMapper;
    @Autowired
    private TradeUserMoneyLogMapper userMoneyLogMapper;
    @Override
    public TradeUser findOne(Long userId) {
        if(userId == null){
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        return tradeUserMapper.selectByPrimaryKey(userId);
    }

    /**
     * 扣除余额
     * @param userMoneyLog
     * @return
     */
    @Override
    public Result changeUserMoney(TradeUserMoneyLog userMoneyLog) {
        //判断请求参数是否合法
        if (userMoneyLog == null
                || userMoneyLog.getUserId() == null
                || userMoneyLog.getUseMoney() == null
                || userMoneyLog.getOrderId() == null
                || userMoneyLog.getUseMoney().compareTo(BigDecimal.ZERO) <= 0) {
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        //2.查询订单余额使用日志
        TradeUserMoneyLogExample userMoneyLogExample = new TradeUserMoneyLogExample();
        TradeUserMoneyLogExample.Criteria criteria = userMoneyLogExample.createCriteria();
        criteria.andOrderIdEqualTo(userMoneyLog.getOrderId());
        criteria.andUserIdEqualTo(userMoneyLog.getUserId());
        //查历史日志，为了区分回退余额和扣除余额
        int r = userMoneyLogMapper.countByExample(userMoneyLogExample);

        TradeUser tradeUser = tradeUserMapper.selectByPrimaryKey(userMoneyLog.getUserId());
        //3.扣减余额...
        if(userMoneyLog.getMoneyLogType().intValue()==ShopCode.SHOP_USER_MONEY_PAID.getCode().intValue()){
            if(r>0){
                //已经付款
                CastException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY);
            }
            //减余额
            tradeUser.setUserMoney(new BigDecimal(tradeUser.getUserMoney()).subtract(userMoneyLog.getUseMoney()).longValue());
            tradeUserMapper.updateByPrimaryKey(tradeUser);
        }
        //4.回退余额...
        if(userMoneyLog.getMoneyLogType().intValue()==ShopCode.SHOP_USER_MONEY_REFUND.getCode().intValue()) {
            if (r < 0) {
                //如果没有支付,则不能回退余额
                CastException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY);
            }
            //防止多次退款
            TradeUserMoneyLogExample userMoneyLogExample2 = new TradeUserMoneyLogExample();
            TradeUserMoneyLogExample.Criteria criteria1 = userMoneyLogExample2.createCriteria();
            criteria1.andOrderIdEqualTo(userMoneyLog.getOrderId());
            criteria1.andUserIdEqualTo(userMoneyLog.getUserId());
            criteria1.andMoneyLogTypeEqualTo(ShopCode.SHOP_USER_MONEY_REFUND.getCode());
            //再查历史日志，如果记录大于0，即一个订单和用户多次退款
            int r2 = userMoneyLogMapper.countByExample(userMoneyLogExample2);
            if (r2 > 0) {
                CastException.cast(ShopCode.SHOP_USER_MONEY_REFUND_ALREADY);
            }
            //退款
            tradeUser.setUserMoney(new BigDecimal(tradeUser.getUserMoney()).add(userMoneyLog.getUseMoney()).longValue());
            tradeUserMapper.updateByPrimaryKey(tradeUser);
        }
        //5.记录订单余额使用日志
        userMoneyLog.setCreateTime(new Date());
        userMoneyLogMapper.insert(userMoneyLog);
        return new Result(ShopCode.SHOP_SUCCESS.getSuccess(),ShopCode.SHOP_SUCCESS.getMessage());
    }
}
