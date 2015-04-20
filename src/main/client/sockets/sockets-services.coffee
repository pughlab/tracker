angular
  .module 'tracker.sockets'

  .factory 'socketFactory', Array '$location', ($location) ->

    () ->

      host = $location.host()
      protocol = $location.protocol()
      port = $location.port()
  
      ## In some rare cases, this might need to be overridden, especially during development
      ## where the Gulp proxying system is borked. That means we need to handle things specially
      ## during the Gulp code. We can pounce on this chance to actually show the exact version
      ## in use while we are at it. 
  
      ## @if NODE_ENV = 'development' *
      host = 'localhost'
      protocol = 'http'
      port = 3001
      ## @endif *
  
      socket = io("#{protocol}://#{host}:#{port}", {path: '/api/events', forceNew: true})  
      return socket