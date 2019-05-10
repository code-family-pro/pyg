// 定义控制器:
app.controller("allOrderListController",function($scope,$controller,$http,allOrderListService){
	// AngularJS中的继承:伪继承
	$controller('baseController',{$scope:$scope});
	
	// 查询所有的品牌列表的方法:
	$scope.findAll = function(){
		// 向后台发送请求:
		allOrderListService.findAll().success(function(response){
			$scope.list = response;
		});
	}

	// 分页查询
	$scope.findPage = function(page,rows){
		// 向后台发送请求获取数据:
		allOrderListService.findPage(page,rows).success(function(response){
			$scope.paginationConf.totalItems = response.total;
			$scope.list = response.rows;
		});
	}
	
	// 保存品牌的方法:
	$scope.save = function(){
		// 区分是保存还是修改
		var object;
		if($scope.entity.orderId != null){
			// 更新
			object = allOrderListService.update($scope.entity);
		}else{
			// 保存
			object = allOrderListService.add($scope.entity);
		}
		object.success(function(response){
			// {flag:true,message:xxx}
			// 判断保存是否成功:
			if(response.flag){
				// 保存成功
				alert(response.message);
				$scope.reloadList();
			}else{
				// 保存失败
				alert(response.message);
			}
		});
	}
	
	// 查询一个:
	$scope.findOne = function(orderId){
		allOrderListService.findOne(orderId).success(function(response){
			$scope.entity = response;
		});
	}
	
	// 删除品牌:
	$scope.dele = function(){
		allOrderListService.dele($scope.selectIds).success(function(response){
			// 判断保存是否成功:
			if(response.flag==true){
				// 保存成功
				// alert(response.message);
				$scope.reloadList();
				$scope.selectIds = [];
			}else{
				// 保存失败
				alert(response.message);
			}
		});
	}
	
	$scope.searchEntity={};
	// 显示状态状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价  {{status[entity.auditStatus]}}
	$scope.statusList = ["","未付款","已付款","未发货","已发货","交易成功","交易关闭","待评价"];
	// 假设定义一个查询的实体：searchEntity
	$scope.search = function(page,rows){
		// 向后台发送请求获取数据:
		allOrderListService.search(page,rows,$scope.searchEntity).success(function(response){
			$scope.paginationConf.totalItems = response.total;
			$scope.list = response.rows;
		});
	}
});
