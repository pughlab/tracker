angular
  .module 'tracker.admin'

  .controller 'ViewController', Array '$scope', '$http', '$stateParams', ($scope, $http, $stateParams) ->

    $scope.study = undefined
    $scope.view = undefined
    $scope.initializedAttributes = false
    $scope.modified = false
    $scope.originalView = undefined
    $scope.selectedAttribute = undefined
    $scope.filterEnabled = false

    $scope.attributeSortableOptions = 
      connectWith: "#viewSortable"
      update: (e, ui) ->
        console.log 'a update', e, ui
      remove: (e, ui) ->
        console.log 'a remove', e, ui

    $scope.viewSortableOptions = 
      connectWith: "#attributeSortable"
      update: (e, ui) ->
        console.log 'a update', e, ui
        $scope.$evalAsync () ->
          $scope.modified = true

      remove: (e, ui) ->
        console.log 'a remove', e, ui

    $scope.selectAttribute = (attribute) ->
      $scope.selectedAttribute = attribute
   
    $scope.reset = () ->
      $scope.modified = false
      $scope.initializedAttribute = false
      $scope.view = $scope.originalView
      $scope.originalView = angular.copy($scope.view)

    $scope.save = (view) ->
      $http
        .put("/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}/schema", view)
        .success (response) ->
          $scope.view = response
          $scope.originalView = angular.copy($scope.view)
          $scope.modified = false
          $scope.initializedAttribute = false

        .error (response) ->
          console.log "Error", response

    removeUsedAttributes = () ->
      used = {}
      for attribute in $scope.view.attributes
        used[attribute.id] = attribute

      $scope.study.attributes = $scope.study.attributes.filter (att) -> ! used[att.id]
      $scope.initializedAttributes = true

    $scope.$watch 'study.attributes', (value) ->
      if value != undefined and $scope.view?.attributes? and ! $scope.initializedAttributes
        removeUsedAttributes()

    $scope.$watch 'view.attributes', (value) ->
      if value != undefined and $scope.study?.attributes? and ! $scope.initializedAttributes
        removeUsedAttributes()

    $scope.$on 'setModified', (e) ->
      $scope.modified = true

    $http
      .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/schema")
      .success (response) ->
        $scope.study = response
      .error (response) ->
        console.log "Error", response

    $http
      .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}/schema")
      .success (response) ->
        $scope.view = response
        $scope.originalView = angular.copy($scope.view)
      .error (response) ->
        console.log "Error", response

