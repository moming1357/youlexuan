//控制层
app.controller('goodsController', function ($scope, $location, $controller, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承
    $scope.status = ['未审核', '已审核', '已驳回', '关闭'];//商品状态
    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //富文本的回显
                editor.html($scope.entity.tbGoodsDesc.introduction);
                //图片的回显
                $scope.entity.tbGoodsDesc.itemImages =
                    JSON.parse($scope.entity.tbGoodsDesc.itemImages);
                //拓展属性的回显
                $scope.entity.tbGoodsDesc.customAttributeItems =
                    JSON.parse($scope.entity.tbGoodsDesc.customAttributeItems);
                console.info($scope.entity.tbGoodsDesc.customAttributeItems)
                //回显规格
                $scope.entity.tbGoodsDesc.specificationItems =
                    JSON.parse($scope.entity.tbGoodsDesc.specificationItems);
                console.info($scope.entity.tbGoodsDesc.specificationItems)
                //SKU列表规格列转换
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec =
                        JSON.parse($scope.entity.itemList[i].spec);
                }

            }
        );
    }

    //保存
    $scope.save = function () {
        //提取文本编辑器的值
        $scope.entity.tbGoodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.tbGoods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.falg) {
                    $scope.entity = {};
                    editor.html("");
                    location.href = "goods.html";//跳转到商品列表页
                } else {
                    alert(response.msg);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.falg) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    /*添加商品*/
    $scope.entity = {'tbGoods': {}, 'tbGoodsDesc': {'itemImages': [], specificationItems: []}}
    $scope.add = function () {
        $scope.entity.tbGoodsDesc.introduction = editor.html();//获取富文本编辑器中的数据
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.falg) {
                    alert('保存成功');
                    $scope.entity = {};//清空添加页面内容
                    editor.html('');//清空富文本编辑器
                } else {
                    alert(response.msg);
                }
            }
        );
    }

    /**
     * 上传图片
     */
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.falg) {//如果上传成功，取出url
                    $scope.image_entity.url = response.msg;//设置文件地址
                } else {
                    alert(response.msg);
                }
            }).error(function () {
            alert("上传发生错误");
        });
    };
    //添加图片列表
    $scope.add_image_entity = function () {
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
    }
    //列表中移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.tbGoodsDesc.itemImages.splice(index, 1);
    }

    /*查询所有一级分类*/
    $scope.findItemCat1List = function () {
        itemCatService.findItemCatListByPid(0).success(
            function (response) {
                $scope.findItemCat1List = response;
            })
    }
    /*当一级分类改变时查询对应的二级分类*/
    $scope.$watch("entity.tbGoods.category1Id", function (newParentId, oldParentId) {
        $scope.entity.tbGoods.typeTemplateId = "";//清空模板id
        $scope.findItemCat3List = "";//当一级分类改变时进行清空后面的三级分类
        $scope.entity.tbGoodsDesc.customAttributeItems = "";
        itemCatService.findItemCatListByPid(newParentId).success(
            function (response) {
                $scope.findItemCat2List = response;
            }
        )
    })
    /*当二级分类改变时查询三级分类*/
    $scope.$watch("entity.tbGoods.category2Id", function (newParentId, oldParentId) {
        $scope.entity.tbGoods.typeTemplateId = "";
        $scope.entity.tbGoodsDesc.customAttributeItems = "";
        itemCatService.findItemCatListByPid(newParentId).success(
            function (response) {
                $scope.findItemCat3List = response;
            }
        )
    })
    /*当三级分类改变时查询模板id*/
    $scope.$watch("entity.tbGoods.category3Id", function (newParentId, oldParentId) {
        itemCatService.findOne(newParentId).success(
            function (response) {
                $scope.entity.tbGoods.typeTemplateId = response.typeId;//将模板对象存到scope域中
            }
        )
    })
    /*当模板id改变时调用查询所有品牌*/
    $scope.$watch("entity.tbGoods.typeTemplateId", function (newId, oldId) {
        $scope.entity.tbGoodsDesc.specificationItems = [];
        // alert(newId)
        typeTemplateService.findOne(newId).success(
            function (resp) {
                $scope.typeTemplate = resp;//获取类型模板
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表
                //扩展属性
                // [{"text":"内存大小","value":"101M"},{"text":"颜色","value":"红色"}]
                //如果没有ID，则加载模板中的扩展数据
                if ($location.search()['id'] == null) {
                    $scope.entity.tbGoodsDesc.customAttributeItems =
                        JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
                }
            }
        )
        typeTemplateService.findSpecList(newId).success(
            function (obj) {
                $scope.specList = obj;
            })
    })
    /* 设置attributeName规格以及attributeValue规格项*/
    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey(//根据规格名查询对象
            $scope.entity.tbGoodsDesc.specificationItems, 'attributeName', name);
        if (object != null) {//如果对象存在说明数组中已经存在该规格,只需要在该规格下添加选中的规格项即可
            if ($event.target.checked) {//获取选中项(规格项),并将选中项加入到该规格下
                object.attributeValue.push(value);
            } else {//取消勾选，如果取消了勾选，执行移除操作
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选项
                //如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {//并且当一个对象中的值为空时,存在无意义删除掉即可
                    $scope.entity.tbGoodsDesc.specificationItems.splice(
                        $scope.entity.tbGoodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {//如果对象不存在证明数组中还没有该规格名以及对应的规格项
            //此时将规格以及其下规格项添加进去
            $scope.entity.tbGoodsDesc.specificationItems.push(
                {"attributeName": name, "attributeValue": [value]});
        }
    }

    //创建SKU列表
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];//初始
        var items = $scope.entity.tbGoodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }
//添加列值
    addColumn = function (list, columnName, conlumnValues) {
        var newList = [];//新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < conlumnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }
    /*商品分类获取123级分类*/
    $scope.itemCatList = [];
    $scope.findItemCatList = function () {
        /*查询所有分类*/
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    /*对每个id对应的分类名称后进行赋值*/
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        )
    }

    $scope.locationPage = function () {
        location = "goods_edit.html";
    }

    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.tbGoodsDesc.specificationItems;
        // console.info(items)
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (object == null) {
            return false;
        } else {
            if (object.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false;
            }
        }
    }
    /*上下架*/
    $scope.updateUpOrDown = function (isMarketable,id) {
        goodsService.updateUpOrDown(isMarketable,id).success(
            function (response) {
                if (response.falg) {
                    $scope.reloadList();//刷新列表
                } else {
                    alert(response.msg);
                }
            }
        )
    }

});