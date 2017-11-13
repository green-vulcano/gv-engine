angular.module('gvconsole')
.service('DashboardService',['ENDPOINTS','$http', function(Endpoints, $http){

	this.getServices = function(){
		return $http.get(Endpoints.gvesb);
	};
	
}]);

angular.module('gvconsole')
.controller("DashboardController",["DashboardService","$scope",function(DashboardService,$scope){
    
	
	
}]);

