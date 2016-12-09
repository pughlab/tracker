angular
  .module 'tracker.studies'

  .controller 'StudiesController', Array '$scope', '$http', '$log', ($scope, $http, $log) ->
    $scope.studies = undefined
    $scope.permissions = undefined

    ## Permissions and studies can both change when the user and/or their permissions
    ## change. We should detect that.

    initializeStudies = () ->
      $http
        .get('/api/studies')
        .success (response) ->
          $scope.studies = response
        .error (response) ->
          $log.error "Error", response

    $scope.$on 'event:loginConfirmed', (e) ->
      initializeStudies()

    initializeStudies()


  .controller 'StudyController', Array '$scope', '$http', '$stateParams', ($scope, $http, $stateParams) ->
    $scope.study = undefined
    encodedStudyName = encodeURIComponent($stateParams.studyName)

    $http
      .get("/api/studies/#{encodedStudyName}")
      .success (response) ->
        $scope.study = response
      .error (response) ->
        $log.error "Error", response


  ## Added a new controller that retrieves the about information for a study, but not much more.
  ## We do this to allow a public view of the about text, independently of the rest of the
  ## permissions system.

  .controller 'StudyAboutController', Array '$scope', '$http', '$stateParams', ($scope, $http, $stateParams) ->
    $scope.study = undefined
    encodedStudyName = encodeURIComponent($stateParams.studyName)

    $http
      .get("/api/about/#{encodedStudyName}")
      .success (response) ->
        $log.debug "About about", response
        $scope.study = response
      .error (response) ->
        $log.error "Error", response
