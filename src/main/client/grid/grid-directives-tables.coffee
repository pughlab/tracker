angular
  .module 'tracker.grid'


  ## Started work on a datatables-based implementation of the grid. Initially, much of this
  ## can be hardwired for testing and embedding.

  .directive 'trackerTable', Array '$timeout', 'searchInTable', 'valueManager', 'booleanValueManager', 'addTableRecord', 'removeTableRecord', 'deleteCase', 'editTableCell', 'validateTableValue', 'reloadTable', \
                                   ($timeout, searchInTable, valueManager, booleanValueManager, addTableRecord, removeTableRecord, deleteCase, editTableCell, validateTableValue, reloadTable) ->
    result =
      restrict: "A"
      replace: true
      scope:
        study: '='
        view: '='
        attributes: '='
        editingStatus: '='
        permissions: '='
      template: '<div class="handsontable tracker-table-hidden" style="width: 800px; height: 500px; overflow: hidden;"></div>'

      link: (scope, iElement, iAttrs) ->

        scope.filters = {}

        scope.getStudyUrl = (scope) ->
          "/api/studies/#{scope.study.name}/views/#{scope.view.name}"

        handsonTable = undefined
        contextMenu = false
        userControllerScope = false

        handleStateCell = (entityIdentifier, state, editingClasses) ->
          rowIndex = handsonTable.trackerEntityRowTable[entityIdentifier]
          return if !rowIndex

          ## Tha labels are applied to the whole entity, so we need to update
          ## a complete row.

          handsonTable.setDataAtRowProp(rowIndex, '$state', state, 'socketEvent')


        ## Reloads the table data. This can be fired when the filters change,
        ## as well as when the initial table has been constructed.

        scope.$on 'table:reload', (e) ->
          console.log "Requesting reload", scope.filters
          e.stopPropagation?()
          reloadTable scope, handsonTable


        scope.$watchCollection 'filters', (newValue, oldValue) ->
          if oldValue != newValue
            scope.$emit 'table:reload', newValue


        scope.$on 'table:positionAtEnd', (e) ->
          e.stopPropagation?()
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
          e.stopPropagation?()
          searchInTable.search handsonTable, query

        scope.$on 'table:search-navigation', (e, direction) ->
          searchInTable.navigation handsonTable, direction


        scope.$on 'socket:welcome', (e, data) ->
          userControllerScope = e.targetScope
          if scope.study
            userControllerScope.$emit 'socket:join', scope.study.name, { "time" : (new Date()).valueOf() }


        scope.$watch 'study', (study) ->
          if userControllerScope
            userControllerScope.$emit 'socket:join', study.name, { "time" : (new Date()).valueOf() }


        scope.$watch 'attributes', (attributes, old) ->

          if attributes?

            ## Here we are notified of a property change, and should locate the cell,
            ## highlight it in some way, and arrange for a request for a more up-to-date
            ## value. Note that the value is never transmitted over the socket.

            scope.$on 'socket:state', (evt, original) ->
              if handsonTable != undefined
                handleStateCell original.data.parameters.case_id, original.data.parameters.state, original.data.editingClasses

            ## If we get a cell editing event, we need to identify the cell element, and then update
            ## the right stuff. We might need to do something similar for a row, too.

            scope.$on 'socket:field', (evt, original) ->
              if handsonTable != undefined and original.data.userNumber > 0
                editTableCell scope, handsonTable, original.data.parameters.case_id, original.data.parameters.field, original.data.editingClasses

            scope.$on 'socket:record', (evt, original) ->
              if handsonTable != undefined and original.data.userNumber > 0
                addTableRecord scope, handsonTable, original.data.parameters.case_id, original.data.editingClasses

            scope.$on 'socket:delete', (evt, original) ->
              if handsonTable != undefined and original.data.userNumber > 0
                removeTableRecord scope, handsonTable, original.data.parameters.case_id

            convertColumn = (attribute) ->
              result = {}
              result.data = valueManager(attribute.name)
              result.validator = (value, callback) ->
                validateTableValue(scope, @instance, @col, @row, value, callback)
              result.renderer = Handsontable.TrackerStringRenderer
              switch attribute.type
                when 'number'
                  result.type = 'numeric'
                  result.correctFormat = true
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

            rowHeaderLabel = (x) ->
              if x == 0
                "Filter"
              else
                "#{x}"

            orderedAttributes = pinnedAttributes.concat(otherAttributes)

            handsonTable = new Handsontable(iElement[0], {
              minSpareRows: 1
              colWidths: (getColWidth(a) for a in orderedAttributes)
              colHeaders: (a.label for a in orderedAttributes)
              rowHeaders: rowHeaderLabel
              columns: (convertColumn(a) for a in orderedAttributes)
              contextMenu: false
              multiSelect: true
              startCols: orderedAttributes.length
              fixedRowsTop: 1
              fixedColumnsLeft: pinnedAttributes.length
              columnSorting: false
              trackerColumnSorting: true
              manualColumnResize: true
              manualRowResize: true
              search: true
              dataSchema: () ->
                schema = {}
                for a in orderedAttributes
                  schema[a.name] = null
                schema
              currentRowClassName: 'currentRow'
              currentColClassName: 'currentCol'
              readOnly: ! (scope.editingStatus or false)
              cells: (row, col, prop) ->
                cellProperties = {}
                if row == 0
                  cellProperties.renderer = Handsontable.TrackerFilterRenderer
                  cellProperties.editor = 'text'
                cellProperties
              afterChange: (changes, source) ->
                return unless changes?
                for change in changes
                  if change? and change[0] == 0
                    scope.$apply () ->
                      if typeof change[3] == 'undefined'
                        delete scope.filters[change[1]]
                      else
                        scope.filters[change[1]] = change[3]
                    return
              outsideClickDeselects: false
            })

            handsonTable.trackerData = {
              stateLabels: scope.study.options?.stateLabels || {}
              typeTable: (attribute.type for attribute in orderedAttributes)
            }

            oldIsEmptyRow = handsonTable.isEmptyRow
            handsonTable.isEmptyRow = (row) ->
              rowCount = @countRows()
              if rowCount < 2
                false
              else
                oldIsEmptyRow(row)

            handsonTable.addHook 'beforeValidate', (value, row, fieldFunction, source) ->
              {"$value": value, "$source": source}

            ## Notify to load the initial table data
            scope.$emit 'table:reload'


        scope.$watch 'editingStatus', (editing, old) ->
          if handsonTable
            commands = {}
            if scope.permissions?.create
              commands['row_above'] = {name: 'Insert row above'}
              commands['row_below'] = {name: 'Insert row below'}
            if scope.permissions?.delete
              commands['row_delete'] = {
                name: 'Delete row',
                callback: (command, selection, evt) ->
                  start = selection.start.row
                  end = selection.end.row
                  for i in [start .. end] by 1
                    entityIdentifier = handsonTable.getSourceDataAtRow(i).id
                    deleteCase scope, handsonTable, entityIdentifier
              }
            handsonTable.updateSettings {
              readOnly: ! editing,
              contextMenu:
                callback: (key, options) ->

                items: if editing and Object.keys(commands).length > 0 then commands else false
            }


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
          if handsonTable?
            handsonTable.unlisten()
            handsonTable.destroy()
            handsonTable = undefined

          jQuery(window).off 'resize', resizeWrapper

          attributeColumnTable = undefined
          userControllerScope = false
