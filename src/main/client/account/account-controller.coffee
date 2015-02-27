angular
  .module 'tracker.account'

  .controller 'AccountController', Array '$scope', '$stateParams', '$http', ($scope, $stateParams, $http) ->

    ## Don't expose $stateParams directly (we could!) but instead, do a server query on user
    ## settings, which meab\ns we can validate and get user information according to permissions.
    ## In effect, we need to allow users to get hold of their own information. And that's okay.
    ## Admin users (not study admin users) can also get hold of this. However, even they should 
    ## not be able to set a password. What they can do is amend the email address, which is
    ## probably going to be important. They should also be able to add users.

    $scope.user = undefined
    originalUser = undefined
    $scope.modified = false

    $scope.$watchCollection 'user.user', (newValue, oldValue) ->
      if ! angular.equals newValue, originalUser?.user
        $scope.modified = true

    $http
      .get "/api/authentication/user/#{encodeURIComponent($stateParams.username)}"
      .success (data) ->
        $scope.user = data
        originalUser = angular.copy($scope.user)
        $scope.modified = false
