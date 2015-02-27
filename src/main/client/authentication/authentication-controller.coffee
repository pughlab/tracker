angular
  .module 'tracker.authentication'

  .controller 'LoginController', Array '$scope', '$modalInstance', ($scope, $modalInstance) ->

    $scope.ok = (username, password) ->
      $scope.clearMessage()
      $modalInstance.close({username: username, password: password})

    $scope.cancel = () ->
      $scope.clearMessage()
      $modalInstance.dismiss('cancel')
      $scope.$emit "event:loginCancelled"


  .controller 'AuthenticationController', Array '$scope', '$modal', ($scope, $modal) ->
    $scope.username = undefined
    $scope.password = undefined
    $scope.shown = false
    $scope.message = ""

    $scope.clearMessage = () ->
      $scope.message = ""
  
    $scope.$on 'event:loginRequired', () ->

      modal = $modal.open
        templateUrl: '/tracker/authentication/login.html'
        controller: 'LoginController'
        scope: $scope

      modal.result.then (selected) ->
        $scope.shown = false
        $scope.$emit "event:loginRequest", selected.username, selected.password

      modal.opened.then () ->
        $scope.shown = true

    $scope.$on 'event:loginDenied', (evt, data) ->
      $scope.message = data.message

      if ! $scope.shown
        $scope.$emit 'event:loginRequired'
