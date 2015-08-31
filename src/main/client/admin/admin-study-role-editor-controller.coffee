angular
  .module 'tracker.admin'

  .controller 'StudyRoleEditorController', Array '$scope', '$state', '$http', 'studyName', ($scope, $state, $http, studyName) ->

    $scope.alerts = []
    $scope.roles = []
    originalRoles = []
    $scope.modified = false

    $scope.$on 'admin:modified', (e) ->
      $scope.modified = true

    $scope.selectedRole = undefined
    originalSelectedRole = undefined

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.save = () ->
      $http
        .put "/api/studies/#{encodeURIComponent(studyName)}/roles", {roles: $scope.roles}
        .success (response) ->
          originalRoles = response.roles
          $scope.reset()
        .error (response) ->
          message = response?.error or response
          $scope.alerts.push {type: 'danger', msg: message}

    $scope.reset = () ->
      $scope.roles = originalRoles
      originalRoles = angular.copy $scope.roles
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
      $scope.modified = false

    $scope.selectRole = (role) ->
      $scope.selectedRole = role
      originalSelectedRole = angular.copy($scope.selectedRole)

    $scope.$watchCollection 'selectedRole', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedRole)
        $scope.$emit 'admin:modified'

    $scope.deleteRole = (role) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
      $scope.roles = $scope.roles.filter (att) -> att != role
      $scope.$emit 'admin:modified'

    $scope.newRole = () ->
      newRole = {name: 'UNNAMED', users: [], permissions: []}
      $scope.roles.unshift newRole
      $scope.selectedRole = newRole
      originalSelectedRole = angular.copy($scope.selectedRole)
      $scope.$emit 'admin:modified'

    $scope.editRole = (role) ->
      $state.go 'studyRoleManage', {studyName: $scope.study.name, roleName: role.name}

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined

    $http
      .get("/api/studies/#{encodeURIComponent(studyName)}/roles")
      .success (response) ->
        originalRoles = response.roles
        $scope.reset()
      .error (error) ->
        console.log "Error", error
