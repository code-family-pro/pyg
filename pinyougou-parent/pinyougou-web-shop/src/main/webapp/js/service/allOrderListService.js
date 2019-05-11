// 定义服务层:
app.service("allOrderListService",function($http){
	this.findAll = function(){
		return $http.get("../allOrderList/findAll.do");
	}
	
	this.findPage = function(page,rows){
		return $http.get("../allOrderList/findPage.do?pageNum="+page+"&pageSize="+rows);
	}
	
	this.add = function(entity){
		return $http.post("../allOrderList/add.do",entity);
	}
	
	this.update=function(entity){
		return $http.post("../allOrderList/update.do",entity);
	}
	
	this.findOne=function(orderId){
		return $http.get("../allOrderList/findOne.do?orderId="+orderId);
	}
	
	this.dele = function(ids){
		return $http.get("../allOrderList/delete.do?ids="+ids);
	}
	
	this.search = function(page,rows,searchEntity){
		return $http.post("../allOrderList/search.do?pageNum="+page+"&pageSize="+rows,searchEntity);
	}
	
	this.selectOptionList = function(){
		return $http.get("../allOrderList/selectOptionList.do");
	}
});