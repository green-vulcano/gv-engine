angular.module('gvconsole')
 .service('XMLService', ['ENDPOINTS', '$http', function(Endpoints, $http){


 }]);

angular.module('gvconsole')
.controller('XMLController',['XMLService','$rootScope', '$scope', '$location', function(XMLService,$rootScope, $scope, $location){

 $scope.service = {};

}]);
