## Handle a filter cell. This also includes an action capable of sending
## filter information back into the system.

filterAction = (evt) ->
  console.log "Clicked", evt


TrackerFilterRenderer = (instance, TD, row, col, prop, value, cellProperties) ->

  button = document.createElement('button')
  button.style.float = "right"
  button.setAttribute "type", "button"
  button.classList.add("btn", "btn-default", "tracker-filter-button")

  icon = document.createElement('span')
  icon.classList.add("glyphicon", "glyphicon-filter")
  button.setAttribute "aria-hidden", "true"

  button.appendChild icon

  button.addEventListener "click", (evt) ->
    myEvent = new evt.view.CustomEvent("filter", {detail: {property: prop(), element: button}})
    filterAction myEvent

  while TD.firstChild
    TD.removeChild TD.firstChild
  TD.appendChild(button)


Handsontable.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.TrackerFilterRenderer = TrackerFilterRenderer
Handsontable.renderers.registerRenderer('trackerFilter', TrackerFilterRenderer)
