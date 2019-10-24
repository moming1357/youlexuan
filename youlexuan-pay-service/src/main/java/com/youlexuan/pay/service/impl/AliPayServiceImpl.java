package com.youlexuan.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ujiuye.mapper.TbOrderMapper;
import com.ujiuye.mapper.TbPayLogMapper;
import com.ujiuye.pojo.TbOrder;
import com.ujiuye.pojo.TbPayLog;
import com.youlexuan.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class AliPayServiceImpl implements AliPayService {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbOrderMapper orderMapper;

    /**
     * @param out_trade_no 订单号
     * @param total_amount 订单金额
     *                     subject  标题   三个必选请求参数
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_amount) {
        Map map = new HashMap();
        //创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," +
                "    \"total_amount\":\"" + total_amount + "\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        //发出预下单业务请求
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //从响应对象读取相应结果
            String code = response.getCode();//网关返回码
            System.out.println("响应码code对应的值为:" + code);
            //全部的响应结果
            String body = response.getBody();
            System.out.println("返回结果:" + body);

            if (code.equals("10000")) {//10000代表  接口调用成功
                map.put("qrcode", response.getQrCode());//生成二维码码串，用二维码生成工具生成二维码
                map.put("out_trade_no", response.getOutTradeNo());//商户的订单号
                map.put("total_amount", total_amount);//订单金额
                System.out.println("qrcode:" + response.getQrCode());
                System.out.println("out_trade_no:" + response.getOutTradeNo());
                System.out.println("total_amount:" + total_amount);
            } else {
                System.out.println("预下单接口调用失败:" + body);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    /*查询订单状态信息
     * out_trade_no 订单号
     * trade_no 支付宝交易号   二选一即可*/
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map map = new HashMap();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":" + out_trade_no + "," +
                "\"trade_no\":\"\"" +
                "  }");
        /*发送请求，获取响应结果集*/
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            String code = response.getCode();//网关返回码
            System.out.println("查询订单状态的订单返回码:" + code);
            if (code.equals("10000")) {//返回正常
                map.put("out_trade_no", out_trade_no);//获取订单号
                map.put("trade_no", response.getTradeNo());//获取支付宝交易号
                map.put("tradestatus", response.getTradeStatus());//获取交易状态
                /*交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、
                            TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、
                            TRADE_SUCCESS（交易支付成功）、
                            TRADE_FINISHED（交易结束，不可退款）*/
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 根据用户查询payLog
     *
     * @param userId
     * @return
     */
    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    /**
     * 修改订单状态
     *
     * @param out_trade_no   支付订单号
     * @param transaction_id 返回的交易流水号
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        System.out.println("支付宝订单号" + out_trade_no + "流水交易号" + transaction_id);
        /*TbPayLog是在生成订单时生成的order  中add()*/
//1.修改支付日志状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        System.out.println("---------------" + payLog);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");//已支付
        payLog.setTransactionId(transaction_id);//交易号
        payLogMapper.updateByPrimaryKeySelective(payLog);
        //2.修改订单状态
        String orderList = payLog.getOrderList();//获取订单号列表
        String[] orderIds = orderList.split(",");//获取订单号数组

        for (String orderId : orderIds) {
            TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (order != null) {
                order.setStatus("2");//已付款
                orderMapper.updateByPrimaryKeySelective(order);
            }
        }
        //清除redis缓存数据
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }

    /**
     * 关闭支付
     *
     * @param out_trade_no 支付订单号
     * @return
     */
    @Override
    public Map closePay(String out_trade_no) {
        Map map = new HashMap();
        /*创建关闭支付对象*/
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数;
        /*发送请求对象*/
        try {
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            String code = response.getCode();
            if (code.equals("10000")) {
                System.out.println("返回值:" + response.getBody());
                map.put("code", code);
                map.put("out_trade_no", out_trade_no);
                return map;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
