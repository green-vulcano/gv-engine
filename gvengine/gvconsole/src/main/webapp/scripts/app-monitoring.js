angular.module('gvconsole')
.service('MonitoringServices', ['ENDPOINTS', '$http', function(Endpoints, $http){
  this.getMemory = function(){
    return $http.get(Endpoints.gvmonitoring + "/memory");
  }

  this.getClasses = function(){
    return $http.get(Endpoints.gvmonitoring + "/classes");
  }

  this.getThreads = function(){
    return $http.get(Endpoints.gvmonitoring + "/threads");
  }

  this.getCPU = function(){
    return $http.get(Endpoints.gvmonitoring + "/cpu");
  }

  this.getConfigInfo = function() {
    return $http.get(Endpoints.gvconfig + '/deploy')
  }

  this.getServices = function(){
		return $http.get(Endpoints.gvesb);
  };
  
  this.getActivePools = function(){
    return $http.get(Endpoints.gvmonitoring + "/pools");
  }

}]);

angular.module('gvconsole')
.controller('MonitoringController', ['$location', 'MonitoringServices', '$scope', '$http', '$interval',
 function($location, MonitoringServices, $scope, $http, $interval){

   var instance = this;
   this.activePool = {};
   this.configInfo = {};
   $scope.services = [];
   $scope.totExecutionOp = {};
   $scope.totFailSucc = {};
   $scope.tot = 0;
   $scope.totF = 0;
   $scope.totS = 0;

   this.loadConfigInfo = function() {

     MonitoringServices.getConfigInfo().then(
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

   MonitoringServices.getActivePools().then(
     function(response){
      instance.activePool = response.data;
      console.log(instance.activePool);
     },
     function(response){
       console.log("Error");
     }
   )


   $scope.init = function() {
     MonitoringServices.getServices().then(function(response){
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



    $interval(function(){

      if($location.path() == '/monitoring'){

      MonitoringServices.getMemory().then(
         function(response) {
           $scope.maxMemory = (response.data.maxMemory/1048576).toFixed(2);
           $scope.totalMemory = (response.data.totalMemory/1048576).toFixed(2);
           $scope.freeMemory = (response.data.freeMemory/1048576).toFixed(2);
           $scope.heapMemory = (response.data.heapMemory/1048576).toFixed(2);
      }, function(response){
           $scope.error = response;
      });

       MonitoringServices.getClasses().then(
          function(response) {
            $scope.totalLoadedClasses = response.data.totalLoadedClasses;
            $scope.loadedClasses = response.data.loadedClasses;
            $scope.unLoadedClasses = response.data.unLoadedClasses;
       }, function(response){
            $scope.error = response;
       });

        MonitoringServices.getThreads().then(
           function(response) {
             $scope.totalThreads = response.data.totalThreads;
             $scope.daemonThreads = response.data.daemonThreads;
             $scope.peakThreads = response.data.peakThreads;
        }, function(response){
             $scope.error = response;
        });

        MonitoringServices.getCPU().then(
          function(response) {
             $scope.cpuUse = response.data.usage;

          }, function(response){
               $scope.error = response;
          });
};

    },2000);


    $interval(function(){

      if($location.path() == '/monitoring'){

      MonitoringServices.getCPU().then(
          function(response) {
             $scope.cpuUse = response.data.usage
          }, function(response){
               $scope.error = response;
          });

        };

    },2000);

}]);
