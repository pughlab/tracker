angular
  .module 'tracker.grid'

  ## We have an endpoint that returns a cell/entity history, but that isn't especially
  ## useful for us directly. This service formats it into HTML that can be rendered
  ## in a tooltip. That means we can do formatting niceness. 


  .factory 'renderHistory', () ->

    return (history) ->

      previousValue = undefined
      for entry in history by -1
        entry.oldValue = previousValue or 'blank'

        formatted = entry.value
        formatted = 'N/A' if typeof formatted == 'object' && formatted?['$notAvailable']
        formatted = 'Yes' if formatted == true
        formatted = 'No' if formatted == false
        formatted = 'blank' if formatted == null
        entry.value = formatted
        previousValue = formatted

      formatEntry = (entry) ->
        oldValue = entry.oldValue or 'blank'
        newValue = entry.value or 'blank'
        "<div class='history-entry'>" +
        "<span class='history-username'>#{entry.username}</span>, " +
        "<span class='history-modified'>#{(new Date(entry.modified)).toLocaleString()}</span>: " + 
        "<span class='history-comment'>Changed from '#{oldValue}' to '#{newValue}'</span>" +
        "</div>"

      (formatEntry(entry) for entry in history).join("")


  .factory 'gridCellFinder', () ->

    (grid, entityId, field) ->
      canvas = grid.element
      rowElement = canvas.find('.t-entity-' + entityId)
      cellElement = rowElement.find('.t-field-' + field)


  .factory 'gridRowFinder', () ->

    (grid, entityId) ->
      canvas = grid.element
      rowElement = canvas.find('.t-entity-' + entityId)


  .factory 'gridColumnDefinitionGenerator', () ->

    (columns, options) ->
      getColumnDef = (column) ->

        cellClassesFunction = (grid, row, col) ->
          classes = column.options?.classes
          classes ?= []
          classes.concat("t-field-#{col.field}").join(" ")

        headerClassesFunction = (row, rowRenderIndex, col, colRenderIndex) ->
          classes = column.options?.classes
          classes ?= []
          classes.join(" ")

        result =
          "name" : column.name
          "field" : column.name
          "displayName" : column.label
          "width" : 110
          "enableCellEdit": true
          "headerCellTemplate": '/tracker/grid/header-cell.html'
          "cellEditableCondition": (options.cellIsEditable or true)
          "cellClass": cellClassesFunction
          "headerCellClass": headerClassesFunction

        if column.type == 'boolean'
          result['cellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-display' ng-class='col.colIndex()' cell-boolean cell-data='row.entity.#{column.name}' cell-notes='row.entity.$notes.#{column.name}'></div>"
          result['editableCellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-editing' editable-cell-boolean cell-data='row.entity.#{column.name}'></div>"
        else if column.type == 'date'
          result['cellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-display' ng-class='col.colIndex()' cell-date cell-data='row.entity.#{column.name}' cell-notes='row.entity.$notes.#{column.name}'></div>"
          result['editableCellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-editing' editable-cell-date cell-data='row.entity.#{column.name}'></div>"
        else if column.type == 'option'
          result['options'] = column.options?.values
          result['cellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-display' ng-class='col.colIndex()' cell-option cell-data='row.entity.#{column.name}' cell-notes='row.entity.$notes.#{column.name}'>{{row.entity.#{column.name}}}</div>"
          result['editableCellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-editing' editable-cell-option cell-data='row.entity.#{column.name}' cell-options='col.colDef.options'></div>"
        else if column.type == 'string'
          result['options'] = column.options
          result['cellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-display' ng-class='col.colIndex()' cell-string cell-data='row.entity.#{column.name}' cell-options='col.colDef.options' cell-notes='row.entity.$notes.#{column.name}'></div>"
          result['editableCellTemplate'] = "<div class='ui-grid-cell-contents tracker-cell-editing' editable-cell-string cell-data='row.entity.#{column.name}' cell-options='col.colDef.options'></div>"

        ## These break a lot of stuff - positioning gets mangled and tooltips appear to interact with issue #2        
        # if column.options?.display == 'pin_left'
        #   result.pinnedLeft = true
        # else if column.options?.display == 'pin_right'
        #   result.pinnedRight = true

        result

      return (getColumnDef(column) for column in columns)


  .factory 'gridCellHistoryProvider', Array '$timeout', '$tooltip', '$position', '$document', '$http', '$stateParams', 'gridCellFinder', 'renderHistory', ($timeout, $tooltip, $position, $document, $http, $stateParams, gridCellFinder, renderHistory) ->

    promise = null
    tooltipValues = null
    tooltipElement = null

    removeHistory = () ->
      tooltipElement.remove()
      tooltipElement = null

    showHistory = () ->

      scope = tooltipValues.scope

      if tooltipElement
        removeHistory()

      encodedStudyName = encodeURIComponent($stateParams.studyName)
      encodedViewName = encodeURIComponent($stateParams.viewName)
      entityId = scope.row.entity.id
      field = scope.col.colDef.field

      $http
        .get("/api/studies/#{encodedStudyName}/views/#{encodedViewName}/entities/#{entityId}/#{field}")
        .success (response) ->

          if response.history?.length
            content = renderHistory(response.history)
  
            cellElement = gridCellFinder scope.row.grid, scope.row.entity.id, scope.col.colDef.field
            
            template = '<div class="popover right fade history-popover">' +
                       '  <div class="arrow"></div>' +
                       '  <div class="popover-inner">' + 
                       '    <div class="popover-content history-popover-content scrollable">' + content + '</div>' +
                       '  </div>' +
                       '</div>'
  
            tooltipElement = angular.element(template)
  
            positionTooltip = () ->
              ttPosition = $position.positionElements(cellElement, tooltipElement, "right", true)

              if ttPosition.left > jQuery(":root").width() - 250
                tooltipElement.removeClass('right')
                tooltipElement.addClass('left')
                ttPosition = $position.positionElements(cellElement, tooltipElement, "left", true)

              ttPosition.top += 'px'
              ttPosition.left += 'px'
              tooltipElement.css ttPosition
  
            $document.find('body').append(tooltipElement)
            positionTooltip()
            tooltipElement.addClass 'in'

    result =
      enter: (values) ->
        if promise
          $timeout.cancel(promise)
          promise = null
        tooltipValues = values
        promise = $timeout (() -> showHistory()), 1000
      leave: (values) ->
        if promise
          $timeout.cancel(promise)
          promise = null
        if tooltipElement
          removeHistory()
