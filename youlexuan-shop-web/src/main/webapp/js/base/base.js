var app = angular.module('youlexuan', []);//定义模块
app.controller('brandController', function ($scope, $http) {
    //重新加载列表 数据
    $scope.reloadList = function () {
        //切换页码
        /*根据分页组件获取当前页与获取每页显示的数据数量*/
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        // $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
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
//分页
//         $scope.findPage = function (page, rows) {
//             $http.get('../brand/findPage.do?page=' + page + '&rows=' + rows).success(
//                 function (response) {
//                     $scope.list = response.rows;//从后台接收的数据list集合
//                     $scope.paginationConf.totalItems = response.total;//更新总记录数
//                 }
//             );
//         }
    /*添加或者修改数据*/
    /*x向后台传递对象数据
    * 传递对象时不能使用get请求*/
    $scope.save = function () {
        console.info($scope.entity.name + $scope.entity.firstChar)
        $http.post("../brand/addBrand.do", $scope.entity).success(function (response) {
            if (response.falg) {
                $scope.reloadList();
            } else {
                alert(response.msg)
            }
        })
    }
    /*查询一个id*/
    $scope.findOne = function (id) {
        $http.get('../brand/findOne.do?id=' + id).success(
            function (response) {
                $scope.entity = response;
            }
        );
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
    $scope.batchdelete = function () {
        $http.get('../brand/batchdelete.do?ids=' + $scope.selectIds).success(
            function (response) {
                if (response.falg) {
                    $scope.reloadList();//刷新列表
                }
            })
    }
    $scope.andReload = function () {
        $scope.reloadList();//刷新列表
    }

    /*模糊查询*/
    $scope.searchEntity = {};//定义搜索对象

//条件查询
    $scope.search = function (page, rows) {
        // alert($scope.searchEntity.name)
        $http.post('../brand/search.do?page=' + page + "&rows=" + rows, $scope.searchEntity).success(
            function (response) {
                $scope.paginationConf.totalItems = response.total;//总记录数
                $scope.list = response.rows;//给列表变量赋值
                console.info($scope.list)
            }
        );
    }

});