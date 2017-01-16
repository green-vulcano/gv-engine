angular.module('gvconsole-admin', []);
	
angular.module('gvconsole-admin')
.controller('UserListController',['$http', '$location', function($http, $location){
		var instance = this;
		this.alerts = [];
		
		this.users = [];		
		this.currentPage = 1;	
		this.loadUsers = function() {
			$http.get('/cxf/gviam/admin/users').then(function(response){
				instance.alerts = [];
				instance.users = response.data;
				},function(response){
		 			switch (response.status) {
	 				case 404:
	 					instance.alerts.push({type: 'danger', msg: 'Users not found'});
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
		
		instance.loadUsers();	
		
}]);

angular.module('gvconsole-admin')
.controller('UserDetailController',['$scope','$http', '$routeParams', '$location', function($scope, $http, $routeParams, $location){
				
		$scope.userDataStatus = "loading";		
		this.alerts = [];
		
		this.user = {roles : {}};			
		this.roles = [{name: "admin", description:'Created by GVConsole'},{name: "system", description:'Created by GVConsole'},{name: "gvadmin", description:'Created by GVConsole'}];	
					
		var instance = this;
		var saveRequest = { url: '/cxf/gviam/admin/users', headers: {'Content-Type': 'application/json'} };
		
		$http.get('/cxf/gviam/admin/roles').success(function(data){
			instance.roles = data;
		});
		
		if ($routeParams.userId == 'new') {
			saveRequest.method='POST';
			this.user.enabled = true;
			$scope.newUser = true;
			$scope.userDataStatus = "ready";					
		} else {
			saveRequest.method='PUT';
			
			$http.get('/cxf/gviam/admin/users/'+$routeParams.userId).then(function(response) {
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
			$http.patch('/cxf/gviam/admin/users/'+instance.user.username+'/enabled')
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
			$http.patch('/cxf/gviam/admin/users/'+instance.user.username+'/password')
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
			$http.delete('/cxf/gviam/admin/users/'+instance.user.username)
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