angular
  .module 'tracker.admin'

  .controller 'CreateStudyController', Array '$scope', '$http', '$state', ($scope, $http, $state) ->

    $scope.study = {}
    $scope.alerts = []

    ## Creating a new study ought to be a simple POST to the main studies resource...
    ## Yes, I said "ought"  -- it isn't quite that simple as we really need to make the
    ## role representation consistent. 

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.create = (study) ->
      $scope.alerts = []
      $http
        .post "/api/studies", study
        .success (response) ->
          $state.go 'adminStudy', {studyName: $scope.study.name}
        .error (response, status) ->
          $scope.alerts.push {type: 'danger', msg: response}


  .controller 'AdminUsersController', Array '$scope', '$http', '$state', ($scope, $http, $state) ->

    $scope.totalItems = 0
    $scope.pageSize = 10
    $scope.currentPage = 1

    $scope.users = []
    $scope.username = ''

    $scope.alerts = []

    $scope.pageChanged = () ->
      queryOptions = {page: $scope.currentPage, pageSize: $scope.pageSize}
      queryOptions.q = $scope.username if $scope.username?
      $http
        .get '/api/authentication/user', {params: queryOptions}
        .success (response) ->
          $scope.users = response.users
          $scope.totalItems = response.counts.total
          foundExactMatch = false
          for user in response.users
            if $scope.username == user.username
              foundExactMatch = true
              break
          $scope.foundExactMatch = foundExactMatch

    $scope.$watch 'username', (value) ->
      $scope.pageChanged()

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.createUser = (username) ->
      $scope.alerts = []
      $http
        .post '/api/authentication/user', {username: username}
        .success (response) ->
          $state.go 'adminUser', {username: username}
        .error (response) ->
          $scope.alerts.push {type: 'danger', msg: response}

    $scope.pageChanged()


  .controller 'AdminUserController', Array '$scope', '$stateParams', '$http', ($scope, $stateParams, $http) ->

    ## Don't expose $stateParams directly (we could!) but instead, do a server query on user
    ## settings, which meab\ns we can validate and get user information according to permissions.
    ## In effect, we need to allow users to get hold of their own information. And that's okay.
    ## Admin users (not study admin users) can also get hold of this. However, even they should 
    ## not be able to set a password. What they can do is amend the email address, which is
    ## probably going to be important. They should also be able to add users.

    $scope.enabled = {}
    $scope.user = undefined
    originalUser = undefined
    $scope.modified = false
    $scope.alerts = []

    $scope.$watchCollection 'user', (newValue, oldValue) ->
      if ! angular.equals newValue, originalUser
        $scope.modified = true

    $scope.saveUser = (user) ->
      user = angular.copy(user)
      $scope.user.currentPassword = ''
      $scope.user.password = ''
      $scope.user.verify = ''
      $scope.alerts = []
      for own k, v of user
        if ! $scope.enabled[k]
          delete user[k]
      $http
        .put "/api/authentication/user/#{encodeURIComponent($stateParams.username)}", user
        .success (response) ->
          $scope.alerts.push {type: 'success', msg: "Successfully saved"}
        .error (response) ->
          message = if response.error? then response.error else response
          $scope.alerts.push {type: 'danger', msg: message}

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.reset = (user) ->
      $http
        .put "/api/authentication/user/#{encodeURIComponent($stateParams.username)}/reset", {}
        .success (response) ->
          $scope.alerts.push {type: 'success', msg: "Password successfully reset. New password is: " + response.data.password}
        .error (message) ->
          $scope.alerts.push {type: 'danger', msg: message}

    $http
      .get "/api/authentication/user/#{encodeURIComponent($stateParams.username)}"
      .success (data) ->
        $scope.user = data.user
        $scope.enabled = data.enabled
        originalUser = angular.copy($scope.user)
        $scope.modified = false


  ## Some refactoring of the editing here, mainly to make the UI better. Basically, each controller
  ## should notify when something has changed as an event, and should handle appropriate events to
  ## reset. 
  .controller 'RoleEditorController', Array '$scope', ($scope) ->

    $scope.selectedRole = undefined
    originalSelectedRole = undefined
   
    $scope.selectRole = (role) ->
      $scope.selectedRole = role
      originalSelectedRole = angular.copy($scope.selectedRole)

    $scope.deleteRole = (role) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined
      $scope.study.roles = $scope.study.roles.filter (att) -> att != role
      $scope.$emit 'admin:modified'

    $scope.newRole = () ->
      newRole = {name: 'unnamed', description: 'Untitled Role'}
      $scope.study.roles.unshift newRole
      $scope.selectedRole = newRole
      originalSelectedRole = angular.copy($scope.selectedRole)
      $scope.$emit 'admin:modified'

    $scope.$watchCollection 'selectedRole', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedRole)
        $scope.$emit 'admin:modified'

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedRole = undefined
      originalSelectedRole = undefined


  ## Some refactoring of the editing here, mainly to make the UI better. Basically, each controller
  ## should notify when something has changed as an event, and should handle appropriate events to
  ## reset. 
  .controller 'ViewEditorController', Array '$scope', '$state', ($scope, $state) ->

    $scope.selectedView = undefined
    originalSelectedView = undefined
   
    $scope.selectView = (view) ->
      $scope.selectedView = view
      originalSelectedView = angular.copy($scope.selectedView)

    $scope.$watchCollection 'selectedView', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedView)
        $scope.$emit 'admin:modified'

    $scope.deleteView = (view) ->
      $scope.selectedView = undefined
      originalSelectedView = undefined
      $scope.study.views = $scope.study.views.filter (att) -> att != view
      $scope.$emit 'admin:modified'

    $scope.newView = () ->
      newView = {name: 'unnamed', description: 'Untitled View'}
      $scope.study.views.unshift newView
      $scope.selectedView = newView
      originalSelectedView = angular.copy($scope.selectedView)
      $scope.$emit 'admin:modified'

    $scope.editView = (view) ->
      $state.go 'studyViewManage', {studyName: $scope.study.name, viewName: view.name}

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedView = undefined
      originalSelectedView = undefined


  .controller 'AttributeEditorController', Array '$scope', ($scope) ->
    
    $scope.selectedAttribute = undefined
    originalSelectedAttribute = undefined

    $scope.$on 'admin:reset', (e) ->
      $scope.selectedAttribute = undefined
      originalSelectedAttribute = undefined

    ## Detect changes to attributes and notify as modified
    $scope.$watchCollection 'selectedAttribute', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedAttribute)
        $scope.$emit 'admin:modified'

    $scope.$watchCollection 'selectedAttribute.options.tags', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedAttribute?.options?.tags)
        $scope.$emit 'admin:modified'

    $scope.$watchCollection 'selectedAttribute.options.values', (newValue, oldValue) ->
      if ! angular.equals(newValue, originalSelectedAttribute?.options?.values)
        $scope.$emit 'admin:modified'

    $scope.selectAttribute = (attribute) ->
      $scope.selectedAttribute = attribute
      originalSelectedAttribute = angular.copy($scope.selectedAttribute)

    $scope.deleteAttribute = (attribute) ->
      $scope.selectedAttribute = undefined
      originalSelectedAttribute = undefined
      $scope.study.attributes = $scope.study.attributes.filter (att) -> att != attribute
      $scope.$emit 'admin:modified'

    $scope.newAttribute = () ->
      newAttribute = {id: undefined, name: 'unnamed', label: 'Untitled Attribute'}
      $scope.study.attributes.push newAttribute
      $scope.selectedAttribute = newAttribute
      originalSelectedAttribute = angular.copy($scope.selectedAttribute)
      $scope.$emit 'admin:modified'

    $scope.attributeTypes = [
      {id: 'string', text: "String"}
      {id: 'boolean', text: "Boolean"}
      {id: 'date', text: "Date"}
      {id: 'option', text: "Option"}
    ]

    $scope.attributeTags = [
      {id: 'identifiable', text: "Identifiable"}
    ]

    $scope.attributeDisplays = [
      {id: 'normal', text: "Normal"}
      {id: 'pin_left', text: "Pin left"}
      {id: 'pin_right', text: "Pin right"}
      {id: 'hidden', text: "Hidden"}
    ]


  .controller 'StudyEditorController', Array '$scope', '$http', '$stateParams', '$q', '$timeout', ($scope, $http, $stateParams, $q, $timeout) ->

    $scope.study = undefined
    originalStudy = undefined

    $scope.modified = false
    $scope.alerts = []
    $scope.params = $stateParams

    $scope.$on 'admin:modified', (e) ->
      $scope.modified = true

    $scope.reset = () ->
      $scope.$broadcast 'admin:reset'

      $scope.study = originalStudy
      originalStudy = angular.copy($scope.study)

      $timeout () ->
        $scope.modified = false

    $scope.closeAlert = (index) ->
      $scope.alerts.splice(index, 1)

    $scope.save = () ->
      $scope.alerts = []
      $http
        .put("/api/studies/#{encodeURIComponent($stateParams.studyName)}/schema", $scope.study)
        .success (response) ->
          originalStudy = response
          $scope.reset()
        .error (response) ->
          message = response?.error or response
          $scope.alerts.push {type: 'danger', msg: message}

    $http
      .get("/api/studies/#{encodeURIComponent($stateParams.studyName)}/schema")
      .success (schema) ->
        originalStudy = schema
        $scope.reset()
      .error (error) ->
        console.log "Error", error
