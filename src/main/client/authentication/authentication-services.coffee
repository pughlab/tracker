angular
  .module 'tracker.authentication'

  .factory 'authenticationService', Array '$rootScope', '$http', (scope, $http) ->

    config =
      headers: {'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'}

    result =
      login: (targetScope, username, password) ->

        payload = jQuery.param
          username: username
          password: password

        $http
          .post '/api/authorization/login', payload, config

          .success (response, status) ->
            $http.get '/api/studies'

          .success (response, status) ->
            targetScope.$emit 'event:loginConfirmed', response.user

          .error (response, status) ->
            targetScope.$broadcast 'event:loginDenied', response

      logout: () ->
        $http
          .post('/api/authorization/logout', {}, config)
          .success (response) ->
            scope.$broadcast 'event:logoutConfirmed'
