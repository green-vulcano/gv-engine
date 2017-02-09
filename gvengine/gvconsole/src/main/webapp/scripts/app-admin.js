angular.module('gvconsole')
 .service('AdminService', ['ENDPOINTS', '$http', function(Endpoints, $http){
	 	
		this.getAllUsers = function(){
			 return $http.get(Endpoints.gviam + '/admin/users');
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
.controller('UsersListController',['AdminService' ,'$scope', '$location', function(AdminService, $scope, $location){

	var instance = this;

	this.alerts = [];

	this.list = [];
	this.currentPage = 1;

	AdminService.getAllUsers().then(
				function(response){
					instance.alerts = [];
					instance.list = response.data;
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
			$scope.loadStatus = "error";
		});

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
				var newrole = {name: instance.newrole, description:'Created by GVConsole'};

				instance.user.roles[instance.newrole] = {name: instance.newrole, description:'Created by GVConsole'};

				delete instance.newrole;
			}
		}

		this.removeRole = function(key){
			delete instance.user.roles[key];
		}

		this.saveUser =  function(){
			$scope.userDataStatus = "saving";

			var save = ($scope.newUser) ? AdminService.createUser(instance.user) : AdminService.updateUser(instance.user.username, instance.user);

			save.then(function(response) {

				  instance.alerts.push({type: 'success', msg: 'User data saved'});
				  $scope.userDataStatus = "ready";
				  if($scope.newUser) {
					  $location.path('/users');
				  }
			  	},function(response){
				  instance.alerts.push({type: 'danger', msg: response.data.message || 'Operation failed'});
				  $scope.userDataStatus = "ready";

			  });
		}

		this.switchUserStatus =  function(){
			$scope.userDataStatus = "saving";
			AdminService.switchUserStatus(instance.user.username)
					.then(function(response) {
				 		instance.user = response.data;
				 		instance.alerts.push({type: 'success', msg: 'User status switched'});
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

		this.resetUserPassword =  function() {
			$scope.userDataStatus = "saving";
			AdminService.resetUserPassword(instance.user.username)
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
			AdminService.deleteUser(instance.user.username)
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
