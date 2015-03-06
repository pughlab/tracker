angular
  .module 'tracker.grid'

  .directive 'uiGridViewport', () ->
    result =
      restrict: "A"
      replace: false
      priority: -500
      link: 
        post: ($scope, iElement, iAttrs) ->
          iElement.addClass 'scrollable'
          iElement.on 'scroll', (e) ->
            $scope.$emit 'grid:scrollDetected'
            $scope.$broadcast 'grid:closeHistory'


  .directive 'gridCellHistory', Array '$timeout', 'gridCellHistoryProvider', ($timeout, gridCellHistoryProvider) ->
    result =
      restrict: "A"
      replace: false
      priority: 200
      link: ($scope, iElement, iAttrs) ->

        iElement.find('div').on 'click', (e) -> 
          gridCellHistoryProvider.enter {scope: $scope}

          if iElement.find('.tracker-cell-display').length > 0
            iElement.on 'mouseleave', (evt) ->
              $timeout () ->
                $scope.$emit 'grid:closeHistory'

        $scope.$on 'grid:closeHistory', () ->
          gridCellHistoryProvider.leave {scope: $scope}


  .directive 'cellNotes', () ->
    result =
      restrict: "A"
      replace: false
      scope: false
      link: (scope, iElement, iAttrs) ->
        expression = iAttrs['cellNotes']
        scope.$watch expression, (newValue, oldValue) ->
          tags = newValue?.tags
          for tag in tags or []
            iElement.toggleClass tag


  .directive 'cellString', Array '$timeout', ($timeout) ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
        cellOptions: '='
      link: (scope, iElement, iAttrs) ->

        longText = false
        textValue = undefined

        scope.$watch 'cellOptions', (newValue) ->
          if newValue?.longtext?
            longText = newValue.longtext

        scope.$watch 'cellData', (newValue, oldValue) ->
          if typeof newValue == 'object' && newValue?['$notAvailable']
            textValue = 'N/A'
          else if ! newValue?
            textValue = ''
          else 
            textValue = newValue
          iElement.text(textValue)

        iElement.on 'click', (e) ->
          if longText
            element = jQuery(iElement).textdropdown({readonly: true})
            jQuery(iElement).textdropdown('setText', textValue)

            jQuery(iElement).on 'keyup.textdropdown', (e) ->
              originalEvent = e.originalEvent
              if originalEvent.keyCode == 27
                e.stopPropagation()
                e.preventDefault()
                jQuery(iElement).textdropdown('hideWidget')

            openFn = () ->
              jQuery(iElement).textdropdown('showWidget')

            $timeout openFn, 50, false


  .directive 'cellOption', () ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
      link: (scope, iElement, iAttrs) ->
        scope.$watch 'cellData', (newValue, oldValue) ->
          if typeof newValue == 'object' && newValue?['$notAvailable']
            iElement.text('N/A')
          else if ! newValue?
            iElement.text('')
          else 
            iElement.text(newValue)


  .directive 'editableCellString', Array '$timeout', ($timeout) ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
        cellOptions: '='
      template: "<input type='text' class='form-control input-padding-override'>"
      link: (scope, iElement, iAttrs) ->

        scope.$watch 'cellData', (newValue, oldValue) ->

          longtext = scope.cellOptions?.longtext
          initializing = newValue == oldValue

          if ! newValue?
            iElement.val('')
          else 
            iElement.val(newValue)

          ## Only initialize once.
          return if ! initializing
          if longtext
            jQuery(iElement).textdropdown()

            jQuery(iElement).on 'hide.textdropdown', (e) ->
              scope.$apply () ->
                scope.cellData = e.text or null
              scope.$emit 'uiGridEventEndCellEdit'

            jQuery(iElement).on 'keyup.textdropdown', (e) ->
              originalEvent = e.originalEvent
              if originalEvent.keyCode == 27
                e.stopPropagation()
                e.preventDefault()
                jQuery(iElement).textdropdown('hideWidget')

            openFn = () ->
              jQuery(iElement).textdropdown('showWidget')
            $timeout openFn, 50, false

          else
            iElement.on 'blur', (e) ->
              scope.$emit 'uiGridEventEndCellEdit'

            iElement.on 'change', (e) ->
              scope.$apply () ->
                scope.cellData = e.target.value or null
              scope.$emit 'uiGridEventEndCellEdit'

            iElement.on 'keydown', (e) ->
              if e.keyCode == 27
                e.stopPropagation()
                e.preventDefault()
                scope.$emit 'uiGridEventCancelCellEdit'
              else if e.keyCode in [9, 13, 37, 38, 39, 40]
                iElement.trigger 'blur'

            openFn = () ->
              iElement.focus()
            $timeout openFn, 50, false


  .directive 'cellBoolean', () ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
      link: (scope, iElement, iAttrs) ->
        scope.$watch 'cellData', (newValue, oldValue) ->
          if typeof newValue == 'object' && newValue?['$notAvailable']
            iElement.text('N/A')
          else if ! newValue?
            iElement.text('')
          else if newValue
            iElement.text('Yes')
          else 
            iElement.text('No')


  .directive 'editableCellBoolean', Array '$timeout', 'findParentCell', ($timeout, findParentCell) ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
      template: "<input type='text' class='form-control select-padding-override'>"
      link: (scope, iElement, iAttrs) ->

        editor = undefined
        dropdownLeft = undefined
        dropdownTop = undefined

        destroy = () ->
          iElement.select2("destroy")
          scope.$emit 'uiGridEventEndCellEdit'

        scope.$watch 'cellData', (val) ->
          id = undefined
          if typeof val == 'object' && val?['$notAvailable']
            id = '$notAvailable'
          else if val == true
            id = 'true'
          else if val == false
            id = 'false'

          editor = iElement.select2({placeholder: "Yes/No", allowClear: true, data: [{id: "true", text: "Yes"}, {id: "false", text: "No"}, {id: "$notAvailable", text: "N/A"}]})

          cell = findParentCell iElement.select2("container"), 'ui-grid-cell'
          dropdownLeft = cell[0].offsetLeft
          dropdownTop = cell[0].offsetTop

          iElement.select2("val", id)

          editor.on 'change', (e) ->
            scope.$apply () ->
              value = null
              if e.target.value == '$notAvailable'
                value = {'$notAvailable': true}
              else if e.target.value
                value = JSON.parse(e.target.value)
              scope.cellData = value

          editor.on 'select2-selecting', (e) ->
            if e.choice.id == e.target.value
              scope.$parent.$emit 'uiGridEventEndCellEdit'

          editor.on 'select2-blur', (e) ->
            scope.$parent.$emit 'uiGridEventEndCellEdit'

          editor.on 'select2-close', (e) ->
            $timeout destroy, 100

          openFn = () ->
            iElement.select2("open")

          $timeout openFn, 50, false

        scope.$on 'gridAdjusted', (data) ->
          cell = findParentCell iElement.select2("container"), 'ui-grid-cell'

          deltaLeft = cell[0].offsetLeft - dropdownLeft
          deltaTop = cell[0].offsetTop - dropdownTop

          if deltaLeft != 0 or deltaTop != 0
            dropdown = iElement.select2("dropdown")
            dropdown.css("left", parseInt(dropdown.css("left")) + deltaLeft + "px") if deltaLeft != 0
            dropdown.css("top", parseInt(dropdown.css("top")) + deltaTop + "px") if deltaTop != 0


  .directive 'cellDate', () ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
      link: (scope, iElement, iAttrs) ->
        scope.$watch 'cellData', (newValue, oldValue) ->
          if typeof newValue == 'object' && newValue?['$notAvailable']
            iElement.text('N/A')
          else if ! newValue?
            iElement.text('')
          else 
            iElement.text(newValue)


  .directive 'editableCellDate', Array 'findParentCell', (findParentCell) ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
      template: "<input type='text' class='form-control datepicker'>"
      link: (scope, iElement, iAttrs) ->

        dropdownLeft = undefined
        dropdownTop = undefined

        datepicker = iElement.datepicker({
          clearBtn: true, 
          magicBtn: "N/A",
          todayHighlight: true, 
          autoclose: false, 
          format: "yyyy-mm-dd"
        })

        iElement.on 'keydown', (e) ->
          if e.keyCode == 27
            e.stopPropagation()
            e.preventDefault()
            scope.$emit 'uiGridEventCancelCellEdit'

        removing = false

        scope.$on 'uiGridEventEndCellEdit', (e) ->
          if !removing
            removing = true
            datepicker.datepicker('remove')
            datepicker = undefined

        datepicker.on 'show', (e) ->
          cell = findParentCell datepicker.data().datepicker.element, 'ui-grid-cell'
          dropdownLeft = cell[0].offsetLeft
          dropdownTop = cell[0].offsetTop

        datepicker.on 'changeDate', (e) ->
          scope.$apply () ->

            dateString = if typeof e.date == 'string' and e.date == 'N/A'
              {'$notAvailable': true}
            else if e.date
             (e.date.getUTCFullYear()) + "-" + ('0' + (1 + e.date.getUTCMonth())).slice(-2) + '-' + ('0' + e.date.getUTCDate()).slice(-2)
            else
              null
            scope.cellData = dateString
          scope.$emit 'uiGridEventEndCellEdit'

        datepicker.on 'hide', (e) ->
          scope.$emit 'uiGridEventEndCellEdit'

        scope.$watch 'cellData', (val) ->
          if datepicker
            if typeof val == 'object' && val?['$notAvailable']
              datepicker.datepicker('update', 'N/A')
            else
              datepicker.datepicker('update', val)
            datepicker.datepicker('show')
            datepicker.data().datepicker.element.focus()

        scope.$on 'gridAdjusted', (data) ->
          cell = findParentCell datepicker.data().datepicker.element, 'ui-grid-cell'

          deltaLeft = cell[0].offsetLeft - dropdownLeft
          deltaTop = cell[0].offsetTop - dropdownTop

          if deltaLeft != 0 or deltaTop != 0
            dropdown = datepicker.data().datepicker.picker
            dropdown.css("left", parseInt(dropdown.css("left")) + deltaLeft + "px") if deltaLeft != 0
            dropdown.css("top", parseInt(dropdown.css("top")) + deltaTop + "px") if deltaTop != 0


  .directive 'editableCellOption', Array '$timeout', 'findParentCell', ($timeout, findParentCell) ->
    result =
      restrict: "A"
      replace: true
      scope:
        cellData: '='
        cellOptions: '='
      template: "<input type='text' class='form-control select-padding-override'>"
      link: (scope, iElement, iAttrs) ->

        editor = undefined
        cellValue = undefined
        dropdownLeft = undefined
        dropdownTop = undefined

        scope.$watch 'cellData', (val, oldVal) ->
          if typeof val == 'object' && val?['$notAvailable']
            val = '$notAvailable'
          if editor
            iElement.select2("val", val)
          else
            cellValue = val

        destroy = () ->
          iElement.select2("destroy")
          scope.$emit 'uiGridEventEndCellEdit'

        scope.$watch 'cellOptions', (options) ->
          if options?
            values = ({id: v, text: v} for v in options)
            values.push {id: "$notAvailable", text: "N/A"}
            editor = iElement.select2({placeholder: "Choose...", allowClear: true, data: values})

            cell = findParentCell iElement.select2("container"), 'ui-grid-cell'
            dropdownLeft = cell[0].offsetLeft
            dropdownTop = cell[0].offsetTop

            if cellValue?
              iElement.select2("val", cellValue)

            editor.on 'change', (e) ->
              scope.$apply () ->
              value = null
              if e.target.value == '$notAvailable'
                value = {'$notAvailable': true}
              else if e.target.value
                value = e.val
              scope.cellData = value

            editor.on 'select2-selecting', (e) ->
              if e.choice.id == e.target.value
                scope.$parent.$emit 'uiGridEventEndCellEdit'

            editor.on 'select2-blur', (e) ->
              scope.$parent.$emit 'uiGridEventEndCellEdit'

            editor.on 'select2-close', (e) ->
              value = e.target.value or null
              $timeout destroy, 100

            openFn = () ->
              iElement.select2("open")
            $timeout openFn, 50, false

        scope.$on 'gridAdjusted', (data) ->
          cell = findParentCell iElement.select2("container"), 'ui-grid-cell'

          deltaLeft = cell[0].offsetLeft - dropdownLeft
          deltaTop = cell[0].offsetTop - dropdownTop

          if deltaLeft != 0 or deltaTop != 0
            dropdown = iElement.select2("dropdown")
            dropdown.css("left", parseInt(dropdown.css("left")) + deltaLeft + "px") if deltaLeft != 0
            dropdown.css("top", parseInt(dropdown.css("top")) + deltaTop + "px") if deltaTop != 0


  .directive 'gridError', () ->
    result =
      restrict: "A"
      replace: true
      scope:
        errorMessage: '='
        close: '&onClose'
      template: '<div ng-show="errorMessage" class="alert alert-danger" role="alert">' +
                '  <button type="button" class="close" ng-click="close()"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>' +
                '  <strong>Error!</strong> <span class="grid-error-message">{{errorMessage}}</span>' +
                '</div>'


  ## Monkeypatch/decorate uiGridRenderContainer so that it doesn't re-initiate events
  ## See: https://github.com/angular-ui/ng-grid/issues/2467#issuecomment-69059283

  .config Array '$provide', ($provide) ->

    $provide.decorator 'uiGridRenderContainerDirective', Array '$delegate', '$document', '$timeout', 'uiGridCellNavService', ($delegate, $document, $timeout, uiGridCellNavService) ->

      directive = undefined
      for delegate in $delegate
        if delegate.scope == false
          directive = delegate
          break

      directive.compile = () ->
        result = 
          pre: ($scope, $elm, $attrs, uiGridCtrl) ->
          post: ($scope, $elm, $attrs, controllers) ->

            uiGridCtrl = controllers[0]
            renderContainerCtrl = controllers[1]

            containerId = renderContainerCtrl.containerId

            grid = uiGridCtrl.grid

            ## Needs to run last after all renderContainers are built
            uiGridCellNavService.decorateRenderContainers(grid)

            ## Let the render container be focus-able
            $elm.attr("tabindex", -1)

            ## Bind to keydown events in the render container
            $elm.on 'keydown', (evt) ->
              evt.uiGridTargetRenderContainerId = containerId
              uiGridCtrl.cellNav.handleKeyDown(evt)

            ## When there's a scroll event we need to make sure to re-focus the right row, because the cell contents may have changed
            $scope.$on 'uiGridScroll', (evt, args) ->

              ## Skip if there's no currently-focused cell
              return if uiGridCtrl.grid.api.cellNav.getFocusedCell() == null

              ## We have to wrap in TWO timeouts so that we run AFTER the scroll event is resolved.
              $timeout () ->
                $timeout () ->
                  ## Get the last row+col combo
                  lastRowCol = uiGridCtrl.grid.api.cellNav.getFocusedCell()

                  ## If the body element becomes active, re-focus on the render container so we can capture cellNav events again.
                  ##   NOTE: this happens when we navigate LET from the left-most cell (RIGHT from the right-most) and have to re-render a new
                  ##   set of cells. The cell element we are navigating to doesn't exist and focus gets lost. This will re-capture it, imperfectly...
                  $elm[0].focus() if $document.activeElement == $document.body


      $delegate
