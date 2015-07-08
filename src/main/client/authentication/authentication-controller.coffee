angular
  .module 'tracker.authentication'
  
  .controller 'LoginController', Array '$scope', 'authenticationService', ($scope, authenticationService) ->

    $scope.clearMessage = () ->
      $scope.message = ""
  
    $scope.$on 'event:loginDenied', (evt, data) ->
      $scope.message = data.message
  
    $scope.ok = (username, password) ->
      $scope.clearMessage()
      console.log "Starting login process", {username: username, password: password}
      authenticationService.login $scope, username, password

    $scope.cancel = () ->
      $scope.clearMessage()
      $scope.$emit "event:loginCancelled"


  .controller 'AuthenticationController', Array '$scope', '$state', ($scope, $state) ->
    $scope.username = undefined
    $scope.password = undefined
    $scope.shown = false
    $scope.message = ""

    $scope.$on 'event:loginRequired', () ->
      $state.go 'login'


    $scope.$on 'event:loginDenied', (evt, data) ->
      $scope.message = data.message

      if ! $scope.shown
        $scope.$emit 'event:loginRequired'
