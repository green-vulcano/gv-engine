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
.controller('SettingsController',['SettingsService', '$location', '$scope', function(SettingsService, $location, $scope){

  $scope.alerts = [];  
  
  SettingsService.getSettings('GVPoolManager').then(
        function(response) {
        	$scope.poolSettings = response.data;
    		
    		if (!angular.isArray($scope.poolSettings.GVPoolManager.GreenVulcanoPool)) {
    		}
        
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
.controller('PoolSettingsFormController',['SettingsService', '$routeParams', '$location', '$scope', function(SettingsService, $routeParams, $location, $scope){
	
	
	var poolSettings = {};
	$scope.currentPool = {};
	$scope.currentPoolIndex = -1;
	$scope.alerts = [];		
	
	$scope.subsystemInConflict = false; 
	
	$scope.checkSubsytem = function() {
		$scope.subsystemInConflict = poolSettings.GVPoolManager.GreenVulcanoPool.find(function(p){return p.subsystem==$scope.currentPool.subsystem}) && $scope.currentPoolIndex==-1;
	}
		
	$scope.savePool = function(){
		
		if ($scope.currentPoolIndex==-1) {
			poolSettings.GVPoolManager.GreenVulcanoPool.push($scope.currentPool);
		} else {
			poolSettings.GVPoolManager.GreenVulcanoPool[$scope.currentPoolIndex] = $scope.currentPool;
		}			
			
		SettingsService.mergeSettings('GVPoolManager', poolSettings).then(function(response){
			$scope.alerts.push({type: 'success', msg: 'Save success'});
			setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
		},function(response){
			
			switch (response.status) {

		        case 401: case 403:
		          $location.path('login');
		          break;
		
		        default:
		          $scope.alerts.push({type: 'danger', msg: 'Error saving data'});
		          break;
	        }			
			
		})		
	}
	
	$scope.deletePool =  function() {
		  poolSettings.GVPoolManager.GreenVulcanoPool.splice($scope.currentPoolIndex,1);
		  
		  SettingsService.mergeSettings('GVPoolManager', poolSettings).then(function(response){
			  $location.path('/settings');
			},function(response){
				
				switch (response.status) {

			        case 401: case 403:
			          $location.path('login');
			          break;
			
			        default:
			          $scope.alerts.push({type: 'danger', msg: 'Error deleting data'});
			          break;
		        }			
				
			});
	  }
	
	SettingsService.getSettings('GVPoolManager').then(function(response){
		
		poolSettings = response.data;
		
		if (!angular.isArray(poolSettings.GVPoolManager.GreenVulcanoPool)) {
			poolSettings.GVPoolManager.GreenVulcanoPool = [poolSettings.GVPoolManager.GreenVulcanoPool];
		}
		
		if($routeParams.settingId != "new"){	
			
			$scope.currentPool = poolSettings.GVPoolManager.GreenVulcanoPool.find(function(p){return p.subsystem==$routeParams.settingId});
			
			if ($scope.currentPool) {
				$scope.currentPoolIndex = poolSettings.GVPoolManager.GreenVulcanoPool.indexOf($scope.currentPool);
			} else {
				$scope.currentPool = {};
				$scope.currentPool.subsystem = $routeParams.settingId;
			}		
			
		}		
		
		
	},function(response){
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

