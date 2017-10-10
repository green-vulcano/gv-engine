angular.module('gvconsole')
.service('PropertiesService',['ENDPOINTS','$http', function(Endpoints, $http){

	this.getProperties = function(){
		return $http.get(Endpoints.gvproperties + '/properties');
	}
	
	this.saveProperties = function(content){
		return $http.post(Endpoints.gvproperties + '/properties',content,{headers:{'Content-type':'text/plain'}});
	}
	
	this.getProperty = function(key){
		return $http.get(Endpoints.gvproperties + '/property/' + key);
	}
	
}]);

angular.module('gvconsole')
.controller('PropertiesController',['PropertiesService', '$scope', function(PropertiesService, $scope){
	
	var instance = this;
	this.alerts = [];
	$scope.value = null;
	
	PropertiesService.getProperties().then(
			function(response){
				instance.alerts = [];
				$scope.propertiesFile = response.data.content;
			}, function(response){
				switch(response.status){
				case 404:
					instance.alerts.push({type: 'warning', msg: 'XMLConfig.properties file not found'});
					break;
				}
			});
	
	$scope.save = function(){
		PropertiesService.saveProperties($scope.propertiesFile);
	};
	
	/* Search single property from input key
	$scope.search = function(){
		PropertiesService.getProperty($scope.key).then(
			function(response){
				console.log("response.data: " + response.data);
				$scope.value = response.data;
			},function(response){
				switch(response.status){
				case 404:
					instance.alerts.push({type: 'warning',msg:'Property not found'});
					break;
				}
			})
	};*/
	
}]);
