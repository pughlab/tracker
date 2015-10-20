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


  .factory 'addTableRecord', Array '$timeout', 'highlightElement', ($timeout, highlightElement) ->
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



  .factory 'editTableCell', Array '$timeout', 'highlightElement', '$http', ($timeout, highlightElement, $http) ->
    return (scope, handsonTable, entityIdentifier, field, editingClasses) ->
      $http
        .get scope.getStudyUrl(scope) + "/entities/#{entityIdentifier}", {}
        .success (response) ->
          columnIndex = attributeColumnTable[field]
          rowIndex = entityRowTable[entityIdentifier]
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
    return (scope, handsonTable, col, row, value, callback) ->
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
          return callback true

        payload = JSON.stringify {value : fieldData[fieldName], oldValue: oldValue}
        $http
          .put "#{baseUrl}/entities/#{encodeURIComponent(caseIdentifier)}/#{encodeURIComponent(fieldName)}", payload
          .success (response) ->

            ## We should also get back an updated set of notes, and we need to make sure that general tags and
            ## field-specific notes are mirrored locally.

            ## caseRecord['$notes'] = response.records[0]['$notes']

            callback true
          .error (response) ->
            callback false
