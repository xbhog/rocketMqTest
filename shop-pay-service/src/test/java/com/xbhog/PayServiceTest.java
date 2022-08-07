package com.xbhog;

import com.xbhog.api.IPayService;
import com.xbhog.constant.ShopCode;
import com.xbhog.shop.entity.Result;
import com.xbhog.shop.pojo.TradePay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author xbhog
 * @describe:
 * @date 2022/8/7
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PayServiceApplication.class)
public class PayServiceTest {
    @Autowired
    private IPayService payService;


    /**
     * 创建支付订单
     */
    @Test
    public void createPayment(){
        long orderId = 753606770898898944L;
        TradePay tradePay = new TradePay();
        tradePay.setOrderId(orderId);
        tradePay.setPayAmount(new BigDecimal(880));
        payService.createPayment(tradePay);
    }
    /**
     * 支付回调测试
     */
    @Test
    public void callbackPayment() throws IOException {
        long payId = 753698930067382272L;
        long orderId = 753606770898898944L;

        TradePay tradePay = new TradePay();
        tradePay.setPayId(payId);
        tradePay.setOrderId(orderId);
        tradePay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
        payService.callbackPayMent(tradePay);

        System.in.read();
    }
}
