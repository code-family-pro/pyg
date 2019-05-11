// 定义服务层:
app.service("seckillOrderService", function ($http) {

    this.findOne = function (id) {
        return $http.get("../seckillOrderService/findOne.do?id=" + id);
    }

    this.dele = function (ids) {
        return $http.get("../seckillOrderService/delete.do?ids=" + ids);
    }

    this.search = function (page, rows, searchEntity) {
        return $http.post('../seckillOrderService/search.do?page=' + page + "&rows=" + rows, searchEntity);
    }
});