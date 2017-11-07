angular.module('gvconsole')
 .service('AdminService', ['ENDPOINTS', '$http', function(Endpoints, $http){

		this.getAllUsers = function(range,params,order){
			
			var querystring = "?";
			if (params.username && params.username.trim().length>0) querystring+="username="+params.username + "&";
	        if (params.fullname && params.fullname.trim().length>0) querystring+="fullname="+params.fullname + "&";
	        if (params.email && params.email.trim().length>0) querystring+="email="+params.email + "&";
	        if (params.role && params.role.trim().length>0) querystring+="role="+params.role + "&";
	        if (params.enabled != undefined) querystring+="enabled="+params.enabled + "&";
	        if (params.expired != undefined) querystring+="expired="+params.expired + "&";
	        
			querystring+="order="+order;
			
			return $http.get(Endpoints.gviam + '/admin/users' + querystring,{headers: {'Range':'users ' + range}});
		}

		this.getUser = function(id){
			return $http.get(Endpoints.gviam + '/admin/users/' + id);
		}

		this.createUser = function(user) {
			return $http.post(Endpoints.gviam + '/admin/users', user, {headers: {'Content-Type':'application/json'} });
		}

		this.updateUser = function(id, user) {
			return $http.put(Endpoints.gviam + '/admin/users/' + id, user, {headers: {'Content-Type':'application/json'} });
		}

		this.switchUserEnablement = function(id) {
			return $http.patch(Endpoints.gviam + '/admin/users/' + id + '/enabled');
		}

		this.resetUserPassword = function(id) {
			return $http.patch(Endpoints.gviam + '/admin/users/' + id + '/password');
		}

		this.deleteUser = function(id){
			 return $http.delete(Endpoints.gviam + '/admin/users/' + id);
		}

		this.getRoles = function() {
			 return $http.get(Endpoints.gviam + '/admin/roles')
	  }

 }]);

angular.module('gvconsole')
.controller('UsersListController',['AdminService','$rootScope', '$scope', '$location', function(AdminService,$rootScope, $scope, $location){

	$scope.searchClick = true;
	
	var instance = this;
  for( prop in $rootScope.globals.currentUser.roles){
    if($rootScope.globals.currentUser.isAdministrator){
      $scope.auth = true;
    }
  };

  $scope.viewby = 10;
  $scope.totalItems = 64;
  $scope.currentPage = 1;
  $scope.selected = true;

	this.alerts = [];

	this.list = [];
	
	$scope.order = "username";
	$scope.params = "";

  $scope.$watchGroup(["currentPage","viewby","order","reverse","params"], function(newValue) {
	  
	  if($scope.selected) {
	      angular.element('#search_param option:eq(0)').prop('selected', true);
	      angular.element('#selctNum option:eq(3)').prop('selected', true);
	      angular.element("#selctNum option[value='? number:10 ?']").remove();
	      angular.element("#search_param option[value='? undefined:undefined ?']").remove();
	      $scope.selected = false;
	    }

    $scope.min = (newValue[0] * newValue[1]) - newValue[1];
    $scope.max = (newValue[0] * newValue[1]);

    $scope.range = $scope.min + '-' + $scope.max;
    	
    	AdminService.getAllUsers($scope.range,$scope.params,$scope.order).then(
    
  				function(response){

            instance.alerts = [];
            instance.list = response.data;
            $scope.totalItems = (response.headers('Content-Range').split('/'))[1];
            },
  				function(response){
  					switch (response.status) {

  							case 401: case 403:
  								$location.path('login');
  								break;

  							default:
  								instance.alerts.push({type: 'danger', msg: 'Data not available'});
  								$scope.dataNa=true;
  								break;
  					}
  			$scope.loadStatus = "error";
  		});

    });

    $scope.maxSize = $scope.totalItems/3;
    
    $scope.enabled = undefined;
    $scope.expired = undefined;
    
    $scope.changeEnabledStatus = function(){
    	if ($scope.enabled == undefined) {
    		$scope.enabled = true;
        } else if ($scope.enabled) {
        	$scope.enabled = false;
        } else {
        	$scope.enabled = undefined;
        }
    	
    }
    
    $scope.changeExpiredStatus = function(){
    	
    	if ($scope.expired == undefined) {
    		$scope.expired = true;
        } else if ($scope.expired) {
        	$scope.expired = false;
        } else {
        	$scope.expired = undefined;
        }
    	
    }
    
   $scope.search_order = function(){
	   
	   $scope.params = 
   	{
   		username: $scope.username,
   		fullname: $scope.fullname,
   		email: $scope.email,
   		role: $scope.role,
   		enabled: $scope.enabled,
		expired: $scope.expired
   	};
	   
   }
    
   $scope.reverse = "username";
    

    $scope.orderFunction = function(by){
    	$scope.order = by;
    	
    	if($scope.order == $scope.reverse){
            $scope.order = $scope.order + ":reverse";
            $scope.reverse = $scope.order;
          }else{
          $scope.reverse = $scope.order;
        }

      };

}]);

