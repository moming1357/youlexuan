app.controller('payController', function ($scope, $location, payService) {

    /*生成二维码*/
    $scope.createNative = function () {
        $scope.mytimeout = ""
        payService.createNative().success(
            function (response) {
                $scope.money = (response.total_amount);	//订单金额
                $scope.out_trade_no = response.out_trade_no;//订单号

                /*二维码生成工具生成二维码*/
                var qr = new QRious({
                    element: document.getElementById("qrious"),
                    size: 200,
                    level: 'H',
                    value: response.qrcode
                })
                queryPayStatus(response.out_trade_no);//查询支付状态
            }
        )
    }

    /*查询支付状态*/
    $scope.mytimeout = ""

    function queryPayStatus(out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(
            function (response) {
                if (response.falg) {
                    location.href = "paysuccess.html#?money=" + $scope.money;
                } else {
                    if (response.msg == '二维码超时1') {
                        $scope.mytimeout = response.msg;
                    } else if (response.msg == '该订单已失效，已取消订单') {
                        $scope.mytimeout = response.msg;
                    } else {
                        location.href = "payfail.html";
                    }
                }

            }
        )
    }

    //获取金额
    $scope.getMoney = function () {
        return $location.search()['money'];
    }

})