## =====================================================================================
## Date editor -- one is built in, and I really like that, but it could have used a
## little better factoring to allow options to be set better.

outerHeight = (elem) ->
  if hasCaptionProblem() and elem.firstChild?.nodeName == 'CAPTION'
    elem.offsetHeight + elem.firstChild.offsetHeight
  else
    elem.offsetHeight

_hasCaptionProblem = undefined

detectCaptionProblem = () ->
  TABLE = document.createElement('TABLE')
  TABLE.style.borderSpacing = 0
  TABLE.style.borderWidth = 0
  TABLE.style.padding = 0
  TBODY = document.createElement('TBODY')
  TABLE.appendChild(TBODY)
  TBODY.appendChild(document.createElement('TR'))
  TBODY.firstChild.appendChild(document.createElement('TD'))
  TBODY.firstChild.firstChild.innerHTML = '<tr><td>t<br>t</td></tr>'
  CAPTION = document.createElement('CAPTION')
  CAPTION.innerHTML = 'c<br>c<br>c<br>c'
  CAPTION.style.padding = 0
  CAPTION.style.margin = 0
  TABLE.insertBefore(CAPTION, TBODY)
  document.body.appendChild(TABLE)
  _hasCaptionProblem = (TABLE.offsetHeight < 2 * TABLE.lastChild.offsetHeight)
  document.body.removeChild(TABLE)

hasCaptionProblem = () ->
  if typeof _hasCaptionProblem == "undefined"
    detectCaptionProblem()
  _hasCaptionProblem

hasEventListeners = !!window.addEventListener

fireEvent = (el, eventName, data) ->
  if document.createEvent
    ev = document.createEvent('HTMLEvents')
    ev.initEvent(eventName, true, false)
    for own k, v of data
      ev[k] = v
    el.dispatchEvent(ev)
  else if document.createEventObject
    ev = document.createEventObject()
    for own k, v of data
      ev[k] = v
    el.fireEvent('on' + eventName, ev)

addEvent = (el, e, callback, capture) ->
  if hasEventListeners
    el.addEventListener(e, callback, !!capture)
  else
    el.attachEvent('on' + e, callback)

removeEvent = (el, e, callback, capture) ->
  if hasEventListeners
    el.removeEventListener(e, callback, !!capture)
  else
    el.detachEvent('on' + e, callback)

hasClass = (el, cn) ->
  (' ' + el.className + ' ').indexOf(' ' + cn + ' ') != -1


