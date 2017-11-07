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
 .service('ConfigService', ['ENDPOINTS', '$http', function(Endpoints, $http){

		this.getServices = function(){
			return $http.get(Endpoints.gvesb);
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

	    this.addConfig = function(id,config){
	    	var fd = new FormData();
	        fd.append('gvconfiguration', config);

	        return $http.post(Endpoints.gvconfig+'/configuration/'+id, fd, {
	            transformRequest: angular.identity,
	            headers: {'Content-Type': 'multipart/form-data'}
	        });
	    }

	    this.getXMLFiles = function(){
	    	return $http.get(Endpoints.gvconfig + '/deploy/xml');
	    }

	    this.getXMLFile = function(name){
	    	return $http.get(Endpoints.gvconfig + '/deploy/xml/' + name);
	    }


 }]);

angular.module('gvconsole')
.controller('ConfigController',['ConfigService' ,'$scope', '$rootScope', '$location', '$routeParams', function(ConfigService, $scope, $rootScope, $location, $routeParams){

  if($rootScope.globals.currentUser.isAdministrator || $rootScope.globals.currentUser.isConfigManager){
    angular.element("myFieldset").disabled = false;
  }

  	$scope.newDeploy = null;

	var instance = this;

	this.alerts = [];

	this.configInfo = {};

	this.deploy = {};

	var instance = this;
	this.history = [];

  this.loadList = function() {
    ConfigService.getConfigHistory().then(function(response){
  		instance.history = response.data;
  		},function(response){
  			instance.alerts.push({type: 'danger', msg: 'Config history not available'});
  		});
  };

	ConfigService.getConfigHistory().then(function(response){
		instance.history = response.data;
		},function(response){
		   instance.alerts.push({type: 'danger', msg: 'Config history not available'});
		});

	this.addConfig = function(){
		ConfigService.addConfig(instance.deploy.id,instance.deploy.configfile)
			.then(function(response){
				instance.alerts.push({type: 'success', msg: 'Configuration added successfully'});
				setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
        angular.element(".fadeout2").modal('hide');
        instance.loadList();
			},function(response){
				instance.alerts.push({type: 'danger', msg: 'Configuration added failed'});
        setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
       angular.element(".fadeout2").modal('hide');
			});

	}

	this.loadConfigInfo = function() {

		ConfigService.getConfigInfo().then(
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

	this.exportConfig = function () {
		$scope.exportInProgress = true;
		ConfigService.exportConfig()
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

	this.deleteConfig = function(id){
		ConfigService.deleteConfig(id)
			.then(function(response){
				instance.alerts.push({type: 'success', msg: 'Configuration deleted successfully'});
				setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
        instance.loadList();
			},function(response){
				instance.alerts.push({type: 'danger', msg: 'Configuration deleted failed'});
        setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			})

	};

	ConfigService.getXMLFiles().then(function(response){
		$scope.filesName = response.data;
		angular.forEach($scope.filesName,function(value,key){
			ConfigService.getXMLFile(value).then(function(response){
				LoadXMLString(value, response.data);
			});
		});
	},function(response){
		instance.alerts.push({type: 'danger', msg: 'XML File not available'});
	});



}]);


angular.module('gvconsole')
.controller('ConfigDeployController',['ConfigService','$routeParams','$scope',function(ConfigService, $routeParams, $scope){

	$scope.newConfigId = $routeParams.newConfigId;

	this.alerts = [];

	var instance = this;

	ConfigService.getConfigInfo()
		.then(function(response){
			$scope.currentConfigId = response.data.id;
			},function(response){
				instance.alerts.push({type: 'danger', msg: 'Configuration Info not available'});
	});

	ConfigService.getXMLFile("GVCore.xml").then(function(response){
		$scope.currentGVCore = response.data;
	},function(response){
		instance.alerts.push({type: 'danger', msg: 'XML File not available'});
	});

	ConfigService.getGVCore($scope.newConfigId)
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

	 ConfigService.getGVCoreProperties($scope.newConfigId).then(function(response){
			instance.propertiesKeys = response.data;
			ConfigService.getProperties().then(function(response){
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
		ConfigService.deploy($scope.newConfigId).then(function(response){
			ConfigService.setProperties(instance.properties).then(function(response){
			},function(response){
				instance.alerts.push({type: 'danger', msg: 'Properties could not be saved'});
			});

		},function(response){
			instance.alerts.push({type: 'danger', msg: 'Configuration deployments could not be made'});
		})

		ConfigService.getConfigHistory().then(function(response){
			instance.history = response.data;
			},function(response){
			     instance.alerts.push({type: 'danger', msg: 'Config history not available'});
			});

	};

}]);