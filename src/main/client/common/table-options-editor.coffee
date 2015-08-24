## =====================================================================================
## Date editor -- one is built in, and I really like that, but it could have used a
## little better factoring to allow options to be set better.

TrackerDateEditor = Handsontable.editors.DateEditor.prototype.extend()

TrackerDateEditor.prototype.createElements = () ->
  that = this
  Handsontable.editors.TextEditor.prototype.createElements.apply(this, arguments)

  @defaultDateFormat = 'DD/MM/YYYY'
  @datePicker = document.createElement('DIV')
  @datePickerStyle = @datePicker.style
  @datePickerStyle.position = 'absolute'
  @datePickerStyle.top = 0
  @datePickerStyle.left = 0
  @datePickerStyle.zIndex = 9999

  Handsontable.Dom.addClass(@datePicker, 'htDatepickerHolder')
  document.body.appendChild(@datePicker)

  defaultOptions = @createDatePickerOptions()
  @$datePicker = @createDatePicker(defaultOptions)

  eventManager = Handsontable.eventManager(this)

  ## Prevent recognizing clicking on datepicker as clicking outside of table
  eventManager.addEventListener @datePicker, 'mousedown', (event) ->
    Handsontable.helper.stopPropagation(event)

  @hideDatepicker()


## Factored out so the options can be set independently of the element
## configuration.

TrackerDateEditor.prototype.createDatePickerOptions = () ->
  that = this
  htInput = that.TEXTAREA

  defaultOptions = {
    format: that.defaultDateFormat
    field: htInput
    trigger: htInput
    container: that.datePicker
    reposition: false
    bound: false
    onSelect: (dateStr) ->
      if !isNaN(dateStr.getTime())
        dateStr = moment(dateStr).format(that.cellProperties.dateFormat || that.defaultDateFormat)
      that.setValue(dateStr)
      that.hideDatepicker()
    onClose: () ->
      that.finishEditing(false) if !that.parentDestroyed
  }

  defaultOptions


## Monkeypatching. If we don't have an original date, we don't inform the picker
## and we need to.
TrackerDateEditor.prototype.showDatepicker = (event) ->
  value = @originalValue
  if !value
    @$datePicker.setDate("")
  else if value == 'N/A'
    @$datePicker.setDate(value)

  Handsontable.editors.DateEditor.prototype.showDatepicker.call(@, event)


## And factored out so we can extend Pikaday -- at least this instance -- if needed.

hasEventListeners = !!window.addEventListener

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

isDate = (obj) ->
  (/Date/).test(Object.prototype.toString.call(obj)) && !isNaN(obj.getTime())

extend = (to, from, overwrite) ->
  for own k, v of from
    hasProp = v != undefined
    if hasProp and typeof v == 'object' and v != null and v.nodeName == undefined
      if isDate(v)
        to[k] new Date(v.getTime()) if overwrite
      else if Array.isArray(v)
        to[k] = v.slice(0) if overwrite
      else
        to[k] = extend({}, v, overwrite)
    else if overwrite || !hasProp
      to[k] = v
  to

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

hasClass = (el, cn) ->
  (' ' + el.className + ' ').indexOf(' ' + cn + ' ') != -1



TrackerDateEditor.prototype.createDatePicker = (options) ->
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

  ## Patch in the new event handlers
  removeEvent(picker.el, (if 'ontouchend' in document then 'touchend' else 'mousedown'), oldMouseDown, true)
  addEvent(picker.el, (if 'ontouchend' in document then 'touchend' else 'mousedown'), newMouseDown, true)
  if picker._o.field
    removeEvent(picker._o.field, 'change', oldInputChange)
    addEvent(picker._o.field, 'change', newInputChange)

  ## Monkeypatching like a fox
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


## =====================================================================================
## Register the various editors.

Handsontable.editors.TrackerDateEditor = TrackerDateEditor
Handsontable.editors.registerEditor('trackerDate', TrackerDateEditor)
