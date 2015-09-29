angular
  .module 'tracker.grid'

  ## We have an endpoint that returns a cell/entity history, but that isn't especially
  ## useful for us directly. This service formats it into HTML that can be rendered
  ## in a tooltip. That means we can do formatting niceness.

  .factory 'searchInTable', () ->
    result =
      search: (table, query) ->
        result = table.search.query query
        table.render()

        if result.length > 0
          [first, rest...] = result
          table.selectCell first.row, first.col, first.row, first.col, true


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
