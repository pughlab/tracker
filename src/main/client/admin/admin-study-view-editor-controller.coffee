angular
  .module 'tracker.admin'

  ## Some refactoring of the editing here, mainly to make the UI better. Basically, each controller
  ## should notify when something has changed as an event, and should handle appropriate events to
  ## reset.
  .controller 'StudyViewEditorController', Array '$scope', '$state', ($scope, $state) ->

    $scope.$state = $state
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
      $scope.schema.views = $scope.schema.views.filter (att) -> att != view
      $scope.$emit 'admin:modified'

    $scope.newGridView = () ->
      newView = {name: 'unnamed', description: 'Untitled View'}
      $scope.schema.views.unshift newView
      $scope.selectedView = newView
      originalSelectedView = angular.copy($scope.selectedView)
      $scope.$emit 'admin:modified'

    $scope.newPageView = () ->
      newView = {name: 'unnamed', description: 'Untitled View', body: '<h3>Empty view</h3>'}
      $scope.schema.views.unshift newView
      $scope.selectedView = newView
      originalSelectedView = angular.copy($scope.selectedView)
      $scope.$emit 'admin:modified'

    $scope.editView = (view) ->
      $state.go 'studyViewManage', {studyName: $scope.study.name, viewName: view.name}

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedView = undefined
      originalSelectedView = undefined
