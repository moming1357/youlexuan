app.controller('baseController', function ($scope, $http) {

//分页控件配置
    $scope.paginationConf = {
        currentPage: 1,//设置当前页
        totalItems: 10,//数据库查询的总条数
        itemsPerPage: 10,//设置每页显示的数据条数
        perPageOptions: [10, 20, 30, 40, 50],//选择每页多少条数据
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };
    //重新加载列表 数据
    $scope.reloadList = function () {
        //切换页码
        /*根据分页组件获取当前页与获取每页显示的数据数量*/
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        // $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    /*批量删除*/
    $scope.selectIds = [];
    //获取复选框状态数据
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {//如果是被选中,则增加到数组
            $scope.selectIds.push(id);
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除
        }
    }

    $scope.andReload = function () {
        $scope.reloadList();//刷新列表
    }
    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);//将json字符串转换为json对象
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ","
            }
            value += json[i][key];
        }
        return value;
    }

    //从集合中按照key值(attributeName)查询对象({"attributeName":"网络","attributeValue":["移动3G","移动4G"]})
    /**
     *[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"机身内存","attributeValue":["16G","32G"]}]
     */
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }

});