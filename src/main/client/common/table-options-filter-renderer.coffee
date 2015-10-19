## Handle a filter cell. This also includes an action capable of sending
## filter information back into the system.

filterAction = (evt) ->
  button = evt.detail.button
  $(button).filterdropdown({filter: evt.detail})
  $(button).filterdropdown('showWidget')

  text = evt.detail.text

  $(button).on 'keyup.filterdropdown', (e) ->

    ## Escape key, close the filter
    if e.originalEvent.keyCode == 27
      $(button).filterdropdown('hideWidget')

    ## Enter key, apply the filter
    if e.originalEvent.keyCode == 13
      instance = evt.detail.instance
      value = $(button).filterdropdown('getText')
      instance.setDataAtRowProp 0, evt.detail.property, value, "filter"

      $(button).filterdropdown('hideWidget')


TrackerFilterRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  text = document.createElement("div")
  text.classList.add("tracker-filter-value")
  text.textContent = value

  button = document.createElement('button')
  button.style.float = "right"
  button.setAttribute "type", "button"
  button.classList.add("btn", "btn-default", "active", "tracker-filter-button")

  icon = document.createElement('span')
  icon.classList.add("glyphicon", "glyphicon-filter")
  button.setAttribute "aria-hidden", "true"

  button.appendChild icon

  propertyName = prop()
  headerName = instance.getColHeader(col)
  columnType = instance.trackerData?.typeTable?[col]

  button.addEventListener "click", (evt) ->
    myEvent = new evt.view.CustomEvent("filter", {value: value, detail: {instance: instance, text: text, value: value, property: propertyName, button: button, header: headerName, type: columnType}})
    filterAction myEvent

  while TD.firstChild
    TD.removeChild TD.firstChild
  TD.appendChild(button)
  TD.appendChild(text)


Handsontable.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.registerRenderer('trackerFilter', TrackerFilterRenderer)
