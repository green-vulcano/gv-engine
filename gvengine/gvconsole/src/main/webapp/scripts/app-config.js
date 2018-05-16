angular.module('gvconsole')
.service('ConfigService',['ENDPOINTS','$http', function(Endpoints, $http){

	this.getServices = function(){
		return $http.get(Endpoints.gvesb);
	};

	this.getConfigInfo = function() {
		return $http.get(Endpoints.gvconfig + '/deploy')
	}

	this.getConfigHistory = function(){
		return $http.get(Endpoints.gvconfig + '/configuration');
	}

}]);

angular.module('gvconsole')
.controller("ConfigController",["ConfigService","$scope",function(ConfigService,$scope){

	$scope.serviceByGroup = [];
	$scope.services = [];
	$scope.groups = [];
	$scope.nServices = 0;
	$scope.OpenServiceDet = false;
	var instance = this;
	this.configInfo = {};
	$scope.Allconfig = [];

  this.loadConfigInfo = function() {

		ConfigService.getConfigInfo().then(
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

	this.loadAllConfig = function() {
	ConfigService.getConfigHistory().then(function(response){
		$scope.Allconfig = response.data;
		ConfigService.getConfigInfo().then(
				function(response){
					$scope.Allconfig.push(response.data);
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
		},function(response){
			instance.alerts.push({type: 'danger', msg: 'Config history not available'});
		});
	};
	$scope.ngSelected = $scope.Allconfig[0];
	$scope.totExecutionOp = {};
	$scope.totFailSucc = {};
	$scope.tot = 0;
	$scope.totF = 0;
	$scope.totS = 0;
	$scope.init = function() {
		ConfigService.getServices().then(function(response){
			$scope.services = response.data;
			$scope.nServices = Object.keys($scope.services).length;
			angular.forEach($scope.services, function(service,key) {
				$scope.idS = service.idService;
				angular.forEach(service.operations, function(operation,key) {
					var infoOperation = $scope.idS+'/'+key;
					$scope.totExecutionOp[infoOperation] = operation.failures + operation.successes;
					$scope.tot += operation.failures + operation.successes ;
					$scope.totF += operation.failures;
					$scope.totS += operation.successes;
				});
				$scope.totFailSucc[$scope.idS] = {Tot : $scope.tot, Fail: $scope.totF, Succ : $scope.totS};
				$scope.tot = 0;
				$scope.totF = 0;
				$scope.totS = 0;
			});

	},function(response){
		console.log("error: " + response.data);
	});
}

}]);