class TrackerDateEditor extends Handsontable.editors.TextEditor

  constructor: (hotInstance) ->
    @$datePicker = null
    @datePicker = null
    @datePickerStyle = null
    @defaultDateFormat = 'DD/MM/YYYY'
    @isCellEdited = false
    @parentDestroyed = false

    super(hotInstance)

  init: () ->
    if typeof moment != 'function'
      throw new Error('You need to include moment.js to your project.')

    if typeof Pikaday != 'function'
      throw new Error('You need to include Pikaday to your project.')

    super()
    @instance.addHook 'afterDestroy', () =>
      @parentDestroyed = true
      @destroyElements()

  ## Creates the date picker. This replaces a simple new Pikaday(options)
  ## with additional logic for patching in the new buttons that we use.
  createDatePicker: (options) ->
    picker = new Pikaday(options)
    picker._na = false

    oldMouseDown = picker._onMouseDown
    oldInputChange = picker._onInputChange

    newInputChange = (event) ->
      if event.firedBy != picker && picker._o.field?.value == "N/A"
        picker.setDate.call(picker, "N/A")
      else
        oldInputChange.call(picker, event)

    newMouseDown = (event) ->
      target = event.target || event.srcElement
      return if !target
      if hasClass(target, 'tracker-button')
        if hasClass(target, 'tracker-clear-button')
          picker.setDate.call(picker, "")
        else if hasClass(target, 'tracker-na-button')
          picker.setDate.call(picker, "N/A")

        hiderFn = () ->
          picker.hide.call(picker)
          picker._o.field.blur() if picker._o.field

        setTimeout hiderFn, 10

      else
        oldMouseDown.call(picker, event)

    picker.setDate = (date, preventOnSelect) ->
      if typeof date == 'string' && date == 'N/A'
        if @_o.field
          @_d = null
          @_na = true
          @_o.field.value = 'N/A'
          fireEvent(@_o.field, 'change', { firedBy: picker })
        @draw()
      else
        @_na = false
        Pikaday.prototype.setDate.call(picker, date, preventOnSelect)

    ## Patch in the new event handlers
    removeEvent(picker.el, (if 'ontouchend' in document then 'touchend' else 'mousedown'), oldMouseDown, true)
    addEvent(picker.el, (if 'ontouchend' in document then 'touchend' else 'mousedown'), newMouseDown, true)
    if picker._o.field
      removeEvent(picker._o.field, 'change', oldInputChange)
      addEvent(picker._o.field, 'change', newInputChange)

    picker.render = (year, month) ->
      body = Pikaday.prototype.render.call(@, year, month)
      classClear = if ! @_d and ! @_na then "is-selected" else ""
      classNA = if @_na then "is-selected" else ""
      body +
        "<div class='tracker-pika-button-container'>" +
        "<div class='tracker-pika-button #{classClear}'><button class='pika-button tracker-button tracker-clear-button' type='button'>Clear</button></div>" +
        "<div class='tracker-pika-button #{classNA}'><button class='pika-button tracker-button tracker-na-button' type='button'>N/A</button></div>" +
        "</div>"

    picker

  createElements: () ->
    super()

    @datePicker = document.createElement('DIV')
    @datePickerStyle = @datePicker.style
    @datePickerStyle.position = 'absolute'
    @datePickerStyle.top = 0
    @datePickerStyle.left = 0
    @datePickerStyle.zIndex = 9999

    @datePicker.classList.add('htDatepickerHolder')
    document.body.appendChild(@datePicker)

    @$datePicker = @createDatePicker(@getDatePickerConfig())
    eventManager = new Handsontable.eventManager(@)

    eventManager.addEventListener(@datePicker, 'mousedown', (event) => event.stopPropagation())
    @hideDatepicker()

  destroyElements: () ->
    @$datePicker.destroy()

  prepare: (row, col, prop, td, originalValue, cellProperties) ->
    @_opened = false
    super(row, col, prop, td, originalValue, cellProperties)

  open: (event) ->
    super()
    @showDatepicker(event)

  close: () ->
    @_opened = false
    timeFn = () => @instance.selection.refreshBorders()
    @instance._registerTimeout(setTimeout(timeFn, 0))
    super()

  finishEditing: (isCancelled = false, ctrlDown = false) ->
    if isCancelled
      value = @originalValue

      if typeof value != "undefined"
        @setValue(value)

    @hideDatepicker()
    super(isCancelled, ctrlDown)

  showDatepicker: (event) ->
    @$datePicker.config(@getDatePickerConfig())

    offset = @TD.getBoundingClientRect()
    dateFormat = @cellProperties.dateFormat || @defaultDateFormat
    datePickerConfig = @$datePicker.config()
    dateStr = undefined
    isMouseDown = @instance.view.isMouseDown()
    isMeta = if event then isMetaKey(event.keyCode) else false

    @datePickerStyle.top = (window.pageYOffset + offset.top + outerHeight(@TD)) + 'px'
    @datePickerStyle.left = (window.pageXOffset + offset.left) + 'px'

    @$datePicker._onInputFocus = () ->
    datePickerConfig.format = dateFormat

    if @originalValue == "N/A"
      @$datePicker.setDate(@originalValue)

    else if @originalValue
      dateStr = @originalValue

      if moment(dateStr, "YYYY-MM-DD", true).isValid()
        @$datePicker.setMoment(moment(dateStr, "YYYY-MM-DD"), true)
      if !isMeta && !isMouseDown
        @setValue('')

    else
      if @cellProperties.defaultDate
        dateStr = @cellProperties.defaultDate

        datePickerConfig.defaultDate = dateStr

        if moment(dateStr, "YYYY-MM-DD", true).isValid()
          @$datePicker.setMoment(moment(dateStr, "YYYY-MM-DD"), true)

        if !isMeta && !isMouseDown
          @setValue('')
      else
        ## if a default date is not defined, set a soft-default-date: display the current day and month in the
        ## datepicker, but don't fill the editor input
        @$datePicker.gotoToday()

    @datePickerStyle.display = 'block'
    @$datePicker.show()

  hideDatepicker: () ->
    @datePickerStyle.display = 'none'
    @$datePicker.hide()

  getDatePickerConfig: () ->
    htInput = @TEXTAREA
    options = {}

    if @cellProperties && @cellProperties.datePickerConfig
      deepExtend(options, @cellProperties.datePickerConfig)

    origOnSelect = options.onSelect
    origOnClose = options.onClose

    options.field = htInput
    options.trigger = htInput
    options.container = @datePicker
    options.bound = false
    options.format = options.format || @defaultDateFormat
    options.reposition = options.reposition || false
    options.onSelect = (dateStr) =>
      if !isNaN(dateStr.getTime())
        dateStr = moment(dateStr).format("YYYY-MM-DD")
      @setValue(dateStr)
      @hideDatepicker()

      if origOnSelect
        origOnSelect()

    options.onClose = () =>
      if !@parentDestroyed
        @finishEditing(false)
      if origOnClose
        origOnClose()

    options

## =====================================================================================
## Register the various editors.

Handsontable.editors.TrackerDateEditor = TrackerDateEditor
Handsontable.editors.registerEditor('trackerDate', TrackerDateEditor)
