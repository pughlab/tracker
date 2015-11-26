angular
  .module 'tracker.admin'

  .controller 'StudySettingsEditorController', Array '$scope', '$http', '$stateParams', '$q', '$timeout', ($scope, $http, $stateParams, $q, $timeout) ->

    $scope.labels = {}

    ## Safely propogate study values into the labels if they are different
    $scope.$watch 'study.study.options.stateLabels', (newValue, oldValue) ->
      if typeof newValue != 'undefined' && ! angular.equals newValue, oldValue
        labels = {}
        for own k, v of newValue
          labels[v] = k
        $scope.labels = labels if ! angular.equals $scope.labels, labels

    ## Safely propogate settings back into the study if they are different
    $scope.$watchCollection 'labels', (newLabels, oldLabels) ->
      if typeof newLabels != 'undefined' && ! angular.equals newLabels, oldLabels
        stateLabels = {}
        for own k, v of newLabels
          stateLabels[v] = k if v? and v.length > 0
        if ! angular.equals $scope.study.study.options.stateLabels, stateLabels
          $scope.study.study.options.stateLabels = stateLabels
          $scope.$emit 'admin:modified'
