angular.module('gvconsole')
.service('ToolsService', ['ENDPOINTS', '$http', function(Endpoints, $http){


}]);

angular.module('gvconsole')
.controller('ToolsController', [ 'Base64', 'ToolsService', '$scope', function(Base64, ToolsService, $scope){

    $scope.convert = function(){

      if($scope.convertType === "binary" && $scope.resultType === "hexadecimal"){
      $scope.converterOutput = parseInt($scope.converterInput, 2).toString(16);
      }

    }

}]);
