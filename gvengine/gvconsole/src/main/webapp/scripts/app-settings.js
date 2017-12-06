angular.module('gvconsole')
 .service('SettingsService', ['ENDPOINTS', '$http', function(Endpoints, $http){
	 
	 var gvPoolObject;
	 
	 this.setObject = function(Subsystem,Initial_size,Maximum_size,Maximum_creation,Default_timeout,Shrink_timeout){
	  
		 	gvPoolObject = {
				 
				subsystem: Subsystem,
				initial_size: Initial_size,
				maximum_size: Maximum_size,
				maximum_creation: Maximum_creation,
				default_timeout: Default_timeout,
				shrink_timeout: Shrink_timeout
				 
		 };
		 
	 };
	 
	 this.getObject = function(){
		 return gvPoolObject;
	 }

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
  
  $scope.passData = function(subsystem,initial_size,maximum_size,maximum_creation,default_timeout,shrink_timeout){
	  SettingsService.setObject(subsystem,initial_size,maximum_size,maximum_creation,default_timeout,shrink_timeout);
  }


}]);


angular.module('gvconsole')
.controller('SettingsForm',['SettingsService', '$routeParams', '$scope', function(SettingsService, $routeParams, $scope){
	
	$scope.subsystem = $routeParams.settingId;
	
	var object = SettingsService.getObject();
	
	$scope.initial_size = object.initial_size;
	$scope.maximum_size = object.maximum_size;
	$scope.maximum_creation = object.maximum_creation;
	$scope.default_timeout = object.default_timeout;
	$scope.shrink_timeout = object.shrink_timeout;
	
	
}]);	

