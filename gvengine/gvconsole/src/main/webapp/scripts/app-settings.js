angular.module('gvconsole')
 .service('SettingsService', ['ENDPOINTS', '$http', function(Endpoints, $http){


 }]);

angular.module('gvconsole')
.controller('SettingsController',['SettingsService', '$scope', function(SettingsService, $scope){

 $scope.service = {};

}]);
