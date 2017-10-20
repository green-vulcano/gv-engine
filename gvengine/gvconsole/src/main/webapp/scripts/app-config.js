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


		/*this.getServices = function(){
			 return $http.get(Endpoints.gvesb);
		} inutile? */

		this.getConfigInfo = function() {
			return $http.get(Endpoints.gvconfig+'/deploy')
		}

		this.deploy = function(config, id){
	        var fd = new FormData();
	        fd.append('gvconfiguration', config);

	        return $http.post(Endpoints.gvconfig+'/deploy/'+id, fd, {
	            transformRequest: angular.identity,
	            headers: {'Content-Type': 'multipart/form-data'}
	        });
	    }

		this.getConfig = function(id) {
			return $http({
				method: 'GET',
			    url: Endpoints.gvconfig + '/deploy/' + id,
		        responseType: 'arraybuffer'
			});
		}

	    this.getConfigHistory = function(){
	      return $http.get(Endpoints.gvconfig + '/configuration');
	    }
	    
	    this.getConfigFiles = function(){
	      return $http.get(Endpoints.gvconfig + '/configuration');
	     }
	    
	    this.getConfigFile = function(fileName){
		  return $http.get(Endpoints.gvconfig + '/configuration/' + fileName);
		 }
	    
	    this.addConfigFile = function(fileName){
	      return $http.post(Endpoints.gvconfig + '/configuration/' + fileName,{headers: {'Content-Type':'application/json'} });
	    }
	
	    //nome da cambiare!
	    this.getGVCore = function(id){
	      return $http.get(Endpoints.gvconfig + '/configuration/' + id + 'GVCore.xml');	
	    }
	    
	    /*
	     api properties 
	     */

 }]);

angular.module('gvconsole')
.controller('ConfigController',['ConfigService' ,'$scope', '$rootScope', '$location', '$routeParams', function(ConfigService, $scope, $rootScope, $location, $routeParams){

  if($rootScope.globals.currentUser.isAdministrator || $rootScope.globals.currentUser.isConfigManager){
    angular.element("myFieldset").disabled = false;
  }

  	$scope.newDeploy = null;
  
	var instance = this;

	this.alerts = [];

	//this.services = {}; inutile?

	this.configInfo = {};

	this.deploy = {};
	
	$scope.configHistory = [{"id":"id1", "time":"time1"},{"id":"id2","time":"time2"}]; // questo poi toglierlo e richiamare l'api
	
	/*$scope.setNewConfig = function(newId){
		$scope.newConfig = newId;
		console.log("instance.configInfo.id: " + instance.configInfo.id);
		console.log("$scope.newConfig " + $scope.newConfig);
	}*/
	
	/*
	 ConfigService.getConfigHistory(response).then(function(){
	 $scope.configHistory = response.data;
	 },function(response){
	 console.log("error: " + error);
	 };
	
	
	*/
	
	this.addConfiguration = function(){
		
		ConfigService.addConfigFile(instance.deploy.id);
		//aggiungere avviso "alert" per "aggiunta avvenuta con successo" tramite il response?
		
	}	

	this.deployConfiguration = function (){

		$scope.deployInProgress = true;

		ConfigService.deploy(instance.deploy.configfile, instance.deploy.id)
		.then(function(response) {
			$scope.deployInProgress = false;
			instance.deploy = {};
			instance.loadConfigInfo();
			instance.alerts.push({type: 'success', msg: 'Configuration deployed successfully'});
      setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
		}, function(response){
			switch (response.status) {

			case 401: case 403:
				$location.path('login');
				break;

			default:
				$scope.deployInProgress = false;
				instance.alerts.push({type: 'danger', msg: 'Configuration deploy failed'});
        setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
				break;
			}

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

	this.exportConfig = function (name) {
		$scope.exportInProgress = true;
		ConfigService.getConfig(instance.configInfo.id)
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
					console.log(ex);

				}
				$scope.exportInProgress = false;

			}, function (responses) {
				$scope.exportInProgress = false;
				instance.alerts.push({type: 'danger', msg: 'Configuration export failed'});
			});

	}
 //XML Viewer modifitcato il nome dell'api, aggiustarlo
 /* ConfigService.getConfigFiles().then(
    function(response){
      $scope.filesName = response.data;
      angular.forEach($scope.filesName,function(value,key){
        ConfigService.getConfigFile(value).then(
          function(response){
            LoadXMLString(value, response.data);
          });
      })
  });*/

	$scope.setId = function(newConfigId, actualConfigId){
		console.log('prova');
		$scope.newConfigId = newConfigId;
		$scope.actualConfigId = actualConfigId;
	};

}]);

angular.module('gvconsole')
.controller('ConfigDeployController',['ConfigService','$routeParams','$scope',function(ConfigService, $routeParams, $scope){
	
	$scope.newConfigId = $routeParams.newConfigId;
	
	ConfigService.getConfigInfo()
		.then(function(response){
			$scope.currentConfigId = response.data.id;		
		},function(response){
			console.log("error: " + reponse.data);
		});
	
	
	
	//
	
	ConfigService.getConfigFile('GVCore.xml').then(
       function(response){
        $scope.currentGVCore = response.data;
     },function(response){
    	 console.log("error: " + response.data);
     });
	
	
	//
	
	
	
	
	
	/*ConfigService.getGVCore($scope.newConfigId)
		.then(function(response){
			$scope.newGVCore = response.data;
		},function(response){
			//gestione errore
		});
	
	ConfigService.getGVCore($scope.currentConfigId)
		.then(function(response){
			$scope.currentGVCore = response.data;
		},function(response){
			//gestione errore
		});	*/
	
	$scope.step = 0;

	  $scope.changeStep = function()  {
	    if ($scope.step != 0111 && $scope.step !=011 && $scope.step !=01) {
	      $scope.step = 0
	    }
	    $scope.step = $scope.step + angular.element("#stepValue").val();
	    console.log($scope.step);
	  }

	  $scope.backStep = function()  {
	    if ($scope.step == 01) {
	      $scope.step = 0;
	    } else {$scope.step = 01;}
	  }
	
	
}]);



angular.element(document).ready(function(){
     angular.element(window).scroll(function () {
            if (angular.element(this).scrollTop() > 50) {
                angular.element('#back-to-top').fadeIn();
            } else {
                angular.element('#back-to-top').fadeOut();
            }
        });


});

