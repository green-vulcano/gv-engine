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
		return $http.get(Endpoints.gvconfig + '/deploy');
	}
	
	this.getSystemProperties = function() {
		return $http.get(Endpoints.gvconfig + '/systemproperty');
	}

	this.getSystemProperty = function(key) {
		return $http.get(Endpoints.gvconfig + '/systemproperty/' + key);
	}

}]);

angular.module('gvconsole')
.controller('PropertiesController',['PropertiesService', '$scope',  function(PropertiesService, $scope){

	var instance = this;
	$scope.properties = [];
	this.Json = {};
	this.alerts = [];
	this.configInfo = {};
	this.coreProperties = [];
	this.check = {};
	$scope.newKey = null;
	$scope.newValue = null;
	$scope.i = 0;
	
	PropertiesService.getSystemProperty('it.greenvulcano.instance.name').then(
			function(response){
				$scope.instanceName = response.data;
			});
	
  this.getConfigInfo = function () {
		PropertiesService.getConfigInfo().then(
				function(response){
					instance.configInfo = response.data;
				},
				function(response){
					switch (response.status) {

							case 401: case 403:
								$location.path('login');
								break;

							default:
								instance.alerts.push({type: 'danger', msg: 'Data not available'});
								break;
					}
		});

	}


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

	$scope.getPropertiesList = function() {
		PropertiesService.getProperties().then(function(response){
			$scope.properties = [];
			angular.forEach(response.data, function(value, key) {
					$scope.properties.push({key: key, value: value});
				});
				console.log($scope.properties);
		},function(response){
			console.log("error: " + response.data);
		});
	};

	$scope.getPropertiesList();

	$scope.search = "";

	$scope.addProperties = function(newKey,newValue) {
		$scope.properties.push({key: newKey, value: newValue});
		instance.alerts.push({type: 'info', msg: 'Property has been added to the last position, remember to save!'});
		setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
	};

	$scope.deleteProperty = function(key) {
		console.log(key);
		PropertiesService.deleteProperties(key).then(function(response){
			instance.alerts.push({type: 'success', msg: 'Properties deleted'});
			setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			$scope.getPropertiesList();
		},function(response){
			instance.alerts.push({type: 'danger', msg: 'Properties not deleted'});
			setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			console.log("error: " + response.data);
			})

	}

	$scope.modifyProperties = function(){
		var j = 0;
		for (j=0; j < $scope.properties.length ; j++) {
			instance.Json[$scope.properties[j].key] = $scope.properties[j].value;
		};
	PropertiesService.setProperties(instance.Json).then(function(response){
		instance.alerts.push({type: 'success', msg: 'Properties saved'});
		setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
	},function(response){
		instance.alerts.push({type: 'danger', msg: 'Properties could not be saved'});
		setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
	});
};
}]);
