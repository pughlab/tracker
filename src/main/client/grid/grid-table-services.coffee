angular
  .module 'tracker.grid'

  ## We have an endpoint that returns a cell/entity history, but that isn't especially
  ## useful for us directly. This service formats it into HTML that can be rendered
  ## in a tooltip. That means we can do formatting niceness.

  .factory 'valueManager', () ->
    return (name) ->
      (row, value) ->
        if !row?
          name
        else if value?
          row[name] =
            switch value
              when "" then null
              when "N/A" then {"$notAvailable": true}
              else value
        else
          current = row[name]
          if current == null or current == undefined
            ""
          else if current.hasOwnProperty('$notAvailable')
            "N/A"
          else
            current


  .factory 'booleanValueManager', () ->
    return (name) ->
      (row, value) ->
        if !row?
          name
        else if value?
          row[name] =
            switch value
              when "" then null
              when "N/A" then {"$notAvailable": true}
              when "Yes" then true
              when "No" then false
        else
          current = row[name]
          if current == null or current == undefined
            ""
          else if current.hasOwnProperty('$notAvailable')
            "N/A"
          else if current == false
            "No"
          else
            "Yes"


  .factory 'highlightElement', Array '$timeout', ($timeout) ->
    return (element, editingClasses) ->

      classes = editingClasses.split(' ')

      highlightOn = () ->
        for cls in classes
          Handsontable.Dom.addClass element, cls

        highlightOff = () ->
          for cls in classes
            Handsontable.Dom.removeClass element, cls

        $timeout highlightOff, 3000

      if element
        $timeout highlightOn, 100
