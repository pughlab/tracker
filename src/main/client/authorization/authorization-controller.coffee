angular
  .module 'tracker.authorization'

  .controller 'AuthorizationRolesController', Array '$scope', '$http', '$state', ($scope, $http, $state) ->

    $scope.totalItems = 0
    $scope.pageSize = 10
    $scope.currentPage = 1

    $scope.roles = []
    $scope.name = ''

    $scope.alerts = []

    $scope.pageChanged = () ->
      queryOptions = {page: $scope.currentPage, pageSize: $scope.pageSize}
      queryOptions.q = $scope.name if $scope.name?
      $http
        .get '/api/authorization/roles', {params: queryOptions}
        .success (response) ->
          $scope.roles = response.roles
          $scope.totalItems = response.counts.total
          foundExactMatch = false
          for role in response.roles
            if $scope.name == role.name
              foundExactMatch = true
              break
          $scope.foundExactMatch = foundExactMatch

    $scope.$watch 'name', (value) ->
      $scope.pageChanged()

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.createRole = (name) ->
      $scope.alerts = []
      $http
        .post '/api/authorization/roles', {name: name}
        .success (response) ->
          $state.go 'adminRole', {name: name}
        .error (response) ->
          $scope.alerts.push {type: 'danger', msg: response}

    $scope.pageChanged()


  .controller 'AuthorizationRoleController', Array '$scope', '$stateParams', '$http', ($scope, $stateParams, $http) ->

    ## Don't expose $stateParams directly (we could!) but instead, do a server query on role
    ## settings, which means we can validate and get role information according to permissions.

    $scope.role = undefined
    originalRole = undefined
    $scope.modified = false
    $scope.alerts = []

    $scope.$watchCollection 'role', (newValue, oldValue) ->
      if ! angular.equals newValue, originalRole
        $scope.modified = true

    $scope.saveRole = (role) ->
      role = angular.copy(role)
      console.log "Saving", role
      $http
        .put "/api/authorization/roles/#{encodeURIComponent($stateParams.name)}", role
        .success (response) ->
          $scope.alerts.push {type: 'success', msg: "Successfully saved"}
        .error (response) ->
          message = if response.error? then response.error else response
          $scope.alerts.push {type: 'danger', msg: message}

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $http
      .get "/api/authorization/roles/#{encodeURIComponent($stateParams.name)}"
      .success (data) ->
        console.log "Got data", data
        role = data.role
        role.users = data.users
        role.permissions = data.permissions
        $scope.role = role
        originalRole = angular.copy($scope.role)
        $scope.modified = false


