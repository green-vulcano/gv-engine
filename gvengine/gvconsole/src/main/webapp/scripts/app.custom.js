angular.module('gvconsole', ['ngCookies','ngRoute','angular-quartz-cron', 'ui.bootstrap'])
.constant('ENDPOINTS', getEndpoints())
.config(['$httpProvider', function($httpProvider){
	$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
}])
.config(['$routeProvider', function($routeProvider) {
	    $routeProvider.
	      when('/login', {
	        templateUrl: 'topics/auth/login.html'
	        }).
    		when('/config', {
    			templateUrl: 'topics/config/console.html'
    		}).
    		when('/schedule', {
    			templateUrl: 'topics/schedule/list.html'
    		}).
    		when('/schedule/add', {
    			templateUrl: 'topics/schedule/form.html'
    		}).
    		when('/users', {
    			templateUrl: 'topics/users/list.html'
    		}).
            when('/users/:userId', {
          templateUrl: 'topics/users/form.html'
            }).
			when('/myprofile', {
          templateUrl: 'topics/profile/myprofile.html'
            }).
			when('/monitoring', {
				templateUrl: 'topics/monitoring/monitoring.html'
			}).
			when('/testing', {
				templateUrl: 'topics/flow/test.html'
			}).
            when('/properties', {
				templateUrl: 'topics/properties/properties.html'
			}).
			when('/properties/modify', {
				templateUrl: 'topics/properties/modify.html'
			}).
            otherwise({
	            redirectTo: '/myprofile'
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
function gototab(reload)
{
window.location.hash = '#/monitoring';
window.location.reload(true);
};

angular.element(document).ready(function() {
  angular.element('.navbar-toggle').click(function () {
      angular.element('.navbar-nav').toggleClass('slide-in');
      angular.element('.side-body').toggleClass('body-slide-in');
  });
});
function slideButtonMenu(){
	var width = angular.element(window).width();
	if(width <= 768){
      angular.element('.navbar-nav').toggleClass('slide-in');
      angular.element('.side-body').toggleClass('body-slide-in');
		};
};
