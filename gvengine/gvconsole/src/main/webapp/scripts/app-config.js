angular.module('gvconsole')
 .service('ConfigService', ['$http', '$location', function($http, $location){

	 	var endpoint = 'http://localhost:8181/cxf/gvesb';

		this.getServices = function(){
			 return $http.get(endpoint);
		}	  

 }]);

angular.module('gvconsole')
.controller('ConfigController',['ConfigService' ,'$scope', '$location', function(ConfigService, $scope, $location){

	var instance = this;

	this.alerts = [];

	this.services = {};
	
	ConfigService.getServices().then(
				function(response){
					instance.alerts = [];
					instance.services = response.data;
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
			$scope.loadStatus = "error";
		});

}]);

