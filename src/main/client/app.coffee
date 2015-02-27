angular
  .module 'tracker', [
    'ui.bootstrap'
    'ui.router'
    'toggle-switch'
    'tracker.filters'
    'tracker.header'
    'tracker.pages'
    'tracker.studies'
    'tracker.grid'
    'tracker.authentication'
    'tracker.admin'
    'tracker.account'
    'tracker.error'
    'tracker-templates'
  ]


  .config Array '$stateProvider', ($stateProvider) -> 
    $stateProvider
      .state 'home',
        controller: 'StudiesController'
        templateUrl: '/tracker/studies/studies.html'
        url: '/'
      .state 'study',
        controller: 'StudyController'
        templateUrl: '/tracker/studies/study-views.html'
        url: '/studies/:studyName'
      .state 'studyView',
        controller: 'GridController'
        templateUrl: '/tracker/grid/grid.html'
        url: '/studies/:studyName/views/:viewName'
      .state 'adminStudy',
        controller: 'StudyEditorController'
        templateUrl: '/tracker/admin/admin.html'
        url: '/admin/:studyName/edit'
      .state 'adminView',
        controller: 'ViewController'
        templateUrl: '/tracker/admin/admin-view.html'
        url: '/admin/:studyName/views/:viewName/edit'
      .state 'logout',
        controller: 'PageController'
        templateUrl: '/tracker/authentication/logout.html'
        url: '/logout'
      .state 'account',
        templateUrl: '/tracker/account/account.html'
      .state 'account.password',
        controller: 'AccountController'
        templateUrl: '/tracker/account/password.html'
      .state 'account.settings',
        controller: 'AccountController'
        templateUrl: '/tracker/account/settings.html'
        url: '/account/:username'
      .state 'adminCreate',
        controller: 'CreateStudyController'
        templateUrl: '/tracker/admin/admin-new-study.html'
        url: '/admin/new/create'
      .state 'adminUsers',
        controller: 'AdminUsersController'
        templateUrl: '/tracker/admin/admin-users.html'
        url: '/admin/users'
      .state 'adminUser',
        controller: 'AdminUserController'
        templateUrl: '/tracker/admin/admin-user.html'
        url: '/admin/users/:username'
      .state 'error',
        controller: 'ErrorController'
        templateUrl: '/tracker/error/error.html'
        url: '/error'
        params: {
          message: "An error"
        }


  .config Array '$locationProvider', ($locationProvider) ->
    $locationProvider.html5Mode(true)
    $locationProvider.hashPrefix = "!"


  .config Array '$httpProvider', ($httpProvider) ->
    $httpProvider.interceptors.push 'httpInterceptor'


  .run Array '$rootScope', '$http', '$timeout', '$state', 'authenticationService', (scope, $http, $timeout, $state, authenticationService) ->

    class User
      constructor: (user) ->
        for own key, value of user
          this[key] = value

    ## Values needed to handle the authentication
    config =
      headers: {'Content-Type':'application/x-www-form-urlencoded; charset=UTF-8'}

    scope.requests401 = []
    scope.user = undefined

    scope.login = () -> scope.$emit "event:loginRequest"
    scope.logout = () -> scope.$emit "event:logoutRequest"

    scope.$on 'event:httpError', (evt, value) ->
      $state.go 'error', value

    scope.$on 'event:loginCancelled', () ->
      $state.go('logout')

    scope.$on 'event:logoutConfirmed', () ->
      scope.user = undefined
      $state.go('logout')

    scope.$on 'event:loginConfirmed', (event, user) ->
      scope.user = new User(user)

      $http.get('/api/authentication/can/system/system/system')
        .then (response) ->
          if response.data?
            scope.user.permissions = response.data

      if user.force_password_change
        return $state.go "adminUser", {username: user.username}

      retry = (req) ->
        $http(req.config).then (response) ->
          req.deferred.resolve(response)

      retry(request) for request in scope.requests401

    scope.$on 'event:loginRequest', (evt, username, password) ->
      authenticationService.login(evt.targetScope, username, password)

    scope.$on 'event:logoutRequest', (evt) ->
      authenticationService.logout()
      $state.go 'logout'

    # When we start the app, we might be on an unauthenticated route but still have a session
    # info available. This allows us to pick up the initial service level user. It should always
    # return a 200 status (i.e., not be restricted by authentication, and return the current user)
    # exactly like the login event system.
    authenticationService.ping()

  .run () ->

    ## Added code from: http://stackoverflow.com/a/16324762/2140998
    ## This prevents scrolling in the popover from bubbling. Handily, we can also use it to 
    ## stop the grid page from scrolling at its limits.
    jQuery(document).on 'DOMMouseScroll mousewheel', '.scrollable', (ev) ->
      $this = jQuery(@)
      scrollTop = @scrollTop
      scrollHeight = @scrollHeight
      height = $this.height()
      delta = if ev.type == 'DOMMouseScroll' then ev.originalEvent.detail * -40 else ev.originalEvent.wheelDelta
      up = delta > 0

      prevent = () ->
        ev.stopPropagation()
        ev.preventDefault()
        ev.returnValue = false
        false

      if !up && -delta > scrollHeight - height - scrollTop
        $this.scrollTop(scrollHeight)
        prevent()
      else if up && delta > scrollTop
        $this.scrollTop(0)
        prevent()
