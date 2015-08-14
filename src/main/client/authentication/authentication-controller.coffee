angular
  .module 'tracker.authentication'

  .controller 'LoginController', Array '$scope', '$state', 'authenticationService', ($scope, $state, authenticationService) ->

    if $state.params.challenge == 'default'
      $state.go 'home'
      return
    else
      $scope.challenge = $state.params.challenge

    $scope.clearMessage = () ->
      $scope.message = ""

    $scope.$on 'event:loginDenied', (evt, data) ->
      $scope.message = data.message

    $scope.ok = (data) ->
      data = if $scope.challenge == 'oidc'
        {redirect: true, client_name: 'uhn'}
      else
        {username: $scope.username, password: $scope.password}
      $scope.clearMessage()
      console.log "Starting login process", data
      authenticationService.login $scope, data

    $scope.cancel = () ->
      $scope.clearMessage()
      $scope.$emit "event:loginCancelled"


  .controller 'AuthenticationController', Array '$scope', '$state', ($scope, $state) ->
    $scope.challenge = undefined
    $scope.username = undefined
    $scope.password = undefined
    $scope.shown = false
    $scope.message = ""

    $scope.$on 'event:loginRequired', (event, values) ->
      challenge = values.challenge
      match = /(\w+)/.exec challenge
      $state.go 'login', {challenge: match[1].toLowerCase()}


    $scope.$on 'event:loginDenied', (evt, data) ->
      $scope.message = data.message

      if ! $scope.shown
        $scope.$emit 'event:loginRequired'
