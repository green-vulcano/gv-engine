angular.module('gvconsole')
.service('ConfigService',['ENDPOINTS','$http', function(Endpoints, $http){

	this.getServices = function(){
		return $http.get(Endpoints.gvesb);
	};

}]);

angular.module('gvconsole')
.controller("ConfigController",["ConfigService","$scope",function(ConfigService,$scope){

	$scope.serviceByGroup = [];
	$scope.groups = [];

	$scope.init = function() {
		ConfigService.getServices().then(function(response){
		angular.forEach(response.data,function(value){
			if(!$scope.groups.includes(value.groupName)){
				$scope.groups.push(value.groupName);

			}
			if(!$scope.serviceByGroup.hasOwnProperty(value.groupName)){
				$scope.serviceByGroup[value.groupName] = [];
			}

			$scope.serviceByGroup[value.groupName].push(value);
		})
	},function(response){
		console.log("error: " + response.data);
	});
}

}]);
