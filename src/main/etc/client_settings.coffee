angular
  .module 'tracker'
  
  .service 'configurationSettings', () ->
    result =
      clientID: "${clientID}"
