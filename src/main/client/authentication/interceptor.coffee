angular
  .module 'tracker.authentication'

  .factory 'httpInterceptor', Array '$rootScope', '$q', ($rootScope, $q) ->
    result =
      request: (request) ->
        if ! request.url.match(/^\/api\/authentication\b/)
          $rootScope.$emit "event:startSpinner"
        request

      response: (response) ->
        $rootScope.$emit "event:stopSpinner"

        if response.status == 200 and response.data?.user? and ! $rootScope.user?
          $rootScope.user = response.data.user

        response

      responseError: (response) ->
        $rootScope.$emit "event:stopSpinner"
        status = response.status

        if status == 0
          $rootScope.$emit "event:httpError", {response: response, message: "Unexpected error from service handling #{response.config.method} #{response.config.url}"}

        else if status == 500
          $rootScope.$emit "event:httpError", {response: response, message: "Unexpected error from service handling #{response.config.method} #{response.config.url}: #{response.data.error}"}

        else if status == 401
          deferred = $q.defer()
          if response.config.url.match(/^\/api\/authorization\b/)
            return $q.reject response
          else
            req = {config: response.config, deferred: deferred}
            challenge = response.headers('www-authenticate')
            $rootScope.requests401.push(req)
            $rootScope.$broadcast 'event:loginRequired', {challenge: challenge}
            deferred.promise
        else
          $q.reject response
