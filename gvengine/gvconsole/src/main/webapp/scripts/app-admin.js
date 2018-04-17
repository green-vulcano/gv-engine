angular.module('gvconsole')
 .service('AdminService', ['ENDPOINTS', '$http', '$rootScope', function(Endpoints, $http, $rootScope){

		this.getAllUsers = function(range,params,order){

			if($rootScope.querystring == undefined && $rootScope.savedQuerystring != undefined){

				var querystring = $rootScope.savedQuerystring;
				range = $rootScope.savedQuery.range;
			}else{

			$rootScope.querystring = "?";
			if (params.username && params.username.trim().length>0) $rootScope.querystring+="username="+params.username + "&";
	        if (params.fullname && params.fullname.trim().length>0) $rootScope.querystring+="fullname="+params.fullname + "&";
	        if (params.email && params.email.trim().length>0) $rootScope.querystring+="email="+params.email + "&";
	        if (params.role && params.role.trim().length>0) $rootScope.querystring+="role="+params.role + "&";
	        if (params.enabled != undefined) $rootScope.querystring+="enabled="+params.enabled + "&";
	        if (params.expired != undefined) $rootScope.querystring+="expired="+params.expired + "&";

	        $rootScope.querystring+="order="+order;

	        var querystring = $rootScope.querystring

			}
			$rootScope.querystring = querystring;


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

    this.getConfigInfo = function() {
  		return $http.get(Endpoints.gvconfig + '/deploy')
  	}

 }]);

angular.module('gvconsole')
.controller('UsersListController',['AdminService','$rootScope', '$scope', '$location', '$routeParams', function(AdminService,$rootScope, $scope, $location, $routeParams){

  $scope.searchClick = true;
  var instance = this;
  this.configInfo = {};

  this.loadConfigInfo = function() {

		AdminService.getConfigInfo().then(
				function(response){
					instance.configInfo = response.data;
				},
				function(response){
					switch (response.status) {

							case 401: case 403:
								$location.path('login');
								break;

							default:
								instance.alerts.push({type: 'danger', msg: 'Data not available'});
								break;
					}
		});

	}




  for( prop in $rootScope.globals.currentUser.roles){
    if($rootScope.globals.currentUser.isAdministrator){
      $scope.auth = true;
    }
  };

  if($scope.viewby == undefined && $rootScope.savedQuery ==undefined){

	  $scope.viewby = 10;
	  $scope.selected = true;

  }else{
	  $scope.viewby = $rootScope.savedQuery.view;
  }

  $scope.totalItems = 64;

  if($scope.currentPage == undefined && $rootScope.savedQuery == undefined){

  $scope.currentPage = 1;

  }else{
	  $scope.currentPage = $rootScope.savedQuery.page;
  }

  if($scope.params == undefined && $rootScope.savedQuery == undefined){
	  $scope.params = "";
  }else{
	  $scope.params = $rootScope.savedQuery.params;
  }

	this.alerts = [];

	this.list = [];

	if($scope.order == undefined && $rootScope.savedQuery == undefined){
	$scope.order = "username";
	}else{
		$scope.order = $rootScope.savedQuery.order;
	}



  $scope.$watchGroup(["currentPage","viewby","order","reverse","params"], function(newValue) {

	  if($scope.selected) {
	      angular.element('#search_param option:eq(0)').prop('selected', true);
	      angular.element("#selctNum option[value='? number:10 ?']").remove();
	      angular.element('#selctNum option:eq(2)').prop('selected', true);
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

   $scope.searchFor = "username";
   $scope.search = "";

   $scope.search_order = function(search, searchFor){


	   $scope.currentPage = 1;

     if(searchFor == "username") {
       $scope.params = { username: search + "*" };
     } else {
       if (searchFor == "fullname") {
         $scope.params = {fullname: search + "*"};
       } else {
         if (searchFor == "email") {
           $scope.params = {email: search + "*"};
         }
       }
     }
	   /* $scope.params =
   	{
   		username: $scope.username,
   		fullname: $scope.fullname,
   		email: $scope.email,
   		role: $scope.role,
   		enabled: $scope.enabled,
		expired: $scope.expired
  };*/

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

      $scope.saveQuery = function(){
    	  $rootScope.savedQuerystring = $rootScope.querystring;
    	  $rootScope.querystring = undefined;

    	  $rootScope.savedQuery = {
    			  view:$scope.viewby,
    			  page:$scope.currentPage,
    			  order:$scope.order,
    			  params:$scope.params,
    			  range:$scope.range
    	  };
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
            console.log(instance.newDescription);
            if (instance.newDescription == undefined) {
              instance.user.roles[instance.newrole] = {name: instance.newrole, description:'Created by GVConsole'};
            } else {
					         instance.user.roles[instance.newrole] = {name: instance.newrole, description:instance.newDescription};
                 }
				}
				delete instance.newrole;
        delete instance.newDescription;
			}
		}

    this.addExistRole = function(r){
        console.log(r.name);
        instance.user.roles[r.name] = r;
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
