angular
  .module 'tracker', [
    'ui.bootstrap'
    'ui.router'
    'toggle-switch'
    'tracker.admin'
    'tracker.filters'
    'tracker.header'
    'tracker.pages'
    'tracker.studies'
    'tracker.grid'
    'tracker.authentication'
    'tracker.admin'
    'tracker.error'
    'tracker.directives'
    'tracker-templates'
  ]


  .config Array '$stateProvider', ($stateProvider) ->
    $stateProvider
      .state 'home',
        controller: 'StudiesController'
        templateUrl: '/tracker/studies/studies.html'
        url: '/'
      .state 'about',
        controller: 'PageController'
        templateUrl: '/tracker/pages/about.html'
        url: '/about'
      .state 'study',
        controller: 'StudyController'
        templateUrl: '/tracker/studies/study-views.html'
        url: '/studies/:studyName'
      .state 'studyAbout',
        controller: 'StudyController'
        templateUrl: '/tracker/studies/study-about.html'
        url: '/studies/:studyName/about'
      .state 'studyView',
        controller: 'GridTableController'
        templateUrl: '/tracker/grid/table.html'
        url: '/studies/:studyName/view/:viewName'
      .state 'adminStudy',
        controller: 'StudyEditorController'
        templateUrl: '/tracker/admin/admin.html'
        abstract: true
        url: '/admin/:studyName'
        resolve:
          studyName: Array '$stateParams', ($stateParams) -> $stateParams.studyName
      .state 'adminStudy.settings',
        templateUrl: '/tracker/admin/admin-study-settings.html'
        url: ''
      .state 'adminStudy.attributes',
        controller: 'AttributeEditorController'
        templateUrl: '/tracker/admin/admin-study-attributes.html'
        url: '/attributes'
      .state 'adminStudy.roles',
        controller: 'StudyRoleEditorController'
        templateUrl: '/tracker/admin/admin-study-roles.html'
        url: '/roles'
      .state 'adminStudy.views',
        controller: 'ViewEditorController'
        templateUrl: '/tracker/admin/admin-study-views.html'
        url: '/views'
      .state 'adminStudy.about',
        controller: 'StudyAboutEditorController'
        templateUrl: '/tracker/admin/admin-study-about.html'
        url: '/about'
      .state 'adminView',
        controller: 'ViewController'
        templateUrl: '/tracker/admin/admin-view.html'
        url: '/admin/:studyName/views/:viewName/edit'
      .state 'logout',
        controller: 'PageController'
        templateUrl: '/tracker/authentication/logout.html'
        url: '/logout'
      .state 'login',
        controller: 'LoginController'
        templateUrl: '/tracker/authentication/login.html'
        params: { challenge : { value: "default" }, prompt : { value : "default" }}
        url: '/login'
      .state 'adminCreate',
        controller: 'CreateStudyController'
        templateUrl: '/tracker/admin/admin-new-study.html'
        url: '/admin/new/create'
      .state 'adminAudit',
        controller: 'AdminAuditController'
        templateUrl: '/tracker/admin/admin-audit.html'
        url: '/admin/audit/:studyName'
      .state 'error',
        controller: 'ErrorController'
        templateUrl: '/tracker/error/error.html'
        url: '/error'
        params: {
          message: "An error"
        }


  .config Array '$urlRouterProvider', ($urlRouterProvider) ->
    $urlRouterProvider.otherwise('/')


  .config Array '$locationProvider', ($locationProvider) ->
    $locationProvider.html5Mode(true)
    $locationProvider.hashPrefix = "!"


  .config Array '$httpProvider', ($httpProvider) ->
    $httpProvider.interceptors.push 'httpInterceptor'
    $httpProvider.defaults.withCredentials = true
    $httpProvider.defaults.useXDomain = true



  .run Array '$rootScope', '$interval', '$document', (scope, $interval, $document) ->

    IDLE_TIMEOUT = 15 * 60
    idleSecondsTimer = null
    idleSecondsCounter = 0

    resetCounter = () ->
      idleSecondsCounter = 0

    $document.on 'click', resetCounter
    $document.on 'mousemove', resetCounter
    $document.on 'keypress', resetCounter

    checkIdleTime = () ->
      idleSecondsCounter++
      if idleSecondsCounter > IDLE_TIMEOUT
        $interval.cancel idleSecondsTimer
        scope.$broadcast 'timeout:logout'

    idleSecondsTimer = $interval checkIdleTime, 1000


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
      console.log 'event:loginConfirmed'
      scope.user = new User(user)
      scope.requests401 = []
      $state.go('home')

    scope.$on 'event:loginRequest', (evt) ->
      $state.go 'home'

    scope.$on 'event:logoutRequest', (evt) ->
      authenticationService.logout()
      $state.go 'logout'

    scope.$on 'timeout:logout', (evt) ->
      authenticationService.logout()
      $state.go 'logout'


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
