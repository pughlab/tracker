angular
  .module 'tracker.studies'

  .controller 'StudiesController', Array '$scope', '$http', ($scope, $http) ->
    $scope.studies = undefined
    $scope.permissions = undefined

    ## Permissions and studies can both change when the user and/or their permissions
    ## change. We should detect that. 

    initializePermissions = () ->
      $http
        .get("/api/authentication/can/create/system/system")
        .success (response) ->
          $scope.permissions = response
        .error (response) ->
          console.log "Error", response

    initializeStudies = () ->
      $http
        .get('/api/studies')
        .success (response) ->
          $scope.studies = response
          initializePermissions()
        .error (response) ->
          console.log "Error", response

    $scope.$on 'event:loginConfirmed', (e) ->
      initializeStudies()

    initializeStudies()


  .controller 'StudyController', Array '$scope', '$http', '$stateParams', ($scope, $http, $stateParams) ->
    $scope.study = undefined
    encodedStudyName = encodeURIComponent($stateParams.studyName)

    $http
      .get("/api/studies/#{encodedStudyName}/views")
      .success (response) ->
        $scope.study = response
      .error (response) ->
        console.log "Error", response
