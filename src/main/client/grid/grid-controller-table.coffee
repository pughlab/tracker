angular
  .module 'tracker.grid'

  ## Controller to create a new record. This handles the dialog box that's used to 
  ## create a new record.
  .controller 'GridTableController', Array '$scope', '$http', '$stateParams', ($scope, $http, $stateParams) ->

    $scope.attributes = undefined
    $scope.study = undefined
    $scope.permissions = undefined
    $scope.editingStatus = false

    $http
      .get "/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}/attributes", {}
      .success (response) ->
        $scope.attributes = response.attributes
        $scope.study = response.study
        $scope.view = response.view
        $scope.permissions = response.permissions
