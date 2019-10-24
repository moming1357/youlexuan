package com.youlexuan.pay.service;

import com.ujiuye.pojo.TbPayLog;

import java.util.Map;

public interface AliPayService {
    /**
     * 生成支付宝支付二维码
     *
     * @param out_trade_no 订单号
     * @param total_amount 订单金额
     * @return
     */
    public Map createNative(String out_trade_no, String total_amount);

    /**
     * 查询支付状态
     *
     * @param out_trade_no 订单号      该接口的请求参数须满足;商品订单号与支付宝交易号2选一
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 根据用户查询payLog
     *
     * @param userId
     * @return
     */
    public TbPayLog searchPayLogFromRedis(String userId);

    /**
     * 修改订单状态
     *
     * @param out_trade_no   支付订单号
     * @param transaction_id 返回的交易流水号
     */
    public void updateOrderStatus(String out_trade_no, String transaction_id);


    /**
     * 关闭支付
     *
     * @param out_trade_no 支付订单号
     * @return
     */
    public Map closePay(String out_trade_no);
}
