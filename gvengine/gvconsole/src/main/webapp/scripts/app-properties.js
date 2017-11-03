angular.module('gvconsole')
.service('PropertiesService',['ENDPOINTS','$http', function(Endpoints, $http){
	
	this.getProperties = function(){
    	return $http.get(Endpoints.gvconfig + '/property');
    }
	
	this.getPropertyValue = function(key){
    	return $http.get(Endpoints.gvconfig + '/property/' + key);
    }

}]);

angular.module('gvconsole')
.controller('PropertiesController',['PropertiesService', '$scope', function(PropertiesService, $scope){
	
	var instance = this;
	this.properties = {};
	
	PropertiesService.getProperties().then(function(response){
		instance.properties = response.data;
	},function(response){
		console.log("error: " + response.data);
	});
	
	$scope.matchKey = function(){
		PropertiesService.getPropertyValue($scope.key).then(function(response){
			$scope.value = response.data;
		},function(response){
			console.log("error: " + response.data);
		})
	}


}]);
