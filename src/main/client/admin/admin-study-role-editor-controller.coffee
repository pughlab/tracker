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


  ## Some refactoring of the editing here, mainly to make the UI better. Basically, each controller
  ## should notify when something has changed as an event, and should handle appropriate events to
  ## reset.
  .controller 'ViewEditorController', Array '$scope', '$state', ($scope, $state) ->

    $scope.selectedView = undefined
    originalSelectedView = undefined

    $scope.selectView = (view) ->
      $scope.selectedView = view
      originalSelectedView = angular.copy($scope.selectedView)

    $scope.$watchCollection 'selectedView', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedView)
        $scope.$emit 'admin:modified'

    $scope.deleteView = (view) ->
      $scope.selectedView = undefined
      originalSelectedView = undefined
      $scope.study.views = $scope.study.views.filter (att) -> att != view
      $scope.$emit 'admin:modified'

    $scope.newView = () ->
      newView = {name: 'unnamed', description: 'Untitled View'}
      $scope.study.views.unshift newView
      $scope.selectedView = newView
      originalSelectedView = angular.copy($scope.selectedView)
      $scope.$emit 'admin:modified'

    $scope.editView = (view) ->
      $state.go 'studyViewManage', {studyName: $scope.study.name, viewName: view.name}

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedView = undefined
      originalSelectedView = undefined
