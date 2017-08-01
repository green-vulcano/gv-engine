angular.module('gvconsole')
.service('MonitoringServices', ['ENDPOINTS', '$http', function(Endpoints, $http){
  this.getMemory = function(){
    return $http.get(Endpoints.gvmonitoring + "/monitoring/memory");
  }

  this.getClasses = function(){
    return $http.get(Endpoints.gvmonitoring + "/monitoring/classes");
  }

  this.getThreads = function(){
    return $http.get(Endpoints.gvmonitoring + "/monitoring/threads");
  }

  this.getCPU = function(){
    return $http.get(Endpoints.gvmonitoring + "/monitoring/cpu");
  }

}]);

/************** START CHART MEMORIES **************/
angular.module('gvconsole')
.factory('ChartServiceMemories', function() {

		function buildChart(chartID) {

			return c3.generate({
			  	bindto: chartID,
			  	data: {
				      	columns: [
				          	['maxMemory', 500],
						        ['totalMemory', 500],
        						['freeMemory', 500],
        						['heapMemory', 500]
				      	],
				      	type: 'bar',
                colors: {
                totalMemory: '#238650',
                freeMemory: '#961E24',
                heapMemory: '#124667',
            },
            labels: true
			  	},
				axis: {
  				y: {
  				    max: 550,
  				    min: 0,
  				    padding: {top:0, bottom:0}
  				}
  			},
        tooltip: {
          format: {
            title: function(){return "Memories";},
            value: function(value){return value + ' MB';}
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
					   'SED00204':'heapMemory'
					};

          if(value == null || value==undefined){
            gloveChart.load({
  						columns: [[gloveMap[id], 0]]
  					});
          }else{
					gloveChart.load({
						columns: [[gloveMap[id], value]]
					});
        }
			}
		};
	});
/************** END CHART MEMORIES **************/

/************** START CHART CLASSES **************/
  angular.module('gvconsole')
  .factory('ChartServiceClasses', function() {

  		function buildChart(chartID) {

  			return c3.generate({
  			  	bindto: chartID,
  			  	data: {
  				      	columns: [
  				    ['totalLoadedClasses', 15000],
  						['loadedClasses', 15000],
  						['unLoadedClasses', 15000],
  				      	],
  				      	type: 'bar',
                  colors: {
                  loadedClasses: '#238650',
                  unLoadedClasses: '#961E24'
              },
              labels: true
  			  	},

  				axis: {
  					y: {
  					    max: 17000,
  					    min: 0,
  					    padding: {top:0, bottom:0}
  					}
  				    },
              tooltip: {
                format: {
                  title: function(){return "Classes";}
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
  					   'SED00206':'totalLoadedClasses',
  					   'SED00207':'loadedClasses',
  					   'SED00208':'unLoadedClasses'
  					};

            if(value == null || value==undefined){
              gloveChart.load({
    						columns: [[gloveMap[id], 0]]
    					});
            }else{
  					gloveChart.load({
  						columns: [[gloveMap[id], value]]
  					});
          }
  			}
  		};
});
/************** END CHART CLASSES **************/

/************** START CHART THREADS **************/
angular.module('gvconsole')
.factory('ChartServiceThreads', function() {

    function buildChart(chartID) {

      return c3.generate({
          bindto: chartID,
          data: {
                columns: [
            ['totalThreads', 350],
            ['daemonThreads', 350],
            ['peakThreads', 350],
                ],
                type: 'bar',
                colors: {
                daemonThreads: '#238650',
                peakThreads: '#961E24'
            },
            labels: true
          },

        axis: {
          y: {
              max: 400,
              min: 0,
              padding: {top:0, bottom:0}
          }
            },
            tooltip: {
              format: {
                title: function(){return "Threads";}
              }
            },
          bar: {
           width: 64
        }
      });
    };
    var gloveChart = buildChart("#glove-chart3");

    return {
      updateGloveChart: function(id, value) {

          var gloveMap = {
             'SED00209':'totalThreads',
             'SED00210':'daemonThreads',
             'SED00211':'peakThreads'
          };

          if(value == null || value==undefined){
            gloveChart.load({
              columns: [[gloveMap[id], 0]]
            });
          }else{
          gloveChart.load({
            columns: [[gloveMap[id], value]]
          });
        }
      }
    };
});
/************** END CHART THREADS **************/

/************** START CHART CPU **************/
angular.module('gvconsole')
.factory('ChartServiceCPU', function() {

  function buildChart(chartID) {

    return c3.generate({
      bindto: chartID,
      data: {
        columns: [
            ['cpuUse', 100]
        ],
        type: 'gauge',
        onclick: function (d, i) { console.log("onclick", d, i); },
        onmouseover: function (d, i) { console.log("onmouseover", d, i); },
        onmouseout: function (d, i) { console.log("onmouseout", d, i); }
    },
    color: {
        pattern: ['#60B044', '#F6C600', '#F97600', '#FF0000'], // the three color levels for the percentage values.
        threshold: {
//            unit: 'value', // percentage is default
//            max: 200, // 100 is default
            values: [30, 60, 90, 100]
        }
    },
    size: {
        height: 180
    }
});
  };
  var gloveChart = buildChart("#glove-chart4");

  return {
    updateGloveChart: function(id, value) {

        var gloveMap = {
					   'SED00212':'cpuUse'
        };

        if(value == null || value==undefined){
          gloveChart.load({
            columns: [[gloveMap[id], 0]]
          });
        }else{
        gloveChart.load({
          columns: [[gloveMap[id], value]]
        });
      }
    }
  };

});
/************** END CHART CPU **************/


angular.module('gvconsole')
.controller('MonitoringController', ['$location', 'MonitoringServices', '$scope', '$http', 'ChartServiceMemories', 'ChartServiceClasses', 'ChartServiceThreads', 'ChartServiceCPU' , '$interval',
 function($location, MonitoringServices, $scope, $http, chartServiceMemories, chartServiceClasses, chartServiceThreads, chartServiceCPU, $interval){

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

      chartServiceMemories.updateGloveChart('SED00201', $scope.maxMemory);
      chartServiceMemories.updateGloveChart('SED00202', $scope.totalMemory);
      chartServiceMemories.updateGloveChart('SED00203', $scope.freeMemory);
      chartServiceMemories.updateGloveChart('SED00204', $scope.heapMemory);

      chartServiceClasses.updateGloveChart('SED00206', $scope.totalLoadedClasses);
      chartServiceClasses.updateGloveChart('SED00207', $scope.loadedClasses);
      chartServiceClasses.updateGloveChart('SED00208', $scope.unLoadedClasses);

      chartServiceThreads.updateGloveChart('SED00209', $scope.totalThreads);
      chartServiceThreads.updateGloveChart('SED00210', $scope.daemonThreads);
      chartServiceThreads.updateGloveChart('SED00211', $scope.peakThreads);
};

    },1000);

    $interval(function(){

      if($location.path() == '/monitoring'){

      MonitoringServices.getCPU().then(
          function(response) {
             $scope.cpuUse = response.data.usage;

          }, function(response){
               $scope.error = response;
          });

          chartServiceCPU.updateGloveChart('SED00212',$scope.cpuUse);

        };

    },3000);

}]);
