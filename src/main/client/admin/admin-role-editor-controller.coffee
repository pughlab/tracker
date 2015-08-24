angular
  .module 'tracker.admin'

  ## Some refactoring of the editing here, mainly to make the UI better. Basically, each controller
  ## should notify when something has changed as an event, and should handle appropriate events to
  ## reset.
  .controller 'RoleEditorController', Array '$scope', ($scope) ->

    $scope.selectedRole = undefined
    originalSelectedRole = undefined

    $scope.selectRole = (role) ->
      $scope.selectedRole = role
      originalSelectedRole = angular.copy($scope.selectedRole)

    $scope.deleteRole = (role) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
      $scope.study.roles = $scope.study.roles.filter (att) -> att != role
      $scope.$emit 'admin:modified'

    $scope.newRole = () ->
      newRole = {name: 'unnamed', description: 'Untitled Role'}
      $scope.study.roles.unshift newRole
      $scope.selectedRole = newRole
      originalSelectedRole = angular.copy($scope.selectedRole)
      $scope.$emit 'admin:modified'

    $scope.$watchCollection 'selectedRole', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedRole)
        $scope.$emit 'admin:modified'

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
