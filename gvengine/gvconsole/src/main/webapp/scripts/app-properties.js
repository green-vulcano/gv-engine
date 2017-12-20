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

	this.getGVCoreProperties = function(id){
			return $http.get(Endpoints.gvconfig + '/configuration/' + id + '/properties');
	}

	this.deleteProperties = function(key){
		return $http.delete(Endpoints.gvconfig + '/property/' + key);
	}

	this.getConfigInfo = function() {
		return $http.get(Endpoints.gvconfig + '/deploy')
	}

}]);

angular.module('gvconsole')
.controller('PropertiesController',['PropertiesService', '$scope',  function(PropertiesService, $scope){

	var instance = this;
	$scope.properties = [];
	this.Json = {};
	this.alerts = [];
	this.coreProperties = [];
	this.check = {};
	$scope.i = 0;

	PropertiesService.getConfigInfo().then(function(response) {
		$scope.currentConfiguration = response.data.id;
		PropertiesService.getGVCoreProperties($scope.currentConfiguration).then(function(response) {
			angular.forEach(response.data, function(value, key) {
				if (instance.coreProperties.indexOf(value) == -1){
				instance.coreProperties[$scope.i] = value;
				PropertiesService.getProperties().then(function(response){
					instance.properties = response.data;
				if (instance.properties[value]){
					instance.check[value] = true;
				}
			});
			$scope.i++;
			};
			});
		});
	});

	PropertiesService.getProperties().then(function(response){
		$scope.properties = [];
		angular.forEach(response.data, function(value, key) {
				$scope.properties.push({key: key, value: value});
			});
	},function(response){
		console.log("error: " + response.data);
	});


	$scope.addProperties = function() {
		$scope.properties.push({key: undefined, value: undefined});
		instance.alerts.push({type: 'info', msg: 'Property has been added to the last position'});
	};

	$scope.deleteProperty = function(key) {
		PropertiesService.deleteProperties(key).then(function(response){
			instance.alerts.push({type: 'success', msg: 'Properties deleted'});
			setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			PropertiesService.getProperties().then(function(response){
				$scope.properties = [];
				angular.forEach(response.data, function(value, key) {
						$scope.properties.push({key: key, value: value});
					});
				});
		},function(response){
			instance.alerts.push({type: 'danger', msg: 'Properties not deleted'});
			setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			console.log("error: " + response.data);
			})

	}

	$scope.matchKey = function(){
		if ($scope.key != undefined && $scope.key != "") {
		PropertiesService.getPropertyValue($scope.key).then(function(response){
			$scope.value = response.data;
		},function(response){
			console.log("error: " + response.data);
			})
		}
	}



	$scope.modifyProperties = function(){
		var j = 0;
		for (j=0; j < $scope.properties.length ; j++) {
			instance.Json[$scope.properties[j].key] = $scope.properties[j].value;
		};
		console.log(instance.Json);
	PropertiesService.setProperties(instance.Json).then(function(response){
		instance.alerts.push({type: 'success', msg: 'Properties saved'});
		setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
	},function(response){
		instance.alerts.push({type: 'danger', msg: 'Properties could not be saved'});
		setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
	});
};
}]);
