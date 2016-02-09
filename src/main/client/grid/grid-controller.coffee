angular
  .module 'tracker.grid'

  .controller 'PageViewController', Array '$scope', ($scope) ->

    $scope.alerts = []
    $scope.page = {}

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.submit = () ->
      $scope.alerts.push {type: 'success', message: "Pressed submit OK"}


  ## Controller to handle the button used to export a record.
  .controller 'GridActionController', Array '$scope', '$modal', '$stateParams', ($scope, $modal, $stateParams) ->

    $scope.message = undefined
    $scope.identifier = undefined
    $scope.shown = false

    $scope.export = () ->
      location.href = "/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}?page=all&media=xml"


  ## Controller to handle connected users
  .controller 'ConnectedUserController', Array '$scope', 'socketFactory', ($scope, socketFactory) ->
    $scope.currentUsers = ["system"]

    socket = socketFactory()

    socket.on 'userconnect', (event) ->
      console.log 'Got userconnect event', event.data.user
      $scope.$apply () ->
        if not (event.data.user in $scope.currentUsers)
          $scope.currentUsers.push event.data.user

    socket.on 'userdisconnect', (event) ->
      $scope.$apply () ->
        newUsers = ["system"].concat(user for user in $scope.currentUsers when user != event.data.user and user != "system")
        $scope.currentUsers = newUsers

    ## If we get a disconnect, we forget all users. We probably should also generate an alert to let people
    ## know that we are currently locked.
    socket.on 'disconnect', (e) ->
      $scope.$evalAsync () ->
        $scope.currentUsers = ["system"]

    socket.on 'welcome', (event) ->
      $scope.currentUsers.push event.data.user
      $scope.$broadcast 'socket:welcome', event

    annotateEventWithUser = (event) ->
      event.data.userNumber = $scope.currentUsers.indexOf(event.data.user)
      if event.data.userNumber > 0
        event.data.editingClasses = "editedCellText editedCellUser-#{event.data.userNumber}"

    socket.on 'state', (event) ->
      annotateEventWithUser event
      $scope.$broadcast 'socket:state', event

    socket.on 'field', (event) ->
      annotateEventWithUser event
      $scope.$broadcast 'socket:field', event

    socket.on 'record', (event) ->
      annotateEventWithUser event
      $scope.$broadcast 'socket:record', event

    socket.on 'delete', (event) ->
      annotateEventWithUser event
      $scope.$broadcast 'socket:delete', event

    $scope.$on 'socket:join', (evt, scope, event) ->
      socket.emit 'join', scope, event

    $scope.$on '$destroy', () ->
      socket.disconnect()
