angular.module('gvconsole')
.service('SchedulerService', [ 'ENDPOINTS', '$http', function(Endpoints, $http){

 	this.getAll = function() {
 		return $http.get(Endpoints.gvscheduler + '/schedules');
 	}

 	this.get = function(id) {
 		return $http.get(Endpoints.gvscheduler + '/schedules/' +id);
 	}

 	this.pause = function(id) {
 		return $http.put(Endpoints.gvscheduler + '/schedules/' +id +'/pause')
 	}

 	this.resume = function(id) {
 		return $http.put(Endpoints.gvscheduler + '/schedules/' +id +'/resume')
 	}

 	this.delete = function(id) {
 		return $http.delete(Endpoints.gvscheduler + '/schedules/' +id);
 	}

 	this.create = function(service, operation, schedule) {
 		return $http.post(Endpoints.gvscheduler + '/schedule/'+ service + '/' + operation, schedule, {headers: {'Content-Type':'application/json'} });
 	}
}]);


angular.module('gvconsole')
.controller('ScheduleListController',['SchedulerService','$rootScope', '$scope', '$location', function(SchedulerService,$rootScope, $scope, $http, $location){

  var instance = this;
  for( prop in $rootScope.globals.currentUser.roles){
    if($rootScope.globals.currentUser.isAdministrator || $rootScope.globals.currentUser.isSchedulerManager){
      $scope.auth = true;
    }
  };
  this.alerts = [];

  this.schedules = {};
  this.currentPage = 1;
  this.loadList = function() {
	  SchedulerService.getAll().then(

      function(response){
        instance.alerts = [];
        instance.schedules = response.data;

      },
      function(response){
        switch (response.status) {

        case 401: case 403:
          $location.path('login');
          $scope.dataNa=true;
          break;

        case 404:
            instance.alerts.push({type: 'warning', msg: 'GVScheduler module not found'});
            $scope.dataNa=true;
            break;

        default:
          instance.alerts.push({type: 'danger', msg: 'Data not available'});
          $scope.dataNa=true;
          break;
      }

      $scope.loadStatus = "error";
    });
  }

  this.delete = function(id) {
		SchedulerService.delete(id).then(function(response){
			instance.loadList();
		},function(response){
			instance.alerts.push({type: 'danger', msg: 'Error processing delete command'});
		});

  }

  this.pause = function(id) {
		SchedulerService.pause(id).then(function(response){
			instance.update(id);
		},function(response){
			instance.alerts.push({type: 'danger', msg: 'Error processing pause command'});
		});

  }

  this.resume = function(id) {
		SchedulerService.resume(id).then(function(response){
			instance.update(id);
		},function(response){
			instance.alerts.push({type: 'danger', msg: 'Error processing resume command'});
		});

  }

  this.update = function(id) {
	  SchedulerService.get(id).then(function(response){
			instance.schedules[id] = response.data;
		},function(response){
			instance.loadList();
		});
  }

  instance.loadList();

}]);

angular.module('gvconsole')
	.controller('ScheduleFormController', ['ConfigService', 'SchedulerService', '$scope','$http', '$location',
				function(ConfigService, SchedulerService, $scope, $http, $location){

    $scope.cronConfig = {
  		allowMultiple: true
  	}

    $scope.alerts = [];
    $scope.operations = [];

    ConfigService.getServices().then(
    	function(response) {
		    angular.forEach(response.data, function(service, sName) {
		    	angular.forEach(service.operations, function(operation, oName) {
		    		$scope.operations.push({ service: sName, operation: oName, key: sName+'/'+oName});
		    	});
		    });
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

    $scope.schedule = {};

	$scope.addParameter = function(){

		if ($scope.schedule.properties == undefined) {
			$scope.schedule.properties = {};
		}

		if ($scope.key && $scope.key.trim().length > 0) {
			   $scope.schedule.properties[$scope.key]= $scope.value;
			   $scope.key = '';
			   $scope.value = '';
		}
	};

	$scope.save = function(){
		SchedulerService.create($scope.flow.service, $scope.flow.operation, $scope.schedule)
						.then(
								function(response){
									$location.path('schedule');
								},

								function(response){
									instance.alerts.push({type: 'danger', msg: 'Failed to schedule operation'});
								});
	}

	$scope.removeParameter=function(key){
		 delete $scope.schedule.properties[key];
	};

}]);
