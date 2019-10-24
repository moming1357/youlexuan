app.service('brandService', function ($http) {

    this.save = function (entity) {
        return $http.post("../brand/addBrand.do", entity)
    }
    /*查询一个id*/
    this.findOne = function (id) {
        return $http.get('../brand/findOne.do?id=' + id)
    }
    this.batchdelete = function (selectIds) {
        return $http.get('../brand/batchdelete.do?ids=' + selectIds)
    }

//条件查询
    this.search = function (page, rows, searchEntity) {
        return $http.post('../brand/search.do?page=' + page + "&rows=" + rows, searchEntity)
    }
/*查询所有品牌得到的List<map>*/
    this.selectOptionList = function () {
        return $http.get('../brand/selectOptionList.do');
    }


})