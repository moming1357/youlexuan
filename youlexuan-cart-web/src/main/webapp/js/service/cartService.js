//服务层
app.service("cartService", function ($http) {
    /*查询购物车列表*/
    this.findCartList = function () {
        return $http.get('../cart/findCartList.do')
    }
    /*添加商品到购物车*/
    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('../cart/addGoodsToCartList.do?itemId=' + itemId + '&num=' + num);
    }
    /*合计数计算*/
    this.sum = function (cartList) {
        var totalValue = {totalNum: 0, totalMoney: 0.00};//合计实体
        for (var i = 0; i < cartList.length; i++) {
            var orderItem = cartList[i].orderItemList;
            for (var j = 0; j < orderItem.length; j++) {
                totalValue.totalNum += orderItem[j].num;
                totalValue.totalMoney += orderItem[j].totalFee;
            }
        }
        console.info(totalValue)
        return totalValue;
    }
    /*查询所有地址信息*/
    this.findListByLoginUser = function () {
        return $http.get('../address/findListByLoginUser.do');
    }


    //查询一个address信息
    this.findOne=function(id){
        return $http.get('../address/findOne.do?id='+id);
    }
    //增加一个address
    this.add=function(entity){
        return  $http.post('../address/add.do',entity );
    }
    //修改一个address
    this.update=function(entity){
        return  $http.post('../address/update.do',entity );
    }
    //删除一个address
    this.dele=function(ids){
        return $http.get('../address/delete.do?ids='+ids);
    }
    //保存订单
    this.submitOrder=function(order){
        return $http.post('../order/add.do',order);
    }
})