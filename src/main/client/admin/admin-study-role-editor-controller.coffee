angular
  .module 'tracker.admin'

  .controller 'StudyRoleEditorController', Array '$scope', '$state', ($scope, $state) ->

    $scope.roles = []

    $scope.selectedRole = undefined
    originalSelectedRole = undefined

    $scope.selectRole = (role) ->
      $scope.selectedRole = view
      originalSelectedRole = angular.copy($scope.selectedRole)

    $scope.$watchCollection 'selectedRole', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedRole)
        $scope.$emit 'admin:modified'

    $scope.deleteRole = (role) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
      $scope.study.roles = $scope.study.roles.filter (att) -> att != role
      $scope.$emit 'admin:modified'

    $scope.newRole = () ->
      newRole = {name: 'UNNAMED'}
      $scope.roles.unshift newRole
      $scope.selectedRole = newRole
      originalSelectedRole = angular.copy($scope.selectedRole)
      $scope.$emit 'admin:modified'

    $scope.editRole = (role) ->
      $state.go 'studyRoleManage', {studyName: $scope.study.name, roleName: role.name}

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
