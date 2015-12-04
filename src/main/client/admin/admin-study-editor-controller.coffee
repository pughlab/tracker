angular
  .module 'tracker.admin'

  .controller 'StudyEditorController', Array '$scope', '$http', '$stateParams', '$q', '$timeout', ($scope, $http, $stateParams, $q, $timeout) ->

    $scope.study = undefined
    originalStudy = undefined

    $scope.schema = undefined
    originalSchema = undefined

    $scope.roles = undefined
    originalRoles = undefined

    $scope.modified = false
    $scope.alerts = []
    $scope.params = $stateParams

    loading = true

    $scope.$watchCollection 'study.study', (newValue, oldValue) ->
      if newValue != oldValue && ! angular.equals(newValue, oldValue) && ! loading
        $scope.$emit 'admin:modified'

    $scope.$watchCollection 'study.study.options', (newValue, oldValue) ->
      if newValue != oldValue && ! angular.equals(newValue, oldValue) && ! loading
        $scope.$emit 'admin:modified'

    $scope.$on 'admin:modified', (e) ->
      $scope.modified = true

    $scope.reset = () ->
      $scope.$broadcast 'admin:reset'

      $scope.study = originalStudy
      originalStudy = angular.copy($scope.study)

      $scope.schema = originalSchema
      originalSchema = angular.copy($scope.schema)

      $scope.roles = originalRoles
      originalRoles = angular.copy($scope.roles)

      $timeout () ->
        $scope.modified = false

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.save = () ->
      $scope.alerts = []

      writeStudy = () ->
        if angular.equals originalStudy, $scope.study
          $q.defer()
        else
          $http
            .put("/api/studies/#{encodeURIComponent($stateParams.studyName)}", $scope.study)
            .then (response) ->
              originalStudy = response.data
            .catch (response) ->
              $scope.alerts.push {type: 'danger', msg: response.data?.error or response.data}

      writeSchema = () ->
        if angular.equals originalSchema, $scope.schema
          $q.defer()
        else
          $http
            .put("/api/studies/#{encodeURIComponent($stateParams.studyName)}/schema", $scope.schema)
            .then (response) ->
              originalSchema = response.data
            .catch (response) ->
              $scope.alerts.push {type: 'danger', msg: response.data?.error or response.data}

      writeRoles = () ->
        if angular.equals originalRoles, $scope.roles
          $q.defer()
        else
          $http
            .put("/api/studies/#{encodeURIComponent($stateParams.studyName)}/roles", $scope.roles)
            .then (response) ->
              originalRoles = response.data
            .catch (response) ->
              $scope.alerts.push {type: 'danger', msg: response.data?.error or response.data}

      loading = true
      $q
        .all [writeStudy(), writeSchema(), writeRoles()]
        .then () ->
          loading = false
          $scope.reset()

    readStudy = () ->
      console.log "Called readStudy"
      $http
        .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}")
        .then (response) ->
          response.data.study.options ?= {}
          response.data.study.options.stateRules ?= []
          response.data.study.options.stateLabels ?= {}
          originalStudy = response.data
        .catch (response) ->
          console.log "Error", response.data

    readSchema = () ->
      console.log "Called readSchema"
      $http
        .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/schema")
        .then (response) ->
          originalSchema = response.data
        .catch (response) ->
          console.log "Error", response.data

    readRoles = () ->
      console.log "Called readRoles"
      $http
        .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/roles")
        .then (response) ->
          originalRoles = response.data
        .catch (response) ->
          console.log "Error", response.data

    loading = true
    $q
      .all [readStudy(), readSchema(), readRoles()]
      .then () ->
        loading = false
        console.log "Finished loading"
        $scope.reset()
