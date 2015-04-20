angular
  .module 'tracker.grid'


  .directive 'trackerSearchButton', () ->

    result =
      restrict: "A"
      replace: false
      link: (scope, iElement, iAttrs) ->

        button = angular.element("<a role='button' class='clear-button' aria-label='Clear'><span class='glyphicon glyphicon-remove-circle'></span></a>")
        iElement.after button

        button.on 'click', (e) ->
          iElement.val("")


  ## Started work on a datatables-based implementation of the grid. Initially, much of this
  ## can be hardwired for testing and embedding.

  .directive 'trackerTable', Array '$http', '$timeout', ($http, $timeout) ->

    highlightElement = (element, editingClasses) ->

      highlightOn = () ->
        classes = editingClasses.split(' ')
        for cls in classes
          Handsontable.Dom.addClass element, cls

        highlightOff = () ->
          for cls in classes
            Handsontable.Dom.removeClass element, cls

        $timeout highlightOff, 3000

      if element
        $timeout highlightOn, 100


    booleanValueManager = (name) ->
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


    valueManager = (name) ->
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


    result =
      restrict: "A"
      replace: true
      scope: 
        trackerStudy: '='
        trackerAttributes: '='
        trackerEditingStatus: '='
      template: '<div class="handsontable tracker-table-hidden" style="width: 800px; height: 500px; overflow: hidden;"></div>'
      link: (scope, iElement, iAttrs) ->

        getStudyUrl = (study) ->
          "/api/studies/#{study.name}/views/#{study.viewName}"

        handsonTable = undefined
        entityRowTable = undefined
        attributeColumnTable = undefined
        contextMenu = false
        userControllerScope = false

        handleAddRecord = (entityIdentifier, editingClasses) ->
          $http
            .get getStudyUrl(scope.trackerStudy) + "/entities/#{entityIdentifier}", {}
            .success (response) ->
              
              ## We don't have a row index, but we need to find the last (but one) row
              ## and insert after it. We'll also have to manage inserting all that data
              ## nicely. We'll also want to do a highlight trick on the row. 

              record = response.records[0]

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


        handleEditCell = (entityIdentifier, field, editingClasses) ->
          $http
            .get getStudyUrl(scope.trackerStudy) + "/entities/#{entityIdentifier}", {}
            .success (response) ->
              columnIndex = attributeColumnTable[field]
              rowIndex = entityRowTable[entityIdentifier]
              return if !columnIndex or !rowIndex

              value = response.records[0][field]
              colData = handsonTable.getCellMeta(rowIndex, columnIndex)

              holder = {}
              fieldName = colData.prop()
              holder[fieldName] = value
              renderedValue = colData.prop(holder)

              ## We should actually set to the converted value, not the internal value. 
              ## Because that seems to be what's needed to make it all work. 

              handsonTable.setDataAtCell(rowIndex, columnIndex, renderedValue, 'socketEvent');

              cellElement = handsonTable.getCell rowIndex, columnIndex
              highlightElement cellElement, editingClasses

            .error (response) ->
              console.log "Error", response


        scope.$on 'table:positionAtEnd', (e) ->

          offset = Handsontable.Dom.offset(iElement[0])
          availableWidth = Handsontable.Dom.innerWidth(document.body) - offset.left + window.scrollX - 46
          availableHeight = Handsontable.Dom.innerHeight(document.body) - offset.top + window.scrollY - 100

          iElement[0].style.width = availableWidth + 'px'
          iElement[0].style.height = availableHeight + 'px'
          handsonTable.render()

          totalRows = handsonTable.countRows()
          lastRow = totalRows - 1
          lastRow = 0 if lastRow < 0

          handsonTable.selectCell(lastRow, 0, lastRow, 0, true)
          handsonTable.deselectCell()

          iElement.removeClass("tracker-table-hidden")

        ## Basic search function. When we get a result, we can choose how to handle it, either
        ## as a selection or as a display. We should somehow make it easy to scroll right to 
        ## a highlighted selected cell. 

        scope.$on 'table:search', (e, query) ->
          result = handsonTable.search.query query
          handsonTable.render()

          [first, rest...] = result
          handsonTable.selectCell first.row, first.col, first.row, first.col, true


        scope.$on 'socket:welcome', (evt, data) ->
          userControllerScope = evt.targetScope
          if scope.trackerStudy 
            userControllerScope.$emit 'socket:join', { 'scope': scope.trackerStudy }


        scope.$watch 'trackerStudy', (study) ->
          if userControllerScope
            userControllerScope.$emit 'socket:join', { 'scope': study.name }


        scope.$watch 'trackerAttributes', (attributes, old) ->

          if attributes?

            ## Here we are notified of a property change, and should locate the cell,
            ## highlight it in some way, and arrange for a request for a more up-to-date
            ## value. Note that the value is never transmitted over the socket. 

            scope.$on 'socket:entityPropertyChange', (evt, data) ->

              ## If we get a cell editing event, we need to identify the cell element, and then update
              ## the right stuff. We might need to do something similar for a row, too. 
              if handsonTable != undefined and data.type == 'cell' and data.userNumber != -1

                handleEditCell data.params.id, data.params.field, data.editingClasses

              else if handsonTable != undefined and data.type == 'record' and data.userNumber != -1

                handleAddRecord data.params.id, data.editingClasses


            ## Needs to find the case identifier, which requires a bit of poking around
            ## inside the raw data. Note that the validator is called before the writing
            ## logic, so the value manager (prop field) should be used to generate a real
            ## sendable value.

            validator = (value, callback) ->
              changeValue = value['$value']
              changeSource = value['$source']

              if changeSource == 'socketEvent'
                return callback true

              ## If the row doesn't have an id field, we're basically creating a new case, and
              ## let's just go ahead and do that. This probably best means dropping the requirement
              ## for an identifier, and for uniqueness of values, but then values are attached to
              ## values not directly to cases. Difference is, if we do a POST then we might get back
              ## an id, and we need to add that to the row data for future hackery.

              caseRecord = @instance.getSourceDataAtRow(@row)
              fieldFunction = @instance.getCellMeta(@row, @col).prop
              fieldName = fieldFunction()
              fieldData = {}
              fieldFunction fieldData, changeValue

              caseIdentifier = caseRecord.id
              baseUrl = getStudyUrl(scope.trackerStudy)

              if ! caseIdentifier
                payload = {}
                payload[fieldName] = fieldData[fieldName]
                payload = JSON.stringify payload
                $http
                  .post "#{baseUrl}/record", payload
                  .success (response) =>
                    id = response.records[0].id
                    @instance.getSourceDataAtRow(@row).id = id
                    callback true
                  .error (response) ->
                    callback false

              else
                payload = JSON.stringify {value : fieldData[fieldName]}
                $http
                  .put "#{baseUrl}/entities/#{encodeURIComponent(caseIdentifier)}/#{encodeURIComponent(fieldName)}", payload
                  .success (response) ->

                    ## We should also get back an updated set of notes, and we need to make sure that general tags and 
                    ## field-specific notes are mirrored locally. 

                    caseRecord['$notes'] = response.records[0]['$notes']

                    callback true
                  .error (response) ->
                    callback false


            convertColumn = (attribute) ->
              result = {}
              result.data = valueManager(attribute.name)
              result.validator = validator
              result.renderer = Handsontable.TrackerStringRenderer
              switch attribute.type
                when 'date'
                  result.type = 'date'
                  result.dateFormat = 'YYYY-MM-DD'
                  result.correctFormat = true
                  result.editor = Handsontable.editors.TrackerDateEditor
                when 'boolean'
                  result.type = 'dropdown'
                  result.source = ['Yes', 'No', 'N/A']
                  result.strict = true
                  result.allowInvalid = false
                  result.filter = false
                  result.renderer = Handsontable.TrackerOptionRenderer
                  result.data = booleanValueManager(attribute.name)
                when 'option'
                  result.type = 'dropdown'
                  result.source = attribute.options.values.concat("N/A")
                  result.strict = true
                  result.allowInvalid = false
                  result.filter = false
                  result.renderer = Handsontable.TrackerOptionRenderer

              result

            ## Distressingly, we have to turn off column sorting because there is essentially
            ## zero modularity, and we need a better handling of column sorting than the standard
            ## plugin applies. This leaves hooks, and even code, embedded, but we can't really 
            ## worry about that here. 

            baseColWidth = 100
            getColWidth = (attribute) ->
              width = attribute.options?.width
              pattern = /(\d+)%$/
              match = undefined
              if width and (match = pattern.exec(width))
                width = (parseInt(match[1]) / 100) * baseColWidth
                width + "pt"
              else
                baseColWidth + "pt"

            pinnedAttributes = []
            otherAttributes = []
            for attribute in attributes
              if attribute.options?.pinned
                pinnedAttributes.push attribute
              else
                otherAttributes.push attribute

            orderedAttributes = pinnedAttributes.concat(otherAttributes)
            
            handsonTable = new Handsontable(iElement[0], {
              minSpareRows: 1
              colWidths: (getColWidth(a) for a in orderedAttributes)
              colHeaders: (a.label for a in orderedAttributes)
              columns: (convertColumn(a) for a in orderedAttributes)
              contextMenu: false
              multiSelect: false
              startCols: orderedAttributes.length
              fixedRowsTop: 0
              fixedColumnsLeft: pinnedAttributes.length
              columnSorting: false
              trackerColumnSorting: true
              manualColumnResize: true
              search: true
              dataSchema: () ->
                schema = {}
                for a in orderedAttributes
                  schema[a.name] = null
                schema
              currentRowClassName: 'currentRow'
              currentColClassName: 'currentCol'
              readOnly: ! (scope.trackerEditingStatus or false)
            })

            handsonTable.addHook 'beforeValidate', (value, row, fieldFunction, source) ->
              {"$value": value, "$source": source}

            ## If we have a "status" attribute, we should add a context handler, with options 
            ## for the different values, all of which modify the record value with that particular
            ## value. We need to poke around in the data for that. 

            addStatusContext = undefined
            for attribute in attributes
              if attribute.name == 'status' and attribute.type == 'option'
                addStatusContext = attribute
                break

            handleStatusChange = (instance, row, status) ->
              instance.setDataAtCell row, attributeColumnTable['status'], status

            updateContextMenu = () ->
              attribute = attributeColumnTable?['status']?
              if attribute?
                attribute = attributes[attributeColumnTable?['status']]
              if attribute and attribute.type == 'option'

                options = {}
                for value in attribute.options.values
                  options[value] = "Mark record as #{value}"

                contextMenu =  {
                  callback: (key, options) ->
                    self = @
                    handler = () -> handleStatusChange self, options.start.row, key
                    setTimeout handler, 100
                  items: options
                }

                handsonTable.updateSettings { contextMenu: (if scope.editing then contextMenu else false) }

              else
                handsonTable.updateSettings { contextMenu: false }

            # Can actually cancel the change by returning false, or true to accept it
            # Of course, this doesn't use a callback, so it's somewhat less helpful for
            # asynchronous validation. 

            $http
              .get getStudyUrl(scope.trackerStudy)
              .success (response) ->
                handsonTable.loadData(response.records)

                ## We should really keep a track of the row information here, i.e., the association
                ## between identifier and row number. We can then use this to locate cells. 
                ##
                ## Note, however, that these are virtual rows not real rows, and they can be translated
                ## to a different offset by the sorting system. Although that requires some access to that
                ## part of the API.

                entityRowTable = {}
                attributeColumnTable = {}
                for entity, i in response.records        
                  entityRowTable[entity.id] = i
                for attribute, i in response.attributes
                  attributeColumnTable[attribute.name] = i

                updateContextMenu()

                ## This is where we have the initial load. Let's initiate a scroll down, but carefully
                scope.$emit 'table:positionAtEnd'


        scope.$watch 'trackerEditingStatus', (editing, old) ->
          if handsonTable
            handsonTable.updateSettings {readOnly: ! editing, contextMenu: (if editing then contextMenu else false)}


        rtime = undefined
        timeout = false
        delta = 200

        resizeHandler = (e) ->
          if new Date() - rtime < delta
            setTimeout resizeHandler, delta
          else
            timeout = false

            offset = Handsontable.Dom.offset(iElement[0])
            availableWidth = Handsontable.Dom.innerWidth(document.body) - offset.left + window.scrollX - 32
            availableHeight = Handsontable.Dom.innerHeight(document.body) - offset.top + window.scrollY - 100

            iElement[0].style.width = availableWidth + 'px'
            iElement[0].style.height = availableHeight + 'px'
            handsonTable.render()

        resizeWrapper = () ->
          rtime = new Date()
          if timeout == false
            timeout = true
            setTimeout resizeHandler, delta

        jQuery(window).on 'resize', resizeWrapper

        scope.$on '$destroy', (evt) ->
          handsonTable.unlisten()
          handsonTable.destroy()          
          handsonTable = undefined
          jQuery(window).off 'resize', resizeWrapper       
          
          entityRowTable = undefined
          attributeColumnTable = undefined
          userControllerScope = false