FilterDropdown = (element, options) ->
    this.widget = ''
    this.$element = $(element)
    this.isOpen = options.isOpen
    this.orientation = options.orientation
    this.container = options.container
    this.filter = options.filter
    @_init()
    @

  FilterDropdown.prototype =

    constructor: FilterDropdown

    _init: () ->
      self = @
      @$element.on {
        'click.filterdropdown': $.proxy(@showWidget, @)
        'blur.filterdropdown': $.proxy(@blurElement, @)
      }

      @$widget = $(@getTemplate()).on('click', $.proxy(@widgetClick, @))
      @$widget.find('input').each () ->
        $(@).on {
          'click.filterdropdown': () -> $(@).select()
          'keyup.filterdropdown': $.proxy(self.widgetKeyup, self)
          'keydown.filterdropdown': $.proxy(self.widgetKeydown, self)
        }

    blurElement: () ->
      @highlightedUnit = null
      @updateFromElementVal()

    clear: () ->
      @$element.val('')

    elementKeydown: (e) ->
      @update()

    getTemplate: () ->
      "<div class='bootstrap-filterdropdown-widget dropdown-menu'>" +
      "<form class='form-inline' onsubmit='return false;'>" +
      "<div class='form-group form-group-sm'>" +
      "<label for='filter-input'>#{@filter.header} matches: </label>" +
      "<input type='text' class='form-control' name='filter' id='filter-input'>" +
      "<a role='button' class='clear-button' aria-label='Clear'><span class='glyphicon glyphicon-remove-circle'></span></a>" +
      "</div>" +
      "</form>" +
      "</div>"

    getText: () ->
      @$widget.find('input').val()

    hideWidget: () ->
      return if @isOpen == false

      @$element.trigger {
        'type': 'hide.filterdropdown'
        'text': @getText()
      }

      @$widget.removeClass('open')

      $(document).off('mousedown.filterdropdown, touchend.filterdropdown')

      @isOpen = false
      @$widget.detach()

    place : () ->
      return if @isInline

      widgetWidth = @$widget.outerWidth()
      widgetHeight = @$widget.outerHeight()
      visualPadding = 10
      windowWidth = $(window).width()
      windowHeight = $(window).height()
      scrollTop = $(window).scrollTop()

      zIndex = parseInt(@$element.parents().first().css('z-index'), 10) + 10
      offset = if @component then @.omponent.parent().offset() else @$element.offset()
      height = if @component then @component.outerHeight(true) else @$element.outerHeight(false)
      width = if @component then @component.outerWidth(true) else @$element.outerWidth(false)
      left = offset.left
      top = offset.top

      @$widget.removeClass('filterdropdown-orient-top filterdropdown-orient-bottom filterdropdown-orient-right filterdropdown-orient-left')

      if @orientation.x != 'auto'
        @picker.addClass('filterdropdown-orient-' + @orientation.x)
        if @orientation.x == 'right'
          left -= widgetWidth - width
      else
        @$widget.addClass('filterdropdown-orient-left')
        if offset.left < 0
          left -= offset.left - visualPadding
        else if offset.left + widgetWidth > windowWidth
          left = windowWidth - widgetWidth - visualPadding

      yorient = @.orientation.y
      topOverflow = undefined
      bottomOverflow = undefined

      if yorient == 'auto'
        topOverflow = -scrollTop + offset.top - widgetHeight
        bottomOverflow = scrollTop + windowHeight - (offset.top + height + widgetHeight)
        if Math.max(topOverflow, bottomOverflow) == bottomOverflow
          yorient = 'top'
        else
          yorient = 'bottom'

      @$widget.addClass('filterdropdown-orient-' + yorient)
      if yorient == 'top'
        top += height
      else
        top -= widgetHeight + parseInt(@$widget.css('padding-top'), 10)

      @$widget.css {
        top : top
        left : left
        zIndex : zIndex
      }

    remove: () ->
      $('document').off('.filterdropdown')
      if @$widget
        @$widget.remove()
      delete @$element.data().filterdropdown

    setText: (text, ignoreWidget) ->
      if !text
        @clear()
        return

      @text = text
      @update(ignoreWidget)

    showWidget: (e) ->
      console.log "Called showWidget"
      e.preventDefault() if e?
      return if @isOpen
      return if @$element.is(':disabled')

      @$widget.appendTo(@container)
      self = @;
      $(document).on 'mousedown.filterdropdown, touchend.filterdropdown', (e) ->
        self.hideWidget() if !(self.$element.parent().find(e.target).length || self.$widget.is(e.target) || self.$widget.find(e.target).length)

      @$widget.find(".clear-button").on 'mousedown.filterdropdown, touchend.filterdropdown', (e) ->
        e.stopPropagation()
        e.preventDefault()
        self.$widget.find("input").val("")
        self.$widget.find("input").focus()

      @$element.trigger {
        'type': 'show.filterdropdown',
        'text': @getText()
      }

      @place()
      @$element.blur()

      @$widget.addClass('open') if @isOpen == false
      @$widget.find("input").focus()
      @isOpen = true

    update: (ignoreWidget) ->
      @updateElement()
      @updateWidget() if !ignoreWidget

    updateElement: () ->
      value = @getText()
      @$element.val(value).trigger('change')

    updateFromElementVal: () ->
      @setText(@$element.val())

    updateWidget: () ->
      return if (@$widget == false)

      text = @text;
      @$widget.find('.bootstrap-filterdropdown-body').val(text)

    updateFromWidgetInputs: () ->
      return if @$widget == false

      t = @$widget.find('.bootstrap-filterdropdown-body').val()
      @setText(t, true)

    widgetClick: (e) ->
      e.stopPropagation()
      e.preventDefault()

      $input = $(e.target)
      action = $input.closest('a').data('action')

      if action
        @[action]()
      @update()

    ## Stop keydown events propagating, as these go through to the grid
    widgetKeydown: (e) ->
      e.stopPropagation()

    widgetKeyup: (e) ->
      @updateFromWidgetInputs()
      @$element.trigger {
        'type': 'keyup.filterdropdown',
        'text': @getText(),
        'originalEvent': e.originalEvent
      }


  $.fn.filterdropdown = (option) ->
    args = Array.apply(null, arguments)
    args.shift()
    result = undefined

    @each () ->
      $this = $(@)
      data = $this.data('filterdropdown')
      options = typeof option == 'object' && option

      if !data
        data = new FilterDropdown(@, $.extend({}, $.fn.filterdropdown.defaults, options, $(@).data()))
        $this.data('filterdropdown', data)

      if typeof option == 'string'
        result = data[option].apply(data, args)

    result


  $.fn.filterdropdown.defaults =
    isOpen: false
    orientation: { x: 'auto', y: 'auto'}
    container: 'body'

  $.fn.filterdropdown.Constructor = FilterDropdown
