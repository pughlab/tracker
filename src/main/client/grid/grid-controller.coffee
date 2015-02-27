angular
  .module 'tracker.grid'


  ## Controller to create a new record. This handles the dialog box that's used to 
  ## create a new record.
  .controller 'CreateNewRecordController', Array '$scope', '$http', '$modalInstance', '$stateParams', ($scope, $http, $modalInstance, $stateParams) ->

    $scope.ok = (identifier) ->

      ## Should also embed data from filtered rows, but we can actually do that server side :-)
      data = {}
      data[$scope.identifierAttribute] = identifier

      encodedStudyName = encodeURIComponent($stateParams.studyName)
      encodedViewName = encodeURIComponent($stateParams.viewName)

      $http
        .post("/api/studies/#{encodedStudyName}/views/#{encodedViewName}/record", data)
        .success (response) ->
          $modalInstance.close({identifier: identifier})
          $scope.$emit 'newEntity', {id: response.records[0].id}
        .error (response) ->
          $scope.$parent.message = response.err

    $scope.cancel = () ->
      $modalInstance.dismiss('cancel')


  ## Controller to handle the button used to start creating a new record. All it really does
  ## is pop up a modal attached to CreateNewRecordController
  .controller 'GridActionController', Array '$scope', '$modal', '$stateParams', ($scope, $modal, $stateParams) ->

    $scope.message = undefined
    $scope.identifier = undefined
    $scope.shown = false

    $scope.export = () ->
      location.href = "/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}?page=all&mimeType=application/vnd.ms-excel"

    $scope.createNewRecord = () ->

      $scope.message = undefined
      $scope.shown = false
      
      modal = $modal.open
        templateUrl: '/tracker/grid/new.html'
        scope: $scope
        controller: 'CreateNewRecordController'

      modal.opened.then () ->
        $scope.shown = true


  ## Main grid controller. 
  .controller 'GridController', Array '$scope', '$http', '$stateParams', '$timeout', 'socketFactory', 'gridCellFinder', 'gridColumnDefinitionGenerator', ($scope, $http, $stateParams, $timeout, socketFactory, gridCellFinder, gridColumnDefinitionGenerator) ->
    'use strict'

    $scope.studyName = $stateParams.studyName
    $scope.viewName = $stateParams.viewName
    $scope.viewOptions = undefined

    $scope.editingStatus = false

    $scope.errorMessage = ''

    $scope.filterText = ""

    ## Built-in filtering, which we don't use, we push to the server
    $scope.filterOptions =
      filterText: ""
      useExternalFilter: false

    $scope.sortOptions = null

    $scope.totalServerItems = 0

    $scope.identifierAttribute = undefined
    $scope.identifierAttributeLabel = undefined

    $scope.$watch 'editingStatus', (newValue, oldValue) ->
      if newValue != oldValue
        if newValue
          $scope.$broadcast 'grid:closeHistory'

    $scope.$on 'grid:scrollDetected', (e, original) ->
      if $scope.editingStatus
        e.targetScope.grid.cellNav.lastRowCol = null
        $scope.$broadcast 'uiGridEventCancelCellEdit'

    columnOptions =
      cellIsEditable: ($cellScope) ->
        return false unless $scope.editingStatus

        entity = $cellScope.row.entity
        field = $cellScope.col.colDef.name
        notes = $cellScope.row.entity.$notes

        return false if notes[field]?.locked
        return true

    $scope.setColumns = (columns) ->

      if $scope.viewOptions?.rows
        rows = $scope.viewOptions.rows
        columns = (col for col in columns when !rows.hasOwnProperty(col.name))

      defs = gridColumnDefinitionGenerator(columns, columnOptions)
      columnDefs = $scope.gridOptions.columnDefs
      columnDefs.splice(0, columnDefs.length)
      columnDefs.push.apply(columnDefs, defs)
      if ! $scope.$$phase
        $scope.$apply()

    $scope.gridData = []
    $scope.nextDataPage = 1
    $scope.dataPageSize = 50
    $scope.dataPageEnd = undefined
    $scope.permissions = undefined

    getDataRequest = (options) ->
      queryOptions = {}
      queryOptions.page ?= options?.page or $scope.nextDataPage
      queryOptions.pageSize ?= options?.pageSize or $scope.dataPageSize
      queryOptions.pages ?= options?.pages

      if $scope.filterText
        queryOptions.q = $scope.filterText.trim().toLowerCase()
        queryOptions.qf = $scope.identifierAttribute

      if $scope.sortOptions
        queryOptions.sort = $scope.sortOptions.field
        queryOptions.direction = $scope.sortOptions.direction

      $http
        .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}", {params: queryOptions})


    getData = (callback) ->
      getDataRequest()
        .success (response) ->
          if response.study.viewOptions
            $scope.viewOptions = response.study.viewOptions 
          $scope.identifierAttribute ?= response.study.identifierAttribute if response.study.identifierAttribute?
          $scope.identifierAttributeLabel ?= response.study.identifierAttributeLabel if response.study.identifierAttributeLabel?
          $scope.gridData.push.apply($scope.gridData, response.records)
          $scope.totalServerItems ?= response.counts.total
          $scope.nextDataPage = $scope.nextDataPage + 1
          $scope.dataPageEnd ?= Math.ceil(response.counts.total / $scope.dataPageSize)
          $scope.permissions = response.permissions

          $scope.setColumns response.attributes if $scope.gridOptions.columnDefs.length == 0
          callback(null)

        .error (response) ->
          callback(response.err)

    getData (err) -> console.log "Error", err if err?

    filterTextTimer = false
    $scope.$watch 'filterText', (newFilter, oldFilter) ->

      handleFilterTextChange = () ->
        if newFilter != oldFilter
          $scope.gridData.splice(0, $scope.gridData.length)
          $scope.nextDataPage = 1
          $scope.dataPageEnd = undefined
          getData (err) -> console.log "Error", err if err?

      if filterTextTimer
        $timeout.cancel(filterTextTimer)
      filterTextTimer = $timeout handleFilterTextChange, 500


    $scope.$watch 'sortOptions', (newOptions, oldOptions) ->
      if newOptions != oldOptions
        $scope.nextDataPage = 1
        $scope.gridData.splice(0, $scope.gridData.length)
        getData (err) -> console.log "Error", err if err?

 
    $scope.gridOptions =
      data: $scope.gridData
      columnDefs: []
      enableColumnMenus: false
      headerTemplate: '/tracker/grid/header.html'
      rowTemplate: '/tracker/grid/row.html'
      enableCellEdit: true
      enableCellEditOnFocus: true
      enableSorting: true
      useExternalSorting: true
      headerRowHeight: 87
      enableInfiniteScroll: true
      infiniteScrollPercentage: 15
      enableHorizontalScrollbar: true

    $scope.gridOptions.onRegisterApi = (gridApi) ->
      $scope.gridApi = gridApi

      gridApi.core.on.sortChanged $scope, (grid, sortColumns) ->

        ## So we know the current size of the data set, and we know the new sorting routine/direction. 
        ## We can re-issue a data request and update the data. 

        if sortColumns.length > 0
          $scope.sortOptions = {field: sortColumns[0].name, direction: sortColumns[0].sort.direction}
        else 
          $scope.sortOptions = null

      gridApi.infiniteScroll?.on?.needLoadMoreData $scope, () ->
        if $scope.nextDataPage <= $scope.dataPageEnd
          getData (err) -> 
            gridApi.infiniteScroll.dataLoaded()
            console.log "Error", err if err?

      gridApi.edit?.on?.afterCellEdit $scope, (entity, colDef, newValue, oldValue) ->
        field = colDef.field
        if newValue != oldValue
          copiedValue = JSON.stringify {value : entity[colDef.field]}
          $http
            .put("/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}/entities/#{entity.id}/#{colDef.field}", copiedValue)
            .success (response) ->
              $scope.errorMessage = undefined
            .error (response) ->
              entity[colDef.field] = oldValue
              $scope.errorMessage = response.err

      gridApi.cellNav?.on?.navigate $scope, (newRowCol, oldRowCol) ->

        ## Given a reference, we need to find the element. Or do we?
        if newRowCol?.row?.entity != oldRowCol?.row?.entity
          $scope.$apply () ->
            oldRowCol.row.entity.selected = false if oldRowCol?.row?.entity
            newRowCol.row.entity.selected = true if newRowCol?.row?.entity


    ## Now we ought to connect to the server using WebSockets. This notifies the server that we 
    ## are tracking a given study. We also need to handle the destroy to close down the socket
    ## properly. 

    $scope.$on 'newEntity', (evt, data) ->
      updateNewRecord data.id

    $scope.currentUsers = []

    ## Yuk! This URL is hard-coded and matters, because the gulp-proxy fails to actually do the
    ## right thing.

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


    updateNewRecord = (entityIdentifier) ->
      getDataRequest({page: 1, pages: $scope.nextDataPage - 1})
        .success (response) ->

          ## It is logically possible that every single record here doesn't match the current
          ## view. That would be.... unfortunate. 
          ##
          ## However, we still need to handle it. Really, we need a mini-diff to work out the minimal
          ## changes we can make to the grid. However, first of all, if we don't find the new record
          ## we don't actually need to do anything at all.

          index = null
          for record, i in response.records
            if record.id == entityIdentifier
              index = i
              break

          return if ! index?

          ## So now we know the record does exist and should be visible. So we can add that entity
          ## to the grid dynamically. 

          $scope.gridData.splice(index, 0, response.records[index])

          expectedEnd = ($scope.nextDataPage - 1) * $scope.dataPageSize
          if $scope.gridData.length > expectedEnd
            $scope.gridData.splice expectedEnd, $scope.gridData.length - expectedEnd

          if ! $scope.$$phase
            $scope.$apply()

          $timeout (() -> $scope.$broadcast 'gridAdjusted', {}), 100

    ## We can't just request a refresh any more, because the redraws the whole darned thing. We 
    ## really want to keep the scroll position, so we should just update the given cell value. 
    ## However, we don't pass the information as to the new cell contents because security would
    ## be broken. Instead, we should allow the client to request and individual element field
    ## value. 

    ## It is possible that the user is an API user, in which case there won't be a linked 
    ## connection. If we can't find the user, and it's not us, then we should temporarily add a 
    ## user and use them instead. Remembering to remove them after we're done. 
    socket.on 'gridEdit', (data) ->

      userNumber = $scope.currentUsers.indexOf(data.user)
      if userNumber != -1
        editingClasses = "editedCellText editedCellUser-#{userNumber}"

      entityIdentifier = parseInt(data.params.id)

      cellElement = undefined

      highlightOff = () ->
        console.log "Removing class", cellElement, editingClasses
        cellElement.removeClass(editingClasses)

      highlightOn = (entityIdentifier, field) ->
        () ->
          cellElement = gridCellFinder($scope.gridApi.grid, entityIdentifier, field)
          cellElement.addClass(editingClasses)
          $timeout highlightOff, 3000

      highlight = (entityIdentifier, field) ->
        $timeout highlightOn(entityIdentifier, field), 200

      ## If we get a cell editing event, we need to identify the cell element, and then update
      ## the right stuff. We might need to do something similar for a row, too. 
      if data.type == 'cell'

        if userNumber != -1
          highlight entityIdentifier, data.params.field

        $http
          .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/views/#{encodeURIComponent($stateParams.viewName)}/entities/#{entityIdentifier}", {})

          .success (response) ->
            newEntity = response.records[0]

            for gridRow in $scope.gridApi.grid.rows
              if gridRow.entity.id == entityIdentifier
                gridRow.entity[data.params.field] = newEntity[data.params.field]
                break

          .error (response) ->
            console.log "Error", response

      else if data.type == 'record'

        ## We should be given a new identifier. We can then do a request as per the original data
        ## request, and when we get data, we merge it with the data records. We do assume that
        ## nothing apart from the current referred record is changing. 
        updateNewRecord entityIdentifier

        ## Called here, we know it's an external event, so we can/should do the highlight thing if
        ## we can. 
        if userNumber != -1
          highlight entityIdentifier, $scope.identifierAttribute


    socket.on 'welcome', (data) ->
      socket.emit 'join', { 'scope': $scope.studyName }

    $scope.$on '$destroy', () ->
      socket.disconnect()

