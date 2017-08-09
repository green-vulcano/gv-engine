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

    this.getConfigFiles = function(){
      return $http.get(Endpoints.gvconfig + '/configuration');
    }

    this.getConfigFile = function(fileName){
      return $http.get(Endpoints.gvconfig + '/configuration/' + fileName);
    }

 }]);

angular.module('gvconsole')
.controller('ConfigController',['ConfigService' ,'$scope', '$rootScope', '$location', function(ConfigService, $scope, $rootScope, $location){

  if($rootScope.globals.currentUser.isAdministrator || $rootScope.globals.currentUser.isConfigManager){
    document.getElementById("myFieldset").disabled = false;
  }

	var instance = this;

	this.alerts = [];

	this.services = {};

	this.configInfo = {};

	this.deploy = {};

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
 //XML Viewer
  ConfigService.getConfigFiles().then(
    function(response){
      $scope.filesName = response.data;
      angular.forEach($scope.filesName,function(value,key){
        ConfigService.getConfigFile(value).then(
          function(response){
            LoadXMLString(value, response.data);
          });
      })
  });


}]);
