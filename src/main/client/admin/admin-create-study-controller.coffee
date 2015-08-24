angular
  .module 'tracker.admin'

  .controller 'CreateStudyController', Array '$scope', '$http', '$state', ($scope, $http, $state) ->

    $scope.study = {}
    $scope.alerts = []

    ## Creating a new study ought to be a simple POST to the main studies resource...
    ## Yes, I said "ought"  -- it isn't quite that simple as we really need to make the
    ## role representation consistent.

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.create = (study) ->
      $scope.alerts = []
      $http
        .post "/api/studies", study
        .success (response) ->
          $state.go 'adminStudy', {studyName: $scope.study.name}
        .error (response, status) ->
          $scope.alerts.push {type: 'danger', msg: response}
