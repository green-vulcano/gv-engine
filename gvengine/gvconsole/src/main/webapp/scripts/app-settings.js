angular.module('gvconsole')
 .service('SettingsService', ['ENDPOINTS', '$http', function(Endpoints, $http){

       this.getAll = function() {
         return $http.get(Endpoints.gvconfig + '/settings');
       }

       this.get = function(name) {
         return $http.get(Endpoints.gvconfig + '/settings/'+name);
       }
 }]);

angular.module('gvconsole')
.controller('SettingsController',['SettingsService', '$scope', function(SettingsService, $scope){

  $scope.alerts = [];

  SettingsService.getAll().then(
        function(response) {
          $scope.settings = response.data;
          $scope.alerts = [];
        },
        function(response){
          switch (response.status) {

              case 401: case 403:
                $location.path('login');
                break;

              default:
                $scope.alerts.push({type: 'danger', msg: 'Data not available'});
                break;
              }
          });


	/*
	 * Scegliere come prendere i valori:
	 * 1) Prenderli tramite i forms(non tutti) e i rimanenti tramite angular.
	 * 2) Prenderli tutti tramite angular .
	 * 3) Modificare la sezione e far un form unico per nodo.
	 */
 $scope.sendCryptoHelper = function(){

	 $scope.cryptoHelper = {


		 type: angular.element("#CryptoHelperType").val(),
		 name: angular.element("#CryptoHelperName").val(),
		 description: angular.element("#CryptoHelperDescription").val(),
		 keyStoreFolder: angular.element("#CryptoHelperKeyStoreFolder").val(),
		 KeyStoreID: {

			 id: angular.element("#KeyStoreIDID").val()

		 }
	 };
 }

 $scope.addKeyStoreID = function(){
	 cryptoHelper.KeyStoreID.push({id: angular.element("#KeyStoreIDID").val()});
 }


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
