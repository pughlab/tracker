describe 'StudyController', ->
  studyController = null
  scope = null

  beforeEach module('tracker')

  beforeEach inject ($injector) ->
    scope = $injector.get('$rootScope')
    $httpBackend = $injector.get('$httpBackend')
    $stateParams = $injector.get('$stateParams')

    $stateParams['studyName'] = 'DEMO'

    studyController = ->
      $injector.get('$controller')('StudyController', $scope:scope)

    $httpBackend
      .when 'GET', '/api/authentication/ping'
      .respond {"data" : {"user" : {"username" : "guest"}}}

    $httpBackend
      .when 'GET', '/api/studies/DEMO/views'
      .respond {
        "views":[
          {"id":1, "name":"manage", "access":{"read":true, "modify":true}},
          {"id":2, "name":"track", "access":{"read":true, "modify":true}}
        ]
      }

    scope.httpBackend = $httpBackend

  it 'should retrieve a study and its views', ->
    studyController()
    scope.$digest()
    scope.httpBackend.flush()
    should.exist(scope.study)
    scope.study.views.should.be.instanceof(Array).and.have.lengthOf(2)
