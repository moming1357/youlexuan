package com.youlexuan.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ujiuye.entity.Result;
import com.ujiuye.pojo.TbPayLog;
import com.ujiuye.pojo.TbSeckillOrder;
import com.youlexuan.order.service.OrderService;
import com.youlexuan.pay.service.AliPayService;
import com.youlexuan.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("pay")
public class PayController {
    @Reference
    private AliPayService aliPayService;
    @Reference
    private SeckillOrderService seckillOrderService;


    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询秒杀订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        //判断秒杀订单存在
        if (seckillOrder != null) {
            long fen = (long) (seckillOrder.getMoney().doubleValue());//金额
            return aliPayService.createNative(seckillOrder.getId() + "", +fen + "");
        } else {
            return new HashMap();
        }
    }


    /**
     * 查询支付状态信息
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        /*获取当前登录用户id*/
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
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
                //保存秒杀结果到数据库
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("trade_no") + "");
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
            /*二维码失效*/
            if (x >= 10 && x <= 60) {
                result = new Result(false, "二维码超时1");
                break;
            }
            /*当超过订单失效时间则取消订单*/
//            if (x > 60) {
//                result = new Result(false, "该订单已失效，已取消订单");
//                /*调用支付关闭接口*/
//                Map closeMap = aliPayService.closePay(out_trade_no);
//                if ("10000".equals(closeMap.get("code"))) {//如果返回结果是正常关闭
//                    /*删除存在redis的本条未支付订单*/
//                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
//                }
//                break;
//            }
        }
        System.out.println(result.isFalg());
        System.out.println(result.getMsg());
        return result;
    }

}