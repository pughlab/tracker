angular
  .module 'tracker.grid'

  ## Controller to handle the button used to export a record.
  .controller 'GridActionController', Array '$scope', '$modal', '$stateParams', ($scope, $modal, $stateParams) ->

    $scope.message = undefined
    $scope.identifier = undefined
    $scope.shown = false

    $scope.export = () ->
      location.href = "/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}?page=all&media=xml"


  ## Controller to handle connected users
  .controller 'ConnectedUserController', Array '$scope', 'socketFactory', ($scope, socketFactory) ->
    $scope.currentUsers = []

    socket = socketFactory()

    socket.on 'userConnected', (data) ->
      $scope.$apply () ->
        if not (data.name in $scope.currentUsers)
          $scope.currentUsers.push data.name

    socket.on 'userDisconnected', (data) ->
      $scope.$apply () ->
        $scope.currentUsers = (user for user in $scope.currentUsers when user != data.name)

    ## If we get a disconnect, we forget all users. We probably should also generate an alert to let people
    ## know that we are currently locked. 
    socket.on 'disconnect', (e) ->
      $scope.$evalAsync () ->
        $scope.currentUsers = []

    socket.on 'welcome', (data) ->
      $scope.$broadcast 'socket:welcome', data

    socket.on 'gridEdit', (data) ->
      data.userNumber = $scope.currentUsers.indexOf(data.user)
      if data.userNumber != -1
        data.editingClasses = "editedCellText editedCellUser-#{data.userNumber}"

      $scope.$broadcast 'socket:entityPropertyChange', data

    $scope.$on 'socket:join', (evt, data) ->
      socket.emit 'join', data

    $scope.$on '$destroy', () ->
      socket.disconnect()

    $scope.search = (q) ->
      $scope.$broadcast 'table:search', q

