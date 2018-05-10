angular.module('gvconsole', ['ngCookies','ngRoute','angular-quartz-cron', 'ui.bootstrap','angularjs-gauge', 'angular-toArrayFilter'])
.constant('ENDPOINTS', getEndpoints())
.config(['$httpProvider', function($httpProvider){
	$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
}])
.config(['$routeProvider', function($routeProvider) {
	    $routeProvider.
	      when('/login', {
	        templateUrl: 'topics/auth/login.html'
	      }).
    		when('/deploy', {
    			templateUrl: 'topics/deploy/console.html'
    		}).
    		when('/deploy/:newConfigId', {
    			templateUrl: 'topics/deploy/deploy.html'
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
			when('/configuration', {
				templateUrl: 'topics/config/configuration.html'
			}).
			when('/testing', {
				templateUrl: 'topics/flow/test.html'
			}).
			when('/properties', {
				templateUrl: 'topics/properties/properties.html'
			}).
			when('/settings', {
				templateUrl: 'topics/settings/settings.html'
			}).
			when('/settings/:settingId', {
				templateUrl: 'topics/settings/gvPoolManager.html'
			}).
			when('/tools', {
				templateUrl: 'topics/tools/tools.html'
			}).
			otherwise({
    	        redirectTo: '/monitoring'
			});
}])
.service('GlobalService',['ENDPOINTS','$http', function(Endpoints, $http){
	this.getSystemProperty = function(key) {
		return $http.get(Endpoints.gvconfig + '/systemproperty/' + key);
	}
}])
.run(['$rootScope', 'GlobalService', '$location', '$cookieStore', '$http',
    function ($rootScope, GlobalService, $location, $cookieStore, $http) {

			(function ($) {
			    $.fn.floatLabels = function (options) {

			        // Settings
			        var self = this;
			        var settings = $.extend({}, options);


			        // Event Handlers
			        function registerEventHandlers() {
			            self.on('input keyup change', 'input, textarea', function () {
			                actions.swapLabels(this);
			            });
			        }


			        // Actions
			        var actions = {
			            initialize: function() {
			                self.each(function () {
			                    var $this = $(this);
			                    var $label = $this.children('label');
			                    var $field = $this.find('input,textarea').first();

			                    if ($this.children().first().is('label')) {
			                        $this.children().first().remove();
			                        $this.append($label);
			                    }

			                    var placeholderText = ($field.attr('placeholder') && $field.attr('placeholder') != $label.text()) ? $field.attr('placeholder') : $label.text();

			                    $label.data('placeholder-text', placeholderText);
			                    $label.data('original-text', $label.text());

			                    if ($field.val() == '') {
			                        $field.addClass('empty')
			                    }
			                });
			            },
			            swapLabels: function (field) {
			                var $field = $(field);
			                var $label = $(field).siblings('label').first();
			                var isEmpty = Boolean($field.val());

			                if (isEmpty) {
			                    $field.removeClass('empty');
			                    $label.text($label.data('original-text'));
			                }
			                else {
			                    $field.addClass('empty');
			                    $label.text($label.data('placeholder-text'));
			                }
			            }
			        }


			        // Initialization
			        function init() {
			            registerEventHandlers();

			            actions.initialize();
			            self.each(function () {
			                actions.swapLabels($(this).find('input,textarea').first());
			            });
			        }
			        init();


			        return this;
			    };

			    $(function () {
			        $('.float-label-control').floatLabels();
			    });
			})(jQuery);
      // keep user logged in after page refresh
	  $rootScope.globals = $cookieStore.get('globals') || {};

	  $rootScope.getInstanceName = function () {
		GlobalService.getSystemProperty('it.greenvulcano.instance.name').then(
			function(response){
				$rootScope.globals.instanceName = response.data;
			},
			function(response){
				switch (response.status) {
					case 404:
						$rootScope.globals.instanceName = "ESB v.4.0";
						break;

					case 403:
						$rootScope.globals.instanceName = "ESB v.4.0";
						break;
					}
		});
	}

      if ($rootScope.globals.currentUser) {
		  $rootScope.getInstanceName();
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

      $rootScope.getUser = function(username){
		  	$rootScope.user = username;
	  };

      $rootScope.routeIsIn = function(route){
                        return  $location.path().startsWith(route);
					  };
					  
}]);
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
