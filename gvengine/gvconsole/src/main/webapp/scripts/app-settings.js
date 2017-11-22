angular.module('gvconsole')
 .service('SettingsService', ['ENDPOINTS', '$http', function(Endpoints, $http){


 }]);

angular.module('gvconsole')
.controller('SettingsController',['SettingsService', '$scope', function(SettingsService, $scope){

 $scope.service = {};
 
 /* Mettere i seguenti controlli:
  * 
  * GreenVulcanoPool[(@maximum-size > 0 ) and 
  * (@initial-size > @maximum-size)]}} initial-size > maximum-size
  * 
  * 
  * GreenVulcanoPool[(@maximum-creation > 0) and 
  * (@maximum-size > @maximum-creation)]}} maximum-size > maximum-creation
  * 
  * 
  * ExtendedData[count(//ExtendedData[@system=current()/@system and 
  * @service=current()/@service]) > 1]}} Attenzione: coppia sistema-servizio duplicata
  * 
  * 
  * 
  * */
 

}]);
