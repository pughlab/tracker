angular
  .module 'tracker.authentication'

  .factory 'authenticationService', Array '$rootScope', '$http', '$window', (scope, $http, $window) ->

    config =
      headers: {'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'}

    result =
      login: (targetScope, data) ->

        if data.redirect
          delete data.redirect
          $window.location.href = '/api/authorization/login?' + jQuery.param data
          return

        payload = jQuery.param data

        $http
          .post '/api/authorization/login', payload, config

          .success (response, status) ->
            targetScope.$emit 'event:loginConfirmed', response.user

          .error (response, status) ->
            targetScope.$broadcast 'event:loginDenied', response

      logout: () ->
        $http
          .post('/api/authorization/logout', {}, config)
          .success (response) ->
            scope.$broadcast 'event:logoutConfirmed'
