angular
  .module 'tracker.authentication'

  .factory 'authenticationService', Array '$rootScope', '$http', '$window', (scope, $http, $window) ->

    config =
      headers: {
        'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'
        'Access-Control-Allow-Origin': '*'
        'Access-Control-Allow-Methods': 'POST, GET, OPTIONS, PUT'
      }

    result =
      login: (targetScope, username, password) ->
      
        $window.location.href = '/api/authorization/login?client_name=uhn';

#        $http
#          .get '/api/authorization/login', config
#
#          .success (response, status) ->
#            console.log 'Got response', targetScope, response, status
#            targetScope.$emit 'event:loginConfirmed', response.user
#  
#          .error (response, status) ->
#            targetScope.$broadcast 'event:loginDenied', response

      logout: () ->
        $http
          .post('/api/authorization/logout', {}, config)
          .success (response) ->
            scope.$broadcast 'event:logoutConfirmed'