angular
  .module 'tracker.sockets'

  .factory 'socketFactory', Array '$location', '$timeout', ($location, $timeout) ->

    () ->

      protocol = $location.protocol()
      host = $location.host()
      port = $location.port()

      ## In some rare cases, this might need to be overridden, especially during development
      ## where the Gulp proxying system is borked. That means we need to handle things specially
      ## during the Gulp code. We can pounce on this chance to actually show the exact version
      ## in use while we are at it.

      ## @if NODE_ENV = 'development' *
      ## host = 'localhost'
      ## protocol = 'http'
      ## port = 8080
      ## @endif *

      class SocketEventEmitter

        constructor: (protocol, host, port) ->
          @events = {}
          @atmosphere = atmosphere

          @request = {
            url: "http://#{host}:#{port}/events"
            contentType: "application/json"
            logLevel: 'debug'
            transport: 'websocket'
            trackMessageLength: true
            reconnectInterval: 5000
          }

          @request.onOpen = (response) ->
            console.log "Called onOpen", response

          @request.onReopen = (response) ->
            console.log "Called onReopen", response

          @request.onReconnect = (response) ->
            console.log "Called onReconnect", response

          @request.onError = (response) ->
            console.log "Called onError", response

          @request.onClose = (response) ->
            console.log "Called onClose", response

          @request.onMessage = (response) =>
            console.log "Called onMessage", response
            decoded = JSON.parse response.responseBody
            if @events[decoded.type]
              listener decoded for listener in @events[decoded.type]

          @socket = @atmosphere.subscribe @request

        addListener: (event, listener) ->
          if @events['newListener']
            l listener for l in @events['newListener']
          (@events[event]?=[]).push listener
          return @

        removeListener: (event, listener) ->
          return @ unless @events[event]
          @events[event] = (l for l in @events[event] when l isnt listener)
          return @

        removeAllListeners: (event) ->
          delete @events[event]
          return @

        ## This should transmit back through the socket
        emit: (evt, scope, data) =>
          @socket.push JSON.stringify { type: evt, scope: scope, data: data }

        on: (evt, handler) =>
          @addListener(evt, handler)

        once: (event, listener) ->
          fn = =>
            @removeListener event, fn
            listener arguments...
          @on event, fn
          return @

        disconnect: () ->
          console.log "Disconnect requested"
          @atmosphere.unsubscribeUrl @request.url

      socket = new SocketEventEmitter(protocol, host, port)

      socket.on 'newListener', (data...) ->

      socket
