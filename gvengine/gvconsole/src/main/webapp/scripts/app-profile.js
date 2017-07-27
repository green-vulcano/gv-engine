
angular.module('gvconsole')
.factory('profileService',['$rootScope','ENDPOINTS','$http','Base64','$cookieStore',
function ($rootScope,Endpoints,$http,Base64,$cookieStore) {

  var instance = this;
  var service = {};

  service.changePassword = function(username,oldPassword,newPassword,callback) {

    var authdata = Base64.encode(username + ':' + oldPassword);

    $http.defaults.headers.common['Authorization'] = 'Basic ' + authdata;
    $http.post(Endpoints.gviam +'/authenticate').then(function(response) {
           angular.merge($rootScope.globals.currentUser, response.data);
           $cookieStore.put('globals', $rootScope.globals);
           callback(response.status);
         },
        function(response) {
           delete $http.defaults.headers.common.Authorization;
           callback(response.status);

          });

    var request = {
        method: 'POST',
        url: Endpoints.gviam + '/password',
        params: {new_password: newPassword}
    };

    $http(request).then(function(response) {
      instance.list = response.data;
      callback(response.status);
    });

 };

return service;

}]);

angular.module('gvconsole')
.controller('profileController',['$scope','$rootScope','profileService',
  function ($scope, $rootScope, profileService) {


    var instance = this;
    this.alerts = [];


  $scope.changePassword = function(){
  profileService.changePassword($rootScope.globals.currentUser.username, $scope.oldPassword, $scope.newPassword,
  function(status){

  switch (status) {
        case 204:
            instance.alerts.length = 0;
            $scope.error = false;
            instance.alerts.push({type: 'success', msg: 'Change Password success!'});
            $scope.errorMessage = 'Password changed';
            setTimeout(function(){ $(".fadeout").fadeOut(); }, 0000);
            setTimeout(function(){ $(".fadeout2").fadeOut(); }, 3000);
            break;

        case 401:
            instance.alerts.length = 0;
            $scope.error = true;
            instance.alerts.push({type: 'danger', msg: 'Authentication failed, please check old password.'});
            setTimeout(function(){ $(".fadeout2").fadeOut(); }, 3000);
            break;
      };

});
};

}]);
