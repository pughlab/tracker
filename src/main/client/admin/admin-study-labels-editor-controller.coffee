angular
  .module 'tracker.admin'

  .controller 'StudyLabelsEditorController', Array '$scope', '$http', '$stateParams', '$q', '$timeout', ($scope, $http, $stateParams, $q, $timeout) ->

    $scope.labels = {}
    $scope.labelNames = []
    $scope.attributeNames = []
    $scope.rules = []

    $scope.$watch 'schema.attributes', (newValue, oldValue) ->
      if typeof newValue != 'undefined'
        $scope.attributeNames = []
        for attribute in newValue
          $scope.attributeNames.push {id: attribute.name, text: attribute.name}

    ## Safely propogate study values into the labels if they are different
    $scope.$watch 'study.study.options.stateLabels', (newValue, oldValue) ->
      if typeof newValue != 'undefined'
        labels = {}
        labelNames = []
        for own k, v of newValue
          labels[v] = k
          labelNames.push {id: k, text: k}
        $scope.labels = labels if ! angular.equals $scope.labels, labels
        $scope.labelNames = labelNames if ! angular.equals $scope.labelNames, labelNames

    ## Safely propogate study rules into the rule values if they are different
    $scope.$watch 'study.study.options.stateRules', (newValue, oldValue) ->
      if typeof newValue != 'undefined'
        $scope.rules = newValue if ! angular.equals $scope.rules, newValue


    ## Safely propogate settings back into the study if they are different
    $scope.$watchCollection 'labels', (newLabels, oldLabels) ->
      if typeof $scope.study != 'undefined' and typeof newLabels != 'undefined' && ! angular.equals newLabels, oldLabels
        stateLabels = {}
        for own k, v of newLabels
          stateLabels[v] = k if v? and v.length > 0
        if ! angular.equals $scope.study.study.options.stateLabels, stateLabels
          $scope.study.study.options.stateLabels = stateLabels
          $scope.$emit 'admin:modified'

    ## Safely propogate rules back into the study if they are different
    ruleWatcher = (newRules, oldRules) ->
      if typeof newRules != 'undefined' and $scope.study? and ! angular.equals newRules, oldRules
        $scope.study.study.options.stateRules = newRules
        $scope.$emit 'admin:modified'

    $scope.$watch 'rules', ruleWatcher, true

    $scope.addRule = (index) ->
      rule = new Object()
      rule.state = ''
      rule.attribute = ''
      rule.value = ''
      $scope.rules.splice(index, 0, rule)

    $scope.removeRule = (index) ->
      $scope.rules.splice(index, 1)
