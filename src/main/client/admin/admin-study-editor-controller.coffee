angular
  .module 'tracker.admin'

  .controller 'StudyEditorController', Array '$scope', '$http', '$stateParams', '$q', '$timeout', ($scope, $http, $stateParams, $q, $timeout) ->

    console.log 'StudyEditorController'

    $scope.study = undefined
    originalStudy = undefined

    $scope.modified = false
    $scope.alerts = []
    $scope.params = $stateParams

    $scope.$on 'admin:modified', (e) ->
      $scope.modified = true

    $scope.reset = () ->
      $scope.$broadcast 'admin:reset'

      $scope.study = originalStudy
      originalStudy = angular.copy($scope.study)

      $timeout () ->
        $scope.modified = false

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.save = () ->
      $scope.alerts = []
      $http
        .put("/api/studies/#{encodeURIComponent($stateParams.studyName)}/schema", $scope.study)
        .success (response) ->
          originalStudy = response
          $scope.reset()
        .error (response) ->
          message = response?.error or response
          $scope.alerts.push {type: 'danger', msg: message}

    $http
      .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/schema")
      .success (schema) ->
        originalStudy = schema
        $scope.reset()
      .error (error) ->
        console.log "Error", error
