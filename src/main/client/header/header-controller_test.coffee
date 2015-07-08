describe 'HeaderController', ->
  headerController = null
  scope = null

  beforeEach module('tracker')

  beforeEach inject ($injector) ->
    scope = $injector.get('$rootScope')

    headerController = ->
      $injector.get('$controller')('HeaderController', $scope:scope)

  it 'root path should be active', ->
    headerController()
    scope.isActive('/').should.be.true

  it 'non-root path should not be active', ->
    headerController()
    scope.isActive('/random').should.be.false
