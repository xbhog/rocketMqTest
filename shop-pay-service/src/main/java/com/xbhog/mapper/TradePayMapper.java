package com.xbhog.mapper;


import com.xbhog.shop.pojo.TradePay;
import com.xbhog.shop.pojo.TradePayExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface TradePayMapper {
    int countByExample(TradePayExample example);

    int deleteByExample(TradePayExample example);

    int deleteByPrimaryKey(Long payId);

    int insert(TradePay record);

    int insertSelective(TradePay record);

    List<TradePay> selectByExample(TradePayExample example);

    TradePay selectByPrimaryKey(Long payId);

    int updateByExampleSelective(@Param("record") TradePay record, @Param("example") TradePayExample example);

    int updateByExample(@Param("record") TradePay record, @Param("example") TradePayExample example);

    int updateByPrimaryKeySelective(TradePay record);

    int updateByPrimaryKey(TradePay record);
}