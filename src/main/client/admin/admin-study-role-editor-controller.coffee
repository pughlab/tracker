angular
  .module 'tracker.admin'

  .controller 'StudyRoleEditorController', Array '$scope', '$state', '$http', ($scope, $state, $http) ->


    $scope.selectedRole = undefined
    originalSelectedRole = undefined

    $scope.$watchCollection 'selectedRole', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedRole)
        $scope.$emit 'admin:modified'

    $scope.selectRole = (role) ->
      $scope.selectedRole = role
      originalSelectedRole = angular.copy($scope.selectedRole)

    $scope.deleteRole = (role) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
      $scope.roles.roles = $scope.roles.roles.filter (att) -> att != role
      $scope.$emit 'admin:modified'

    $scope.newRole = () ->
      newRole = {name: 'UNNAMED', users: [], permissions: []}
      $scope.roles.roles.unshift newRole
      $scope.selectedRole = newRole
      originalSelectedRole = angular.copy($scope.selectedRole)
      $scope.$emit 'admin:modified'

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined

    $scope.editRole = (role) ->
      $state.go 'studyRoleManage', {studyName: $scope.study.name, roleName: role.name}