angular.module('gvconsole')
.controller('UserFormController',['AdminService' , '$scope', '$routeParams', '$location', function(AdminService, $scope, $routeParams, $location){

		$scope.userDataStatus = "loading";
		this.alerts = [];

		this.user = {roles : {}};
		this.roles = [{name: "admin", description:'Created by GVConsole'},{name: "system", description:'Created by GVConsole'},{name: "gvadmin", description:'Created by GVConsole'}];

		var instance = this;

		AdminService.getRoles().then(function(response){
			instance.roles = response.data;
		});

		if ($routeParams.userId == 'new') {

			this.user.enabled = true;
			$scope.newUser = true;
			$scope.userDataStatus = "ready";
		} else {
			AdminService.getUser($routeParams.userId)
			.then(function(response) {
				 		instance.user = response.data;
				 		$scope.userDataStatus = "ready";
			 		},function(response){
			 			switch (response.status) {
			 				case 404:
			 					instance.alerts.push({type: 'danger', msg: 'User not found'});
			 					break;

			 				case 401: case 403:
			 					$location.path('login');
			 					break;

			 				default:
			 					instance.alerts.push({type: 'danger', msg: 'Data not available'});
			 					break;
			 			}

			 			$scope.userDataStatus = "error";
			 		});
		}



		this.addRole = function(){

			if (instance.newrole) {
				var role = instance.roles.find( function(r){ return r.name == instance.newrole });

				if (role) {
					instance.user.roles[role.name] = role;
				} else {
					instance.user.roles[instance.newrole] = {name: instance.newrole, description:'Created by GVConsole'};
				}
				delete instance.newrole;
			}
		}


		this.removeRole = function(key){
			delete instance.user.roles[key];
		}

		this.saveUser =  function(){
			$scope.userDataStatus = "saving";

			var save = ($scope.newUser) ? AdminService.createUser(instance.user) : AdminService.updateUser(instance.user.id, instance.user);

			save.then(function(response) {

				  instance.alerts.push({type: 'success', msg: 'User data saved'});
          setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
				  $scope.userDataStatus = "ready";
				  if($scope.newUser) {
					  $location.path('/users');
				  }
			  	},function(response){
				  instance.alerts.push({type: 'danger', msg: response.data.message || 'Operation failed'});
          setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
				  $scope.userDataStatus = "ready";

			  });
		}

		this.switchUserStatus =  function(){
			$scope.userDataStatus = "saving";
			AdminService.switchUserEnablement(instance.user.id)
					.then(function(response) {
				 		instance.user = response.data;
				 		instance.alerts.push({type: 'success', msg: 'User status switched'});
            setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
				 		$scope.userDataStatus = "ready";
				 		$location.path('/users');
			 		},function(response){
			 			switch (response.status) {
			 				case 404:
			 					instance.alerts.push({type: 'danger', msg: 'User not found'});
			 					break;

			 				case 401: case 403:
			 					$location.path('login');
			 					break;

			 				default:
			 					instance.alerts.push({type: 'danger', msg: response.data.message || 'Operation failed'});
                setTimeout(function(){ angular.element(".fadeout").fadeOut(); }, 3000);
			 					break;
			 			}

			 			$scope.userDataStatus = "error";
			 		});
		}

		this.resetUserPassword =  function() {
			$scope.userDataStatus = "saving";
			AdminService.resetUserPassword(instance.user.id)
					.then(function(response) {
				 		instance.user = response.data;
				 		instance.alerts.push({type: 'success', msg: 'User password has been resetted'});
				 		$scope.userDataStatus = "ready";
				 		$location.path('/users');
			 		},function(response){
			 			switch (response.status) {
			 				case 404:
			 					instance.alerts.push({type: 'danger', msg: 'User not found'});
			 					break;

			 				case 401: case 403:
			 					$location.path('login');
			 					break;

			 				default:
			 					instance.alerts.push({type: 'danger', msg: response.data.message || 'Operation failed'});
			 					break;
			 			}

			 			$scope.userDataStatus = "error";
			 		});
		}

		this.deleteUser =  function() {
			$scope.userDataStatus = "saving";
			AdminService.deleteUser(instance.user.id)
					.then(function(response) {
				 		instance.user = response.data;
				 		instance.alerts.push({type: 'success', msg: 'User deleted'});
				 		$scope.userDataStatus = "ready";
				 		$location.path('/users');
			 		},function(response){
			 			switch (response.status) {
			 				case 404:
			 					instance.alerts.push({type: 'danger', msg: 'User not found'});
			 					break;

			 				case 401: case 403:
			 					$location.path('login');
			 					break;

			 				default:
			 					instance.alerts.push({type: 'danger', msg: response.data.message || 'Operation failed'});
			 					break;
			 			}

			 			$scope.userDataStatus = "error";
			 		});

		}

}]);

function slide(){
      angular.element('.side-body').removeClass('body-slide-in');
};