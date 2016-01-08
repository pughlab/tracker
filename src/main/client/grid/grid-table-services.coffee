angular
  .module 'tracker.grid'

  ## We have an endpoint that returns a cell/entity history, but that isn't especially
  ## useful for us directly. This service formats it into HTML that can be rendered
  ## in a tooltip. That means we can do formatting niceness.

  .factory 'valueManager', () ->
    return (name) ->
      (row, value) ->
        if !row?
          name
        else if value?
          row[name] =
            switch value
              when "" then null
              when "N/A" then {"$notAvailable": true}
              else value
        else
          current = row[name]
          if current == null or current == undefined
            ""
          else if current.hasOwnProperty('$notAvailable')
            "N/A"
          else
            current


  .factory 'booleanValueManager', () ->
    return (name) ->
      (row, value) ->
        if !row?
          name
        else if value?
          row[name] =
            switch value
              when "" then null
              when "N/A" then {"$notAvailable": true}
              when "Yes" then true
              when "No" then false
        else
          current = row[name]
          if current == null or current == undefined
            ""
          else if current.hasOwnProperty('$notAvailable')
            "N/A"
          else if current == false
            "No"
          else
            "Yes"


  .factory 'highlightElement', Array '$timeout', ($timeout) ->
    return (element, editingClasses) ->

      classes = editingClasses.split(' ')

      highlightOn = () ->
        for cls in classes
          Handsontable.Dom.addClass element, cls

        highlightOff = () ->
          for cls in classes
            Handsontable.Dom.removeClass element, cls

        $timeout highlightOff, 3000

      if element
        $timeout highlightOn, 100


  .factory 'deleteCase', Array '$http', '$timeout', 'removeTableRecord', ($http, $timeout, removeTableRecord) ->
    return (scope, handsonTable, entityIdentifier) ->
      $http
        .delete scope.getStudyUrl(scope) + "/entities/#{entityIdentifier}", {}
        .success (response) ->
          ## If we get here, we should remove the row.
          console.log "Got delete response", response
          removeTableRecord scope, handsonTable, entityIdentifier
        .error (response) ->
          console.log "Got delete error response", response


  .factory 'removeTableRecord', () ->
    return (scope, handsonTable, entityIdentifier) ->

      return if typeof entityIdentifier == 'undefined'

      rowIndex = handsonTable.trackerEntityRowTable[entityIdentifier]
      return if typeof rowIndex == 'undefined' or !rowIndex

      ## Now, how can we tell the original remove from a second one when we
      ## get a notified event back? Easy, remove it from the table as well

      handsonTable.alter('remove_row', rowIndex)

      ## Fix the row indexes
      delete handsonTable.trackerEntityRowTable[entityIdentifier]
      for own k, v of handsonTable.trackerEntityRowTable
        if v > rowIndex
          handsonTable.trackerEntityRowTable[k] = v - 1


  .factory 'addTableRecord', Array '$http', '$timeout', 'highlightElement', ($http, $timeout, highlightElement) ->
    return (scope, handsonTable, entityIdentifier, editingClasses) ->
      $http
        .get scope.getStudyUrl(scope) + "/entities/#{entityIdentifier}", {}
        .success (response) ->

          ## We don't have a row index, but we need to find the last (but one) row
          ## and insert after it. We'll also have to manage inserting all that data
          ## nicely. We'll also want to do a highlight trick on the row.

          record = response.entity

          ## Add in a new row
          totalRows = handsonTable.countRows()
          lastRow = totalRows - 2
          lastRow = 0 if lastRow < 0
          newRow = lastRow + 1

          totalCols = handsonTable.countCols()
          changes = []
          for i in [0..totalCols - 1]
            colData = handsonTable.getCellMeta(newRow, i)
            fieldName = colData.prop()
            holder = {}
            holder[fieldName] = record[fieldName]
            renderedValue = colData.prop(holder)
            changes.push [newRow, i, renderedValue]

          ## We can't really use populateFromArray, as it doesn't actually work when the
          ## grid is marked readOnly. So build a change set and use that instead.
          handsonTable.setDataAtCell changes, 'socketEvent'

          ## We also need to make sure that this row has the identifier set, which might
          ## not happen otherwise.
          handsonTable.getSourceDataAtRow(newRow).id = entityIdentifier

          cellElement = handsonTable.getCell newRow, 0
          rowElement = cellElement.parentNode
          highlightElement rowElement, editingClasses


  .factory 'handleStateCell', () ->
    return (scope, handsonTable, entityIdentifier, state, editingClasses) ->
      console.log "Calling handleStateCell", entityIdentifier
      rowIndex = handsonTable.trackerEntityRowTable[entityIdentifier]
      console.log "Current rowIndex", rowIndex
      return if !rowIndex

      for entry, i in handsonTable.sortIndex
        if entry[0] == rowIndex
          rowIndex = i
          break

      ## Tha labels are applied to the whole entity, so we need to update
      ## a complete row.

      handsonTable.setDataAtRowProp(rowIndex, '$state', state, 'socketEvent')


  .factory 'editTableCell', Array '$timeout', 'highlightElement', '$http', ($timeout, highlightElement, $http) ->
    return (scope, handsonTable, entityIdentifier, field, editingClasses) ->
      $http
        .get scope.getStudyUrl(scope) + "/entities/#{entityIdentifier}", {}
        .success (response) ->
          console.log 'response from editTableCell put', response
          columnIndex = handsonTable.trackerAttributeColumnTable[field]
          rowIndex = handsonTable.trackerEntityRowTable[entityIdentifier]
          console.log 'columnIndex', columnIndex, 'rowIndex', rowIndex
          return if !columnIndex or !rowIndex

          value = response.entity[field]
          colData = handsonTable.getCellMeta(rowIndex, columnIndex)

          holder = {}
          fieldName = colData.prop()
          holder[fieldName] = value
          renderedValue = colData.prop(holder)

          ## We should actually set to the converted value, not the internal value.
          ## Because that seems to be what's needed to make it all work.

          handsonTable.setDataAtCell(rowIndex, columnIndex, renderedValue, 'socketEvent')

          cellElement = handsonTable.getCell rowIndex, columnIndex
          highlightElement cellElement, editingClasses

        .error (response) ->
          console.log "Error", response



  ## Needs to find the case identifier, which requires a bit of poking around
  ## inside the raw data. Note that the validator is called before the writing
  ## logic, so the value manager (prop field) should be used to generate a real
  ## sendable value.

  .factory 'validateTableValue', Array '$timeout', 'highlightElement', '$http', ($timeout, highlightElement, $http) ->
    return (scope, handsonTable, col, row, value, cellProperties, callback) ->
      changeValue = value['$value']
      changeSource = value['$source']

      if changeSource == 'socketEvent'
        return callback true

      ## If the row doesn't have an id field, we're basically creating a new case, and
      ## let's just go ahead and do that. This probably best means dropping the requirement
      ## for an identifier, and for uniqueness of values, but then values are attached to
      ## values not directly to cases. Difference is, if we do a POST then we might get back
      ## an id, and we need to add that to the row data for future hackery.

      caseRecord = handsonTable.getSourceDataAtRow(row)
      fieldFunction = handsonTable.getCellMeta(row, col).prop
      fieldName = fieldFunction()
      fieldData = {}
      fieldFunction fieldData, changeValue

      caseIdentifier = caseRecord.id
      baseUrl = scope.getStudyUrl(scope)

      if ! caseIdentifier
        payload = {}
        payload[fieldName] = fieldData[fieldName]
        $http
          .post "#{baseUrl}/entities", JSON.stringify {entity: payload}
          .success (response) =>
            id = response.entity.id
            handsonTable.getSourceDataAtRow(row).id = id
            callback true
          .error (response) ->
            callback false

      else
        oldValue = handsonTable.getSourceDataAtRow(row)[fieldName]
        oldValue = null if oldValue == undefined
        value = fieldData[fieldName]
        value = null if value == undefined

        if angular.equals value, oldValue
          cellProperties.validationResponse = undefined
          return callback true

        payload = {value : fieldData[fieldName], oldValue: oldValue}
        cellProperties.validationData = payload

        payload = JSON.stringify payload
        $http
          .put "#{baseUrl}/entities/#{encodeURIComponent(caseIdentifier)}/#{encodeURIComponent(fieldName)}", payload
          .then (response) ->
            callback true
          .catch (response) ->
            callback false



  ## When reloading, we need to pass back any filters to the server, as these should be
  ## used to limit the data we get back.
  .factory 'reloadTable', Array '$http', ($http) ->
    return (scope, handsonTable) ->
      requestUrl = scope.getStudyUrl(scope)
      if scope.filters? and typeof scope.filters == 'object' and Object.keys(scope.filters).length > 0
        filters = encodeURIComponent(JSON.stringify(scope.filters))
        requestUrl = requestUrl + "?q=#{filters}"

      ## We don't want to remove the old filters, so we need to install them as new
      ## filters.

      $http
        .get requestUrl
        .success (response) ->
          newFilterRow = {id: -1, _filter_row: true}
          for own k, v of scope.filters
            newFilterRow[k] = v
          modified = [newFilterRow].concat(response.records)
          handsonTable.loadData(modified)

          ## We should really keep a track of the row information here, i.e., the association
          ## between identifier and row number. We can then use this to locate cells.
          ##
          ## Note, however, that these are virtual rows not real rows, and they can be translated
          ## to a different offset by the sorting system. Although that requires some access to that
          ## part of the API.

          entityRowTable = {}
          attributeColumnTable = {}
          for entity, i in response.records
            entityRowTable[entity.id] = i + 1
          for attribute, i in response.attributes
            attributeColumnTable[attribute.name] = i + 1

          handsonTable.trackerEntityRowTable = entityRowTable
          handsonTable.trackerAttributeColumnTable = attributeColumnTable

          ## This is where we have the initial load. Let's initiate a scroll down, but carefully
          scope.$emit 'table:positionAtEnd'
