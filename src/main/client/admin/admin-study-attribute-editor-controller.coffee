angular
  .module 'tracker.admin'

  .controller 'StudyAttributeEditorController', Array '$scope', ($scope) ->

    $scope.selectedAttribute = undefined
    originalSelectedAttribute = undefined

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedAttribute = undefined
      originalSelectedAttribute = undefined

    updater = (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedAttribute)
        $scope.$emit 'admin:modified'

    ## Detect changes to attributes and notify as modified
    $scope.$watch 'selectedAttribute', updater, true

    $scope.selectAttribute = (attribute) ->
      $scope.selectedAttribute = attribute
      originalSelectedAttribute = angular.copy($scope.selectedAttribute)

    $scope.deleteAttribute = (attribute) ->
      $scope.selectedAttribute = undefined
      originalSelectedAttribute = undefined
      $scope.schema.attributes = $scope.schema.attributes.filter (att) -> att != attribute
      console.log "Sending admin:modified deleteAttribute", attribute
      $scope.$emit 'admin:modified'

    $scope.newAttribute = () ->
      newAttribute = {id: undefined, type: 'string', name: 'unnamed', label: 'Untitled Attribute'}
      $scope.schema.attributes.push newAttribute
      $scope.selectedAttribute = newAttribute
      originalSelectedAttribute = angular.copy($scope.selectedAttribute)
      console.log "Sending admin:modified newAttribute", newAttribute
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
