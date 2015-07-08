describe 'StudiesController', ->
  studiesController = null
  scope = null

  beforeEach module('tracker')

  beforeEach inject ($injector) ->
    scope = $injector.get('$rootScope')
    $httpBackend = $injector.get('$httpBackend')

    studiesController = ->
      $injector.get('$controller')('StudiesController', $scope:scope)

    $httpBackend
      .when 'GET', '/api/authorization/ping'
      .respond {"data" : {"user" : {"username" : "guest"}}}

    $httpBackend
      .when 'GET', '/api/studies'
      .respond {
        "studies": [{
          "id":1, 
          "name":"DEMO", 
          "views":[
            {"id":1, "name":"manage", "access":{"read":true, "modify":true}},
            {"id":2, "name":"track", "access":{"read":true, "modify":true}}
          ],
          "access":{"read":true, "modify":true}
        }]
      }

    scope.httpBackend = $httpBackend

  it 'should retrieve a list of studies', ->
    studiesController()
    scope.$digest()
    scope.httpBackend.flush()
    should.exist(scope.studies)
    scope.studies.studies.length.should.equal(1)
    scope.studies.studies[0].name.should.equal("IMPACT")
