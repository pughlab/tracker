angular
  .module 'tracker.sockets'

  .factory 'socketFactory', Array '$location', ($location) ->

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
      	  @eventer = jQuery(@)
      	  @socket = new WebSocket("ws://#{host}:#{port}/events")
      
      	emit: (evt, data) ->
      	  @eventer.emit evt, data
      	
      	on: (evt, handler) ->
      	  @eventer.bind evt, handler
      
      	off: (evt, handler) ->
      	  @eventer.unbind evt, handler
      
  
      new SocketEventEmitter(protocol, host, port)
