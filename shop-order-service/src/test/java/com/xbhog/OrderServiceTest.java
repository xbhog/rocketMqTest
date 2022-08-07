package com.xbhog;

import com.xbhog.api.IOrderService;
import com.xbhog.shop.pojo.TradeOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author xbhog
 * @describe:订单流程测试
 * @date 2022/8/2
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrderServiceApplication.class)
public class OrderServiceTest {

    @Autowired
    private IOrderService orderService;

    @Test
    public void confirmOrder() throws IOException {
        Long coupouId = 345988230098857984L;
        Long goodsId = 345959443973935104L;
        Long userId = 345963634385633280L;
        //创建订单
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setGoodsId(goodsId);
        tradeOrder.setCouponId(coupouId);
        tradeOrder.setUserId(userId);

        tradeOrder.setAddress("北京");
        tradeOrder.setGoodsNumber(1);
        tradeOrder.setGoodsPrice(new BigDecimal(1000));
        tradeOrder.setShippingFee(BigDecimal.ZERO);
        tradeOrder.setOrderAmount(new BigDecimal(1000));
        tradeOrder.setMoneyPaid(new BigDecimal(100));
        orderService.confirmOrder(tradeOrder);
        System.in.read();
    }
}
