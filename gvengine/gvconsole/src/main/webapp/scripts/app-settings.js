angular.module('gvconsole')
 .service('SettingsService', ['ENDPOINTS', '$http', function(Endpoints, $http){

	 this.getAllSettings = function() {
		 return $http.get(Endpoints.gvconfig + '/settings/');
	 }

	 this.getSettings = function(group) {
		 return $http.get(Endpoints.gvconfig + '/settings/'+group);
	 }
   
	 this.mergeSettings = function(group,setting) {
		 return $http.put(Endpoints.gvconfig + '/settings/'+ group, setting, {headers:{'Content-Type':'application/json'}});
	 }
 }]);

angular.module('gvconsole')
.controller('SettingsController',['SettingsService', '$scope', function(SettingsService, $scope){

  $scope.alerts = [];

  SettingsService.getAllSettings().then(
        function(response) {
          var settings = response.data;

          $scope.poolSettings = angular.isArray(settings.GVConfig.GVPoolManager.GreenVulcanoPool) ?
                                settings.GVConfig.GVPoolManager.GreenVulcanoPool :
                                [settings.GVConfig.GVPoolManager.GreenVulcanoPool];
        
          $scope.alerts = [];
          
        },
        function(response){
          switch (response.status) {

              case 401: case 403:
                $location.path('login');
                break;

              default:
                $scope.alerts.push({type: 'danger', msg: 'Data not available'});
                break;
              }
    });

}]);


angular.module('gvconsole')
.controller('PoolSettingsFormController',['SettingsService', '$routeParams', '$scope', function(SettingsService, $routeParams, $scope){
	
	$scope.flag = true;
	var poolSettings = [];
	$scope.currentPool = {};
	$scope.currentPoolIndex = -1;
	$scope.alerts = [];
		
	$scope.save = function(){
		
		if (currentPoolSystem==-1) {
			if(poolSettings.find(function(p){return p.subsystem==$scope.currentPool.settingId})) {
				$scope.alerts.push({type: 'danger', msg: 'please change subsystem name. it already exist'});
				setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			} else {
				poolSettings.push($scope.currentPool);
			}
		} else {
			poolSettings[currentPoolSystem] = $scope.currentPool;
		}
			
			
		SettingsService.mergeSettings('GVPoolManager', poolSettings).then(function(response){
			$scope.alerts.push({type: 'success', msg: 'Save success'});
			setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
		},function(response){
			$scope.alerts.push({type: 'danger', msg: 'Save error'});
			setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
		})		
	}
	
	SettingsService.getSettings('GVPoolManager').then(function(response){
		
		poolSettings = angular.isArray(response.data.GVPoolManager.GreenVulcanoPool) ?
				response.data.GVPoolManager.GreenVulcanoPool :
                [response.data.GVPoolManager.GreenVulcanoPool];
			
		if($routeParams.settingId != "new"){	
			
			$scope.currentPool = poolSettings.find(function(p){return p.subsystem==$routeParams.settingId});
			
			if ($scope.currentPool) {
				$scope.currentPoolIndex = poolSettings.indexOf($scope.currentPool);
			} else {
				$scope.currentPool = {};
				$scope.currentPool.subsystem = $routeParams.settingId;
			}		
			
		}		
		
		
	},function(response){
		console.log("Error: " + response.data);
	});
		
}]);	

