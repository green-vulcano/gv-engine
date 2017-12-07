angular.module('gvconsole')
 .service('SettingsService', ['ENDPOINTS', '$http', function(Endpoints, $http){

	 this.getAllSettings = function() {
		 return $http.get(Endpoints.gvconfig + '/settings/');
	 }

	 this.getSettings = function(group) {
		 return $http.get(Endpoints.gvconfig + '/settings/'+group);
	 }
   
	 this.mergeSettings = function(group,setting) {
		 return $http.put(Endpoints.gvconfig + '/settings/'+group, setting, {headers:{'Content-Type':'application/json'}});
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
.controller('SettingsForm',['SettingsService', '$routeParams', '$scope', function(SettingsService, $routeParams, $scope){
	
	var group;
	var object;
	
		//a seconda di quale pulsante clicca nell'html, mi faccio passare una variabile che userò per effettuare la chiamata
		SettingsService.getSettings('GVPoolManager').then(function(response){
			
			group = response.data.GVPoolManager.GreenVulcanoPool;
			
			if($routeParams.settingId != "new"){
				object = group.find(function (o){return o.subsystem==$routeParams.settingId});
				$scope.subsystem = object['subsystem'];
				$scope.initial_size = object['initial-size'];
				$scope.maximum_size = object['maximum-size'];
				$scope.maximum_creation = object['maximum-creation'];
				$scope.default_timeout = object['default-timeout'];
				$scope.shrink_timeout = object['shrink-timeout'];
			}
		},function(response){
			console.log("Error: " + response.data);
		});
		
		object = {
				
				'subsystem': $scope.subsystem,
				'initial-size': $scope.initial_size,
				'maximum-size': $scope.maximum_size,
				'maximum-creation': $scope.maximum_creation,
				'default-timeout': $scope.default_timeout,
				'shrink-timeout': $scope.shrink_timeout
			}
		
}]);	

