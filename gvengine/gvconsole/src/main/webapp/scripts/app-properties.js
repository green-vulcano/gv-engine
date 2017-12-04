angular.module('gvconsole')
.service('PropertiesService',['ENDPOINTS','$http', function(Endpoints, $http){

	this.getProperties = function(){
    	return $http.get(Endpoints.gvconfig + '/property');
    }

	this.getPropertyValue = function(key){
    	return $http.get(Endpoints.gvconfig + '/property/' + key);
    }

	this.setProperties = function(properties){
			return $http.put(Endpoints.gvconfig + '/property',properties,{headers:{'Content-Type':'application/json'}});
		}

}]);

angular.module('gvconsole')
.controller('PropertiesController',['PropertiesService', '$scope', function(PropertiesService, $scope){

	var instance = this;
	this.properties = {};
	this.alerts = [];

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

	$scope.modifyProperties = function(){
	PropertiesService.setProperties(instance.properties).then(function(response){
		instance.alerts.push({type: 'success', msg: 'Properties saved'});
		setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
	},function(response){
		instance.alerts.push({type: 'danger', msg: 'Properties could not be saved'});
		setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
	});
};
}]);
