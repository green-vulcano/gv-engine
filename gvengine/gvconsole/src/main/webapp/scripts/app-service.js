angular.module('gvconsole')
.controller('ServiceController', ['$scope', 'ConfigService', '$http', function($scope, ConfigService, $http){

    $scope.operations = [];
    $scope.service = {};

    ConfigService.getServices().then(
    function(response) {
      angular.forEach(response.data, function(service, sName) {
        angular.forEach(service.operations, function(operation, oName) {
          $scope.operations.push({ service: sName, operation: oName, key: sName+'/'+oName});
        });
      });
    });

    $scope.addParameter = function(){

  		if ($scope.service.properties == undefined) {
  			$scope.service.properties = {};
  		}

  		if ($scope.key && $scope.key.trim().length > 0) {
  			   $scope.service.properties[$scope.key]= $scope.value;
  			   $scope.key = '';
  			   $scope.value = '';
  		}
  	};

    $scope.removeParameter=function(key){
  		 delete $scope.service.properties[key];
  	};

    $scope.run = function(){

      $scope.call = "http://localhost:8181/cxf/gvesb/" + Object.values($scope.flow)[2];

      var request = {
        method: $scope.http_method,
        url : $scope.call,
        headers: {
          'Content-Type' : 'application/json'
        },
        data: $scope.service.object,
        params: $scope.service.properties,
        /*transformRequest: [function (data){
          return data;
        }], */
        transformResponse: [function (data) {
          return data;
        }]
      };

      $http(request).then(function(response){

      $scope.output = 'RESULT :\n\n' + response.data;

      },function(response){

        $scope.output = 'Error\n\n' + 'Status code: ' + response.status;

      });

    };


}]);
