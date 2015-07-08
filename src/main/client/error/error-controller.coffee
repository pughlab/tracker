angular
  .module 'tracker.error'

  .controller 'ErrorController', Array '$scope', '$http', '$stateParams', ($scope, $http, $stateParams) ->
    $scope.message = $stateParams.message
