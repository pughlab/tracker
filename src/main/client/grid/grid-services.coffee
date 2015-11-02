angular
  .module 'tracker.grid'

  ## We have an endpoint that returns a cell/entity history, but that isn't especially
  ## useful for us directly. This service formats it into HTML that can be rendered
  ## in a tooltip. That means we can do formatting niceness.

  .factory 'searchInTable', () ->
    result =

      ## handles the main search
      search: (table, query) ->

        searchResult = table.search.query query
        table.render()
        table.trackerLastSearchResult = searchResult

        if searchResult.length > 0
          [first, rest...] = searchResult
          table.selectCell first.row, first.col, first.row, first.col, true


      ## handles the forwards and backwards navigation
      navigation: (table, direction) ->

        advanceCell = (obj, offset, rows, columns) ->
          obj.col = obj.col + offset
          if obj.col < 0 or obj.col >= columns
            obj.col = if obj.col < 0 then columns - 1 else 0
            obj.row = obj.row + offset
            if obj.row < 0 or obj.row >= rows
              obj.row = if obj.row < 0 then rows - 1 else 0
          obj

        equalsCell = (obj1, obj2) ->
          return obj1.row == obj2.row and obj1.col == obj2.col

        ## We should really use the htSearchResult and the "most recently selected"
        ## cell. One of the issues we face is that as soon as we use a navigation
        ## control we defocus the grid and erase the currently selected cell, so
        ## we no longer know where to start.

        offset = if direction == 'next' then 1 else -1

        searchResults = table.trackerLastSearchResult
        return if !searchResults == 'undefined' or searchResults.length == 0

        ## We can't actually assume that we're on a selected cell, because that
        ## would be far too easy.

        rows = table.countRows()
        columns = table.countCols()

        return if rows == 0 or columns == 0

        start = table.getSelected()
        if typeof start == 'undefined'
          start = {row: 0, col: 0}
        else
          start = {row: start[0], col: start[1]}
          advanceCell start, offset, rows, columns

        count = rows * columns
        while count-- > 0
          for cell in searchResults
            if equalsCell start, cell
              return table.selectCell cell.row, cell.col, cell.row, cell.col, true
          advanceCell start, offset, rows, columns


  .factory 'renderHistory', () ->

    return (history) ->

      formatEntry = (entry) ->
        if entry.event_type == 'set_value'
          oldValue = entry.args.old or 'N/A'
          newValue = entry.args.value or 'N/A'
          oldValue = "N/A" if typeof oldValue == 'object' and oldValue['$notAvailable']
          newValue = "N/A" if typeof newValue == 'object' and newValue['$notAvailable']
          "<div class='history-entry'>" +
          "<span class='history-username'>#{entry.event_user}</span>, " +
          "<span class='history-modified'>#{(new Date(entry.event_time)).toLocaleString()}</span>: " +
          "<span class='history-comment'>Changed from '#{oldValue}' to '#{newValue}'</span>" +
          "</div>"

      (formatEntry(entry) for entry in history).join("")


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
