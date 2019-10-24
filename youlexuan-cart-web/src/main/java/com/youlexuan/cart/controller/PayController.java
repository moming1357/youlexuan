package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ujiuye.entity.Result;
import com.ujiuye.pojo.TbPayLog;
import com.youlexuan.pay.service.AliPayService;
import com.youlexuan.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference(timeout = 10000)
    private AliPayService aliPayService;

    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        System.out.println("=============");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(userId+"生成验证码时的用户id");
        TbPayLog tbPayLog = aliPayService.searchPayLogFromRedis(userId);
        if (tbPayLog != null) {
            return aliPayService.createNative(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee() + "");
        } else {
            return new HashMap();
        }
    }

    /**
     * 查询支付状态信息
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;//用于计数循环的次数   防止用户不扫码的情况(不然会一直死循环下去)
        while (true) {
            //调用查询接口
            Map map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e1) {
                System.out.println("调用查询服务出错");
            }
            if (map == null) {//出错
                result = new Result(false, "支付出错");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {
                result = new Result(true, "支付成功");
                //修改订单状态
                aliPayService.updateOrderStatus(out_trade_no, map.get("trade_no") + "");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {
                result = new Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {
                result = new Result(true, "交易结束，不可退款");
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为3分钟x>=60
            x++;
            if (x >= 10) {
                result = new Result(false, "二维码超时");
                break;
            }
        }
        System.out.println(result.isFalg());
        System.out.println(result.getMsg());
        return result;
    }
}
