angular.module('gvconsole-admin', []);

angular.module('gvconsole-admin')
.controller('UserListController',['$scope','$http', '$location', function($scope, $http, $location){

	var resourceURI = 'http://localhost:8181/cxf/gviam/admin/users';
	var instance = this;

	this.alerts = [];

	this.list = [];
	this.currentPage = 1;
	this.loadList = function() {
		$http.get(resourceURI).then(

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
	}

	instance.loadList();

}]);

angular.module('gvconsole-admin')
.controller('UserDetailController',['$scope','$http', '$routeParams', '$location', function($scope, $http, $routeParams, $location){

		$scope.userDataStatus = "loading";
		this.alerts = [];

		this.user = {roles : {}};
		this.roles = [{name: "admin", description:'Created by GVConsole'},{name: "system", description:'Created by GVConsole'},{name: "gvadmin", description:'Created by GVConsole'}];

		var instance = this;
		var saveRequest = { url: 'http://localhost:8181/cxf/gviam/admin/users', headers: {'Content-Type': 'application/json'} };

		$http.get('http://localhost:8181/cxf/gviam/admin/roles').success(function(data){
			instance.roles = data;
		});

		if ($routeParams.userId == 'new') {
			saveRequest.method='POST';
			this.user.enabled = true;
			$scope.newUser = true;
			$scope.userDataStatus = "ready";
		} else {
			saveRequest.method='PUT';

			$http.get('http://localhost:8181/cxf/gviam/admin/users/'+$routeParams.userId).then(function(response) {
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

			saveRequest.data = instance.user;

			$http(saveRequest)
			.then(function(response) {

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
			$http.patch('http://localhost:8181/cxf/gviam/admin/users/'+instance.user.username+'/enabled')
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
			$http.patch('http://localhost:8181/cxf/gviam/admin/users/'+instance.user.username+'/password')
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
			$http.delete('http://localhost:8181/cxf/gviam/admin/users/'+instance.user.username)
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
