angular.module('gvconsole')
.service('MonitoringServices', ['ENDPOINTS', '$http', function(Endpoints, $http){

  this.getMaxMemory = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/maxMemory");
  }

  this.getTotalMemory = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/totalMemory");
  }

  this.getFreeMemory = function() {
    return $http.post(Endpoints.gvmonitoring + "/monitoring/freeMemory");
  }

  this.getHeapMemory = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/heapMemory");
  }

  this.getCPU = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/cpuUsage");
  }

  this.getTotalLoadedClasses = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/totalLoadedClasses");
  }

  this.getLoadedClasses = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/loadedClasses");
  }

  this.getUnLoadedClasses = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/unLoadedClasses");
  }

  this.getThreads = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/threads");
  }

  this.getDaemonThreads = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/daemonThreads");
  }

  this.getPeakThreads = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/peakThreads");
  }

}]);

angular.module('gvconsole')
.factory('ChartServiceMemory', function() {

		function buildChart(chartID) {

			return c3.generate({
			  	bindto: chartID,
			  	data: {
				      	columns: [
				          	['maxMemory', 500],
						['totalMemory', 500],
						['freeMemory', 500],
						['heapMemory', 500],
						['CPU', 500],
				      	],
				      	type: 'bar'
			  	},
				axis: {
					y: {
					    max: 500,
					    min: 0,
					    padding: {top:0, bottom:0}
					}
				    },
			  	bar: {
				   width: 64
				}
			});
		};
		var gloveChart = buildChart("#glove-chart");

		return {
			updateGloveChart: function(id, value) {

					var gloveMap = {
					   'SED00201':'maxMemory',
					   'SED00202':'totalMemory',
					   'SED00203':'freeMemory',
					   'SED00204':'heapMemory',
					   'SED00205':'CPU'
					};

					gloveChart.load({
						columns: [[gloveMap[id], value]]
					});

			}
		};
	});

  angular.module('gvconsole')
  .factory('ChartServiceClassesThreads', function() {

  		function buildChart(chartID) {

  			return c3.generate({
  			  	bindto: chartID,
  			  	data: {
  				      	columns: [
  				          	['totalLoadedClasses', 15000],
  						['loadedClasses', 15000],
  						['unLoadedClasses', 15000],
              ['threads',15000],
  						['daemonThreads', 15000],
  						['peakThreads', 15000],
  				      	],
  				      	type: 'bar'
  			  	},
  				axis: {
  					y: {
  					    max: 15000,
  					    min: 0,
  					    padding: {top:0, bottom:0}
  					}
  				    },
  			  	bar: {
  				   width: 64
  				}
  			});
  		};
  		var gloveChart = buildChart("#glove-chart2");

  		return {
  			updateGloveChart: function(id, value) {

  					var gloveMap = {
  					   'SED00201':'totalLoadedClasses',
  					   'SED00202':'loadedClasses',
  					   'SED00203':'unLoadedClasses',
  					   'SED00204':'threads',
  					   'SED00205':'daemonThreads',
               'SED00206':'peakThreads'
  					};

  					gloveChart.load({
  						columns: [[gloveMap[id], value]]
  					});

  			}
  		};
  	});



angular.module('gvconsole')
.controller('MonitoringController', ['MonitoringServices', '$scope', '$http', 'ChartServiceMemory', 'ChartServiceClassesThreads' , '$interval',
 function(MonitoringServices, $scope, $http, chartServiceMemory, chartServiceClassesThreads, $interval) {

$interval(function(){

  MonitoringServices.getMaxMemory().then(
     function(response) {
       $scope.maxMemory = response.data;
  }, function(response){
       //fail case
       $scope.maxMemory = response;
   });

   MonitoringServices.getTotalMemory().then(
     function(response) {
       $scope.totalMemory = response.data;
  }, function(response){
       //fail case
       $scope.totalMemory = response;
   });

   MonitoringServices.getFreeMemory().then(
      function(response) {
        $scope.freeMemory = response.data;
   }, function(response) {
        //fail case
        $scope.freeMemory = response;
    });

   MonitoringServices.getHeapMemory().then(
     function(response) {
       $scope.heapMemory = response.data;
  }, function(response){
       //fail case
       $scope.heapMemory = response;
   });

   MonitoringServices.getTotalLoadedClasses().then(
    function(response) {
      $scope.totalLoadedClasses = response.data;
 }, function(response){
     //fail case
      $scope.totalLoadedClasses = response;
 });

   MonitoringServices.getLoadedClasses().then(
     function(response) {
       $scope.loadedClasses = response.data;
     }, function(response){
       //fail case
       $scope.loadedClasses = response;
     });

     MonitoringServices.getUnLoadedClasses().then(
       function(response) {
         $scope.unLoadedClasses = response.data;
       }, function(response){
         //fail case
         $scope.unLoadedClasses = response;
       });

   MonitoringServices.getThreads().then(
    function(response) {
      $scope.threads = response.data;
  }, function(response){
     //fail case
      $scope.threads = response;
  });

  MonitoringServices.getDaemonThreads().then(
   function(response) {
     $scope.daemonThreads = response.data;
 }, function(response){
    //fail case
     $scope.daemonThreads = response;
 });

 MonitoringServices.getPeakThreads().then(
  function(response) {
    $scope.peakThreads = response.data;
}, function(response){
   //fail case
    $scope.peakThreads = response;
});


  chartServiceMemory.updateGloveChart('SED00201', $scope.maxMemory);
  chartServiceMemory.updateGloveChart('SED00202', $scope.totalMemory);
  chartServiceMemory.updateGloveChart('SED00203', $scope.freeMemory);
  chartServiceMemory.updateGloveChart('SED00204', $scope.heapMemory);

  chartServiceClassesThreads.updateGloveChart('SED00201', $scope.totalLoadedClasses);
  chartServiceClassesThreads.updateGloveChart('SED00202', $scope.loadedClasses);
  chartServiceClassesThreads.updateGloveChart('SED00203', $scope.unLoadedClasses);
  chartServiceClassesThreads.updateGloveChart('SED00204', $scope.threads);
  chartServiceClassesThreads.updateGloveChart('SED00205', $scope.daemonThreads);
  chartServiceClassesThreads.updateGloveChart('SED00206', $scope.peakThreads);

},1000);

$interval(function(){

  MonitoringServices.getCPU().then(
    function(response) {
      $scope.CPU = response.data;
 }, function(response){
     //fail case
      $scope.CPU = response;
 });

  chartServiceMemory.updateGloveChart('SED00205', $scope.CPU);
},2000);

}]);
