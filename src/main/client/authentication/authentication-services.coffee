angular
  .module 'tracker.authentication'

  .factory 'authenticationService', Array '$rootScope', '$http', '$window', (scope, $http, $window) ->

    config =
      headers: {'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'}

    result =
      login: (targetScope, data) ->

        if data.redirect
          delete data.redirect
          $window.location.href = '/api/authentication/login?' + jQuery.param data
          return

        payload = jQuery.param data

        $http
          .post '/api/authentication/login', payload, config

          .success (response, status) ->
            targetScope.$emit 'event:loginConfirmed', {user: response.user}

          .error (response, status) ->
            targetScope.$broadcast 'event:loginDenied', response, status

      logout: () ->
        $http
          .post('/api/authentication/logout', {}, config)
          .success (response) ->
            scope.$broadcast 'event:logoutConfirmed'
