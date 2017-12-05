angular.module('gvconsole')
 .service('SettingsService', ['ENDPOINTS', '$http', function(Endpoints, $http){

       this.getAll = function() {
         return $http.get(Endpoints.gvconfig + '/settings/',{headers:{'Content-Type':'application/json'}});
       }

       this.get = function(name) {
         return $http.get(Endpoints.gvconfig + '/settings/'+name);
       }
 }]);

angular.module('gvconsole')
.controller('SettingsController',['SettingsService', '$scope', function(SettingsService, $scope){

  $scope.alerts = [];

  SettingsService.getAll().then(
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
.controller('SettingsForm',['SettingsService', '$routeParams', '$scope', function(SettingsService, $routeParams, $scope){
	
	$scope.subsystem = $routeParams.settingId;
	
	var i = 0;
	
	SettingsService.getAll().then(
	        function(response) {
	          var settings = response.data;

	          $scope.poolSettings = angular.isArray(settings.GVConfig.GVPoolManager.GreenVulcanoPool) ?
	                                settings.GVConfig.GVPoolManager.GreenVulcanoPool :
	                                [settings.GVConfig.GVPoolManager.GreenVulcanoPool];
	          
	          for(i=0; $scope.poolSettings[i].subsystem!=null; i++ ){
	      		if($scope.poolSettings[i].subsystem == $scope.subsystem){
	      			$scope.initial_size = $scope.poolSettings[i]['initial-size'];
	      			$scope.maximum_size = $scope.poolSettings[i]['maximum-size'];
	      			$scope.maximum_creation = $scope.poolSettings[i]['maximum-creation'];
	      			$scope.default_timeout = $scope.poolSettings[i]['default-timeout'];
	      			$scope.shrink_timeout = $scope.poolSettings[i]['shrink-timeout'];
	      			break;
	      		}
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

