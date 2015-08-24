angular
  .module 'tracker.filters', []

  .filter 'keys', () ->
    (input) ->
      if input
        Object.keys(input)
      else
        input

  .filter 'find', () ->
    (input, property, value) ->
      for element in input
        if element[property] == value
          return true
      false

  .filter 'mapProperties', () ->
    (input, property) ->
      if input
        for element in input
          element[property]
      else
        input
