angular.module('gvconsole')
.service('MonitoringServices', ['ENDPOINTS', '$http', function(Endpoints, $http){

  this.getMemory = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/memory");
  }

  this.getClasses = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/classes");
  }

  this.getThreads = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/threads");
  }

  this.getCPU = function(){
    return $http.post(Endpoints.gvmonitoring + "/monitoring/cpuUsage");
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
						['cpuUsage', 500],
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
					   'SED00205':'cpuUsage'
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
              ['totalThreads',15000],
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
  					   'SED00204':'totalThreads',
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
 function(MonitoringServices, $scope, $http, chartServiceMemory, chartServiceClassesThreads, $interval ) {

$interval(function(){

  MonitoringServices.getMemory().then(
     function(response) {
       $scope.maxMemory = response.data.maxMemory;
       $scope.totalMemory = response.data.totalMemory;
       $scope.freeMemory = response.data.freeMemory;
       $scope.heapMemory = response.data.heapMemory;
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

  chartServiceMemory.updateGloveChart('SED00201', $scope.maxMemory);
  chartServiceMemory.updateGloveChart('SED00202', $scope.totalMemory);
  chartServiceMemory.updateGloveChart('SED00203', $scope.freeMemory);
  chartServiceMemory.updateGloveChart('SED00204', $scope.heapMemory);

  chartServiceClassesThreads.updateGloveChart('SED00201', $scope.totalLoadedClasses);
  chartServiceClassesThreads.updateGloveChart('SED00202', $scope.loadedClasses);
  chartServiceClassesThreads.updateGloveChart('SED00203', $scope.unLoadedClasses);
  chartServiceClassesThreads.updateGloveChart('SED00204', $scope.totalThreads);
  chartServiceClassesThreads.updateGloveChart('SED00205', $scope.daemonThreads);
  chartServiceClassesThreads.updateGloveChart('SED00206', $scope.peakThreads);

},1000);


$interval(function(){

  MonitoringServices.getCPU().then(
    function(response) {
       $scope.cpuUsage = response.data;
    }, function(response){
         $scope.error = response;
    });

  chartServiceMemory.updateGloveChart('SED00205', $scope.cpuUsage);
},2000);

}]);
