angular.module('gvconsole', ['RDash','gvconsole-auth','gvconsole-admin','gvconsole-scheduling','ngCookies','ngRoute'])
.config(['$routeProvider',
         function($routeProvider) {
	    $routeProvider.
	      when('/login', {
	        templateUrl: 'auth/login.html'
	      }).
		when('/flows', {
			templateUrl: 'flows/flowList.html'
		}).
		when('/flows/:userId', {
			templateUrl: 'flows/flowDetail.html'
		}).
		when('/scheduling', {
			templateUrl: 'scheduling/schedulingList.html'
		}).
		when('/scheduling/:userId', {
			templateUrl: 'scheduling/schedulingDetail.html'
		}).
		when('/users', {
			templateUrl: 'admin/userList.html'
		}).
	      when('/users/:userId', {
	        templateUrl: 'admin/userDetail.html'
	      }).
	      otherwise({
		        redirectTo: '/users'
		  });
}])
.run(['$rootScope', '$location', '$cookieStore', '$http',
    function ($rootScope, $location, $cookieStore, $http) {
      // keep user logged in after page refresh
      $rootScope.globals = $cookieStore.get('globals') || {};
      if ($rootScope.globals.currentUser) {
          $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata;
      }

      $rootScope.$on('$locationChangeStart', function (event, next, current) {
          // redirect to login page if not logged in
          if ($location.path() !== '/login' && !$rootScope.globals.currentUser) {
              $location.path('/login');
          }
      });

      $rootScope.go = function(path) {
		                    $location.path(path);
	                    };

      $rootScope.routeIsIn = function(route){
                        return  $location.path().startsWith(route);
                      };
}]);
