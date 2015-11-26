angular
  .module 'tracker.admin'

  .controller 'StudyAttributeEditorController', Array '$scope', ($scope) ->

    $scope.selectedAttribute = undefined
    originalSelectedAttribute = undefined

    console.log "Initialized StudyAttributeEditorController"

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedAttribute = undefined
      originalSelectedAttribute = undefined

    ## Detect changes to attributes and notify as modified
    $scope.$watchCollection 'selectedAttribute', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedAttribute)
        console.log "Sending admin:modified selectedAttribute", newValue, oldValue
        $scope.$emit 'admin:modified'

    $scope.$watchCollection 'selectedAttribute.options.tags', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedAttribute?.options?.tags)
        console.log "Sending admin:modified selectedAttribute.options.tags", newValue, oldValue
        $scope.$emit 'admin:modified'

    $scope.$watchCollection 'selectedAttribute.options.values', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedAttribute?.options?.values)
        console.log "Sending admin:modified selectedAttribute.options.values", newValue, oldValue
        $scope.$emit 'admin:modified'

    $scope.selectAttribute = (attribute) ->
      $scope.selectedAttribute = attribute
      originalSelectedAttribute = angular.copy($scope.selectedAttribute)

    $scope.deleteAttribute = (attribute) ->
      $scope.selectedAttribute = undefined
      originalSelectedAttribute = undefined
      $scope.study.attributes = $scope.study.attributes.filter (att) -> att != attribute
      console.log "Sending admin:modified deleteAttribute", newValue, oldValue
      $scope.$emit 'admin:modified'

    $scope.newAttribute = () ->
      newAttribute = {id: undefined, name: 'unnamed', label: 'Untitled Attribute'}
      $scope.study.attributes.push newAttribute
      $scope.selectedAttribute = newAttribute
      originalSelectedAttribute = angular.copy($scope.selectedAttribute)
      console.log "Sending admin:modified newAttribute", newValue, oldValue
      $scope.$emit 'admin:modified'

    $scope.attributeTypes = [
      {id: 'string', text: "String"}
      {id: 'boolean', text: "Boolean"}
      {id: 'date', text: "Date"}
      {id: 'option', text: "Option"}
      {id: 'number', text: "Number"}
    ]

    $scope.attributeTags = [
      {id: 'identifiable', text: "Identifiable"}
    ]

    $scope.attributeDisplays = [
      {id: 'normal', text: "Normal"}
      {id: 'pin_left', text: "Pin left"}
      {id: 'pin_right', text: "Pin right"}
      {id: 'hidden', text: "Hidden"}
    ]
