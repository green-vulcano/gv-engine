angular.module('gvconsole-scheduling', ['angular-quartz-cron']);

angular.module('gvconsole-scheduling')
.controller('SchedulingListController',['$scope','$http', '$location', function($scope, $http, $location){
  var resourceURI = 'http://localhost:8181/cxf/gvscheduler/schedules';
  var instance = this;

  this.alerts = [];

  this.list = [];
  this.currentPage = 1;
  this.loadList = function() {
    $http.get(resourceURI).then(

      function(response){
        instance.alerts = [];
        instance.list = [];
        angular.forEach(response.data, function(id, entry) {
          instance.list.push(entry);
        });
      },
      function(response){
        switch (response.status) {

        case 401: case 403:
          $location.path('login');
          break;

        case 404:
            instance.alerts.push({type: 'warning', msg: 'GVScheduler module not found'});
            break;

        default:
          instance.alerts.push({type: 'danger', msg: 'Data not available'});
          break;
      }

      $scope.loadStatus = "error";
    });
  }

  instance.loadList();

}]);

angular.module('gvconsole-scheduling')
	.controller('SchedulingDetailController',['$scope','$http', '$location', function($scope, $http, $location){

    $scope.cronConfig = {
  		allowMultiple: true
  	}

    $scope.operation = '';
    $scope.schedule = {};

		$scope.addParameter=function(){
      if ($scope.schedule.properties == undefined) {
        $scope.schedule.properties = {};
      }
      if ($scope.key && $scope.key.trim().length > 0) {
			   $scope.schedule.properties[$scope.key]= $scope.value;
         $scope.key = '';
         $scope.value = '';
      }
		};
		$scope.removeParameter=function(key){
			 delete $scope.schedule.properties[key];
		};

	}]);
