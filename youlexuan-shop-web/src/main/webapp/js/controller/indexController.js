app.controller("indexController", function ($scope, loginService, $controller) {

    //读取当前登录人
    $scope.showLoginName = function () {
        loginService.loginName().success(
            function (response) {
                $scope.loginName = response.loginName;
            }
        );
    }
})