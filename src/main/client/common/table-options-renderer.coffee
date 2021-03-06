clonableWRAPPER = document.createElement('DIV')
clonableWRAPPER.className = 'htAutocompleteWrapper'

clonableARROW = document.createElement('DIV')
clonableARROW.className = 'htAutocompleteArrow'

## workaround for https://github.com/handsontable/handsontable/issues/1946
##this is faster than innerHTML. See: https://github.com/handsontable/handsontable/wiki/JavaScript-&-DOM-performance-tips
clonableARROW.appendChild(document.createTextNode(String.fromCharCode(9660)));

wrapTdContentWithWrapper = (TD, WRAPPER) ->
  WRAPPER.innerHTML = TD.innerHTML
  Handsontable.Dom.empty(TD)
  TD.appendChild(WRAPPER)


annotateCells = (cellProperties, instance, TD, row, col, prop) ->
  row = instance.sortIndex?[row]?[0] or row
  rowData = instance.getSourceDataAtRow(row)
  fieldName = prop()
  tags = rowData['$notes']?[fieldName]?.tags or []
  for tag in tags
    Handsontable.Dom.addClass(TD, tag)
  if rowData['$state']
    mapping = instance.trackerData?.stateLabels
    label = mapping?[rowData['$state']]
    Handsontable.Dom.addClass(TD, label) if label?
  if rowData['$notes']?[fieldName]?.locked == true
    cellProperties.readOnly = true

## Modified autocomplete option renderer
## @param {Object} instance Handsontable instance
## @param {Element} TD Table cell where to render
## @param {Number} row
## @param {Number} col
## @param {String|Number} prop Row object property name
## @param value Value to render (remember to escape unsafe HTML before inserting to DOM!)
## @param {Object} cellProperties Cell properites (shared by cell renderer and editor)


revertValueIfNeeded = (instance, row, prop, oldValue, cellProperties) ->
  oldValue = null if !oldValue?
  if cellProperties.valid == false and cellProperties.invalidCellClassName
    restore = () ->
      cellProperties.valid = true
      instance.setDataAtRowProp(row, prop(), oldValue, "revert")

    setTimeout restore, 3000


TrackerOptionRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  if value and value.hasOwnProperty('$notAvailable')
    value = "N/A"

  WRAPPER = clonableWRAPPER.cloneNode(true); ##this is faster than createElement
  ARROW = clonableARROW.cloneNode(true); ##this is faster than createElement

  oldValue = cellProperties.validationData?.oldValue
  revertValueIfNeeded instance, row, prop, oldValue, cellProperties

  Handsontable.renderers.TextRenderer(instance, TD, row, col, prop, value, cellProperties)
  annotateCells cellProperties, instance, TD, row, col, prop

  TD.appendChild(ARROW)
  Handsontable.Dom.addClass(TD, 'htAutocomplete')

  if !TD.firstChild
    TD.appendChild(document.createTextNode(String.fromCharCode(160)))

  if !instance.acArrowListener
    eventManager = Handsontable.eventManager(instance)

    ## not very elegant but easy and fast
    instance.acArrowListener = (event) ->
      if Handsontable.Dom.hasClass(event.target,'htAutocompleteArrow')
        instance.view.wt.getSetting('onCellDblClick', null, new WalkontableCellCoords(row, col), TD)

    eventManager.addEventListener(instance.rootElement,'mousedown',instance.acArrowListener)

    ## We need to unbind the listener after the table has been destroyed
    instance.addHookOnce 'afterDestroy', () ->
      eventManager.clear()


## Boolean renderer, based on the option renderer

TrackerBooleanRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  if value == null or value == undefined
    value = ""
  else if value == false
    value = "No"
  else if value == true
    value = "Yes"

  oldValue = cellProperties.validationData?.oldValue
  revertValueIfNeeded instance, row, prop, oldValue, cellProperties

  TrackerOptionRenderer(instance, TD, row, col, prop, value, cellProperties)


TrackerStringRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  if value and value.hasOwnProperty('$notAvailable')
    value = "N/A"

  oldValue = cellProperties.validationData?.oldValue
  revertValueIfNeeded instance, row, prop, oldValue, cellProperties

  escaped = Handsontable.helper.stringify(value)
  if instance.trackerData?.editingStatus != true && /^\s*(?:https?|ftp|smb):/.test(escaped)

    element = document.createElement('A')
    element.href = value
    element.textContent = value
    element.setAttribute "target", "_blank"
    Handsontable.Dom.addEvent element, 'mousedown', (e) ->
      e.preventDefault()

    Handsontable.Dom.empty(TD)
    TD.appendChild(element)

  else

    Handsontable.renderers.TextRenderer(instance, TD, row, col, prop, value, cellProperties)

  annotateCells cellProperties, instance, TD, row, col, prop


TrackerDateRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  originalValue = value
  oldValue = undefined

  if value and value.hasOwnProperty('$notAvailable')
    value = "N/A"

  formatString = instance.trackerData?.dateFormat

  if formatString? and value != "N/A" and value != "" and value?
    parsed = moment(value, "YYYY-MM-DD", true)
    if parsed.isValid()
      value = parsed.format(formatString)
    else
      oldValue = cellProperties.validationData?.oldValue

  ## In the event of an invalid value, we can revert the value in a
  ## a brief moment. That means we need to write the value back as it
  ## was.
  revertValueIfNeeded instance, row, prop, oldValue, cellProperties

  Handsontable.renderers.TextRenderer(instance, TD, row, col, prop, value, cellProperties)
  annotateCells cellProperties, instance, TD, row, col, prop


## And finally, deploy the various renderers.

Handsontable.TrackerDateRenderer = TrackerDateRenderer
Handsontable.renderers.TrackerDateRenderer = TrackerDateRenderer
Handsontable.renderers.registerRenderer('trackerDate', TrackerDateRenderer)

Handsontable.TrackerOptionRenderer = TrackerOptionRenderer
Handsontable.renderers.TrackerOptionRenderer = TrackerOptionRenderer
Handsontable.renderers.registerRenderer('trackerOption', TrackerOptionRenderer)

Handsontable.TrackerBooleanRenderer = TrackerBooleanRenderer
Handsontable.renderers.TrackerBooleanRenderer = TrackerBooleanRenderer
Handsontable.renderers.registerRenderer('trackerBoolean', TrackerBooleanRenderer)

Handsontable.TrackerStringRenderer = TrackerStringRenderer
Handsontable.renderers.TrackerStringRenderer = TrackerStringRenderer
Handsontable.renderers.registerRenderer('trackerString', TrackerStringRenderer)
