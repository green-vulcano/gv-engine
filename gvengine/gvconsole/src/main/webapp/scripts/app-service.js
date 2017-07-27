angular.module('gvconsole')
.controller('ServiceController', ['$rootScope', 'Base64','$scope', 'ConfigService', '$http', function($rootScope, Base64, $scope, ConfigService, $http){

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

      $http.defaults.headers.common['Authorization'] = undefined;

      if($scope.username != undefined && $scope.username != ""){

        var authdata = Base64.encode($scope.username + ':' + $scope.password);

        $http.defaults.headers.common['Authorization'] = "Basic " + authdata;
      };


      $scope.call = "http://localhost:8181/cxf/gvesb/" + $scope.flow.service + '/' + $scope.flow.operation;

      console.log('call: ' + $scope.call);

      var request = {
        method: $scope.http_method,
        url : $scope.call,
        headers: {
          'Content-Type' : 'application/json'
        },
        data: $scope.service.object,
        params: $scope.service.properties,
        transformResponse: [function (data) {
          return data;
        }]
      };

      $http(request).then(function(response){

      $scope.output = 'RESULT :\n\n' + response.data;

      },function(response){

        $scope.output = 'Error\n\n' + 'Status code: ' + response.status;

      });

      $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata;

    };

    $("textarea").keydown(function(e) {
        if(e.keyCode === 9) { // tab was pressed
            // get caret position/selection
            var start = this.selectionStart;
            var end = this.selectionEnd;

            var $this = $(this);
            var value = $this.val();

            // set textarea value to: text before caret + tab + text after caret
            $this.val(value.substring(0, start)
                        + "\t"
                        + value.substring(end));

            // put caret at right position again (add one for the tab)
            this.selectionStart = this.selectionEnd = start + 1;

            // prevent the focus lose
            e.preventDefault();
        }
    });


}]);


/*
angular.module('services', ['ngCookies','ngRoute','angular-quartz-cron', 'ui.bootstrap','ConfigService'])
.controller("ServiceController", ['$scope', 'ConfigService', '$http', function($scope, ConfigService, $http){

  $scope.operations = [];

  ConfigService.getServices().then(
  function(response) {
    angular.forEach(response.data, function(service, sName) {
      angular.forEach(service.operations, function(operation, oName) {
        $scope.operations.push({ service: sName, operation: oName, key: sName+'/'+oName});
      });
    });
  });

}]);
*/
