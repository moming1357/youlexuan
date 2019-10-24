app.controller('brandController', function ($scope, brandService, $controller) {
//继承父controller
    $controller('baseController', {$scope: $scope})

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
       brandService.save($scope.entity).success(function (response) {
            if (response.falg) {
                $scope.reloadList();
            } else {
                alert(response.msg)
            }
        })
    }
    /*查询一个id*/
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }
    $scope.batchdelete = function () {
       brandService.batchdelete($scope.selectIds).success(
            function (response) {
                if (response.falg) {
                    $scope.reloadList();//刷新列表
                }
            })
    }
    /*模糊查询*/
    $scope.searchEntity = {};//定义搜索对象
//条件查询
    $scope.search = function (page, rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.paginationConf.totalItems = response.total;//总记录数
                $scope.list = response.rows;//给列表变量赋值
                console.info($scope.list)
            }
        );
    }


})