angular.module('gvconsole')
.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

angular.module('gvconsole')
 .service('DeployService', ['ENDPOINTS', '$http', function(Endpoints, $http){

		this.getCurrentServices = function(){
			return $http.get(Endpoints.gvesb);
		}

		this.getCurrentFlows = function(service,operation){
			return $http.get(Endpoints.gvesb + '/' + service + '/' + operation + '/flows');
		}

		this.getNewServices = function(id){
			return $http.get(Endpoints.gvconfig + '/configuration/' + id);
		}

	    this.reloadConfiguration = function(){
	      	return $http.put(Endpoints.gvconfig + '/deploy');
	    }

		this.getConfigInfo = function() {
			return $http.get(Endpoints.gvconfig + '/deploy')
		}

		this.deploy = function(id){
			return $http.post(Endpoints.gvconfig + '/deploy/' + id);
		}

		this.exportConfig = function() {
			return $http({
				method: 'GET',
			    url: Endpoints.gvconfig + '/deploy/export',
		        responseType: 'arraybuffer'
			});
		}

		this.exportHistoryConfig = function(id) {
			return $http({
				method: 'GET',
			    url: Endpoints.gvconfig + '/deploy/export/' + id,
		        responseType: 'arraybuffer'
			}, id);
		}

		this.getConfigHistory = function(){
	    	return $http.get(Endpoints.gvconfig + '/configuration');
	    }

	    this.getGVCore = function(id){
	    	return $http.get(Endpoints.gvconfig + '/configuration/' + id + '/GVCore.xml');
	    }

	    this.getGVCoreProperties = function(id){
	    	return $http.get(Endpoints.gvconfig + '/configuration/' + id + '/properties');
		}

	    this.getProperties = function(){
	    	return $http.get(Endpoints.gvconfig + '/property');
	    }

	    this.getPropertyValue = function(key){
	    	return $http.get(Endpoints.gvconfig + '/property/' + key);
	    }

	    this.setProperties = function(properties){
	    	return $http.put(Endpoints.gvconfig + '/property',properties,{headers:{'Content-Type':'application/json'}});
	    }

	    this.deleteConfig = function(id){
	    	return $http.delete(Endpoints.gvconfig + '/configuration/' + id);
	    }

	    this.addConfig = function(id,desc,config){
	    	var fd = new FormData();
	        fd.append('gvconfiguration', config);

	        return $http.post(Endpoints.gvconfig+'/configuration/'+id+"/"+desc, fd, {
	            transformRequest: angular.identity,
	            headers: {'Content-Type': 'multipart/form-data'}
	        });
	    }

	    this.getConfigurationFileList = function(){
	    	return $http.get(Endpoints.gvconfig + '/deploy/xml');
	    }

	    this.getConfigurationFile = function(name){
	    	return $http.get(Endpoints.gvconfig + '/deploy/xml/' + name);
	    }


 }]);

angular.module('gvconsole')
.controller('DeployController',['DeployService' ,'$scope', '$rootScope', '$location', '$routeParams','$anchorScroll', function(DeployService, $scope, $rootScope, $location, $routeParams, $anchorScroll){

  if($rootScope.globals.currentUser.isAdministrator || $rootScope.globals.currentUser.isConfigManager){
    angular.element("myFieldset").disabled = false;
  }

  	$scope.newDeploy = null;

	var instance = this;

	this.alerts = [];

  this.alertsAdd = [];

	this.configInfo = {};

	this.deploy = {};

	this.history = [];

	$scope.gotoTop = function() {
		$location.hash('TOP');
		$anchorScroll();
	  };

	  

    this.loadList = function() {
	  DeployService.getConfigHistory().then(function(response){
  		instance.history = response.data;
  		},function(response){
  			instance.alerts.push({type: 'danger', msg: 'Config history not available'});
  		});
    };


	this.addConfig = function(){
    if (instance.configInfo.id != instance.deploy.id) {
    	if (!instance.deploy.desc){
			instance.deploy.desc = "No description";
		}
    	DeployService.addConfig(instance.deploy.id,instance.deploy.desc,instance.deploy.configfile)
			.then(function(response){
				instance.alerts.push({type: 'success', msg: 'Configuration added successfully'});
				setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
		        angular.element(".fadeout2").modal('hide');
		        instance.loadList();
			},function(response){
				console.log(response);
				instance.alerts.push({type: 'danger', msg: 'Configuration upload failed'});
		        setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
		        angular.element(".fadeout2").modal('hide');
			});
    } else {
      instance.alertsAdd.push({type: 'danger', msg: 'ID already used for the current configuration'});
      setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
    }

	}

	this.loadConfigInfo = function() {

		DeployService.getConfigInfo().then(
				function(response){
					instance.configInfo = response.data;
					instance.getFiles();
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

  $scope.historyOrderFunction = function(propertyName) {
    $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
    $scope.propertyName = propertyName;
  };

	this.exportConfig = function () {
		$scope.exportInProgress = true;
		DeployService.exportConfig()
			.then( function(response) {

				var linkElement = document.createElement('a');

				try {

					var blob = new Blob([response.data], { type: 'application/zip' });
					var url = window.URL.createObjectURL(blob);
					linkElement.setAttribute('href', url);
	                linkElement.setAttribute("download", instance.configInfo.id+'.zip');

	                var clickEvent = new MouseEvent("click", {
	                	"view": window,
	                	"bubbles": true,
	                	"cancelable": false
	                });
	                linkElement.dispatchEvent(clickEvent);

				} catch (ex) {
					instance.alerts.push({type: 'danger', msg: 'Configuration export failed'});
					setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
					console.log(ex);

				}
				$scope.exportInProgress = false;

			}, function (responses) {
				$scope.exportInProgress = false;
				instance.alerts.push({type: 'danger', msg: 'Configuration export failed'});
				setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			});
		}

	this.exportHistoryConfig = function (id) {
		$scope.exportInProgress = true;
		DeployService.exportHistoryConfig(id)
			.then( function(response) {

				var linkElement = document.createElement('a');

				try {

					var blob = new Blob([response.data], { type: 'application/zip' });
					var url = window.URL.createObjectURL(blob);
					linkElement.setAttribute('href', url);
	                linkElement.setAttribute("download", id+'.zip');

	                var clickEvent = new MouseEvent("click", {
	                	"view": window,
	                	"bubbles": true,
	                	"cancelable": false
	                });
	                linkElement.dispatchEvent(clickEvent);

				} catch (ex) {
					instance.alerts.push({type: 'danger', msg: 'Configuration export failed'});
					setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
					console.log(ex);

				}
				$scope.exportInProgress = false;

			}, function (responses) {
				$scope.exportInProgress = false;
				instance.alerts.push({type: 'danger', msg: 'Configuration export failed'});
				setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			});
		}

  this.reloadConfig = function() {
			DeployService.reloadConfiguration().then(
				function(response){
					instance.loadConfigInfo();
					instance.loadList();
					instance.alerts.push({type: 'success', msg: 'Reload configuration success'});
					setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
				},
				function(response){
					instance.alerts.push({type: 'danger', msg: 'Reload configuration failed'});
					setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
				}	
			)}

	this.deleteConfig = function(id){
		DeployService.deleteConfig(id)
			.then(function(response){
				instance.alerts.push({type: 'success', msg: 'Configuration deleted successfully'});
				setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
        instance.loadList();
			},function(response){
				instance.alerts.push({type: 'danger', msg: 'Configuration deleted failed'});
        setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			})

	};

  this.getFiles = function() {
	  DeployService.getConfigurationFileList().then(function(response){
		$scope.filesName = response.data;
		console.log(response);
		angular.forEach($scope.filesName,function(value,key){
			DeployService.getConfigurationFile(value).then(function(response){
				LoadXMLString(value, response.data);
			},function(response){
        instance.alerts.push({type: 'danger', msg: 'XML files not available'});
      });
		}); 
	},function(response){
		instance.alerts.push({type: 'danger', msg: 'XML fileList not available'});
	});
  };



}]);


angular.module('gvconsole')
.controller('DeployConfigController',['DeployService','$routeParams','$rootScope', '$scope','$location', function(DeployService, $routeParams, $rootScope, $scope, $location){

	$scope.newConfigId = $routeParams.newConfigId;

	this.alerts = [];
    this.alertsAdd = [];

    $scope.servicesCurrent = [];
    $scope.flows = {};
    $scope.result = {};

    DeployService.getCurrentServices().then(function(response){
    	angular.forEach(response.data,function(value){
	    	$scope.servicesCurrent.push(value);
    	});
    },function(response){
    	console.log("error: " + response.data);
    });

    $scope.servicesNew = [];

    /*DeployService.getNewServices($scope.newConfigId).then(function(response){
    	console.log("new: " + response.data);
  		angular.forEach(response.data,function(value){
  			if(!$scope.groupsNew.includes(value.groupName)){
  				$scope.groupsNew.push(value.groupName);

  			}
  			if(!$scope.serviceByGroupNew.hasOwnProperty(value.groupName)){
  				$scope.serviceByGroupNew[value.groupName] = [];
  			}

  			$scope.serviceByGroupNew[value.groupName].push(value);
  		})
  	},function(response){
  		console.log("error: " + response.data);
  	});*/

    DeployService.getNewServices($scope.newConfigId).then(function(response){
    	angular.forEach(response.data,function(value){
    		$scope.servicesNew.push(value);
    	});
    },function(response){
    	console.log("error: " + response.data);
    });




	var instance = this;

	DeployService.getConfigInfo()
		.then(function(response){
			$scope.currentConfigId = response.data.id;
			},function(response){
				instance.alerts.push({type: 'danger', msg: 'Configuration Info not available'});
	});

	DeployService.getConfigurationFile("GVCore.xml").then(function(response){
		$scope.currentGVCore = response.data;
	},function(response){
		instance.alerts.push({type: 'danger', msg: 'XML File not available'});
	});

	DeployService.getGVCore($scope.newConfigId)
		.then(function(response){
			$scope.newGVCore = response.data;
		},function(response){
			instance.alerts.push({type: 'danger', msg: 'GV Core not available'});
	});

	$scope.step = 0;

	  $scope.changeStep = function()  {
	    if ($scope.step != 0111 && $scope.step !=011 && $scope.step !=01) {
	      $scope.step = 0
	    }
	    $scope.step = $scope.step + angular.element("#stepValue").val();
	  }

	  $scope.backStep = function()  {
	    if ($scope.step == 01) {
	      $scope.step = 0;
	    } else {$scope.step = 01;}
	  }

	  this.properties = {};
	  this.propertiesKeys = [];

	  DeployService.getGVCoreProperties($scope.newConfigId).then(function(response){
			instance.propertiesKeys = response.data;
			DeployService.getProperties().then(function(response){
				angular.forEach(instance.propertiesKeys,function(value){
					if(response.data[value]){
						instance.properties[value] = response.data[value];
					}else{
						instance.properties[value] = null;
					}
				})
			})
	 });

	this.deploy = function(){
		DeployService.deploy($scope.newConfigId).then(function(response){
			DeployService.setProperties(instance.properties).then(function(response){
			},function(response){
				instance.alerts.push({type: 'danger', msg: 'Properties could not be saved'});
			});
			$rootScope.go('/deploy');
			instance.alerts.push({type: 'success', msg: 'Deployment success'});
		},function(response){
			$rootScope.go('/deploy');
			instance.alerts.push({type: 'danger', msg: 'Configuration deployments could not be made'});
		})

	};

}]);
