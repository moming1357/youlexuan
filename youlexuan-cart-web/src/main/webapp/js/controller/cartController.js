//控制层
app.controller('cartController', function ($scope, $location, cartService) {

    /*查询购物车列表*/
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);//求合计数
            }
        )
    }
    /*添加商品到购物车*/
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.falg) {
                    $scope.findCartList()
                } else {
                    alert(response.msg)
                }
            }
        )
    }
    /*查询所有地址信息*/
    $scope.findListByLoginUser = function () {
        cartService.findListByLoginUser().success(
            function (response) {
                $scope.addressList = response
                for (var i = 0; $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault == 1) {
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }
            }
        )
    }
    /*选择收货地址*/
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }
    /*判断是否选中当前地址*/
    $scope.isSelectAddress = function (address) {

        if (address == $scope.address) {
            return true;
        } else {
            return false;
        }
    }


    //根据id查询收货地址信息
    $scope.findOne = function (id) {
        cartService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //添加一个收货地址
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = cartService.update($scope.entity); //修改
        } else {
            serviceObject = cartService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.falg) {
                    //重新查询
                    location = "getOrderInfo.html"
                } else {
                    alert(response.msg);
                }
            }
        );
    }
    $scope.dele = function (id) {
        cartService.dele(id).success(
            function (resp) {
                if (resp.falg) {
                    location = "getOrderInfo.html"
                } else {
                    alert(resp.msg)
                }
            }
        )
    }

    $scope.order = {paymentType: '1'};
//选择支付方式
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    //保存订单
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver = $scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.falg) {
                    //页面跳转
                    if ($scope.order.paymentType == '1') {//如果是微信支付，跳转到支付页面
                        location.href = "pay.html";
                    } else {//如果货到付款，跳转到提示页面
                        location.href = "paysuccess.html";
                    }
                } else {
                    alert(response.msg);	//也可以跳转到提示页面
                }
            }
        );
    }


})