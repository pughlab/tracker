angular
  .module 'tracker.authentication'

  .factory 'authenticationService', Array '$rootScope', '$http', (scope, $http) ->

    config =
      headers: {'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'}

    result =
      ping: () ->
        $http.get('/api/authentication/ping', {}, config).success (response) ->
          if response.user
            scope.$broadcast 'event:loginConfirmed', response.user

      login: (targetScope, username, password) ->

        payload = jQuery.param
          username: username
          password: password

        $http
          .post '/api/authentication/login', payload, config

          .success (response, status) ->
            targetScope.$broadcast 'event:loginApproved', response.user
            scope.$broadcast 'event:loginConfirmed', response.user
  
          .error (response, status) ->
            targetScope.$broadcast 'event:loginDenied', response

      logout: () ->
        $http
          .post('/api/authentication/logout', {}, config)
          .success (response) ->
            scope.$broadcast 'event:logoutConfirmed'