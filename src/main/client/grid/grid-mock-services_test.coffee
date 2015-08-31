angular
  .module 'tracker.grid'

  .factory 'mockEventEmitter', () ->

    class EventEmitter

      constructor: () ->
        @events = {}

      on: (evt, fn) ->
        @events[evt] ?= []
        @events[evt].push fn

      emit: (evt, args...) ->
        events = @events[evt]
        return if ! events?
        for fn in events
          fn.apply(this, args)

    EventEmitter
