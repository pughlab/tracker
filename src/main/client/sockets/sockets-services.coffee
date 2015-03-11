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
          @eventer = jQuery(@)[0]
          @atmosphere = atmosphere
          
          request = {
            url: "http://#{host}:#{port}/events"
            contentType: "application/json"
            logLevel: 'debug'
            transport: 'websocket'
            trackMessageLength: true
            reconnectInterval: 5000
          }
          
          request.onOpen = (response) =>
            console.log "Called onOpen", response
            console.log "this1", @
            $timeout () =>    
              console.log "this2", @
              @socket.push @atmosphere.util.stringifyJSON({ author: "stuart", message: "hi there" })
            
          request.onMessage = (response) ->
            console.log "Called onMessage", response
          
          @socket = @atmosphere.subscribe request
          
          disconnect: () ->
            @atmosphere.unsubscribeUrl request.url
          
      
        emit: (evt, data) =>
          @socket
          ## @eventer.emit evt, data
        
        on: (evt, handler) ->
          ## @eventer.bind evt, handler
      
        off: (evt, handler) ->
          ## @eventer.unbind evt, handler
          
        disconnect: () ->
          console.log "Disconnect requested"
          @socket.disconnect()
  
      new SocketEventEmitter(protocol, host, port)
