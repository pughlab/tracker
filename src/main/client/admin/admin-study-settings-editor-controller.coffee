angular
  .module 'tracker.admin'

  .controller 'StudySettingsEditorController', Array '$scope', '$http', '$stateParams', '$q', '$timeout', ($scope, $http, $stateParams, $q, $timeout) ->

    $scope.labels = {}

    console.log "Initialized StudySettingsEditorController"
