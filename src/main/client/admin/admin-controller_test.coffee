describe 'StudyEditorController', ->
  studyEditorController = null
  scope = null
  $timeout = null
  $httpBackend = null

  beforeEach module('tracker.admin')

  beforeEach inject ($injector) ->
    scope = $injector.get('$rootScope')
    $httpBackend = $injector.get('$httpBackend')
    $timeout = $injector.get('$timeout')

    studyEditorController = ->
      $injector.get('$controller')('StudyEditorController', $scope: scope, $stateParams: {studyName: 'DEMO', viewName: 'track'})

    $httpBackend
      .when 'GET', '/api/authorization/ping'
      .respond {"data" : {"user" : {"username" : "guest"}}}

    $httpBackend
      .when 'GET', '/api/studies/DEMO/schema'
      .respond {
        "attributes": [
          {id:1, study_id:1, name:"dateEntered", label:"Date Entered", type:"date", rank:1, options:null},
          {id:2, study_id:1, name:"patientId", label:"Patient ID", type:"string", rank:2, options: {unique:true, display:"pin_left"}},
          {id:3, study_id:1, name:"patientInitials", label:"Patient Initials", type:"string", rank:3, options:null}
        ],
        "studyId": 1,
        "studyName": "DEMO",
        "views": [
          {id:1, study_id:1, name:"manage"},
          {id:2, study_id:1, name:"track"}
        ]
      }

    scope.httpBackend = $httpBackend


  it 'should initialize the study editor correctly', () ->

    studyEditorController()
    scope.$digest()
    scope.httpBackend.flush()

    scope.attributeTypes.should.be.instanceof(Array).and.have.lengthOf(4)
    scope.attributeTypes[0].should.have.property('id', 'string')
    scope.attributeTypes[1].should.have.property('id', 'boolean')
    scope.attributeTypes[2].should.have.property('id', 'date')
    scope.attributeTypes[3].should.have.property('id', 'option')

    scope.attributeDisplays.should.be.instanceof(Array).and.have.lengthOf(4)
    scope.attributeDisplays[0].should.have.property('id', 'normal')
    scope.attributeDisplays[1].should.have.property('id', 'pin_left')
    scope.attributeDisplays[2].should.have.property('id', 'pin_right')
    scope.attributeDisplays[3].should.have.property('id', 'hidden')


  it 'should allow attributes to be selected', () ->

    studyEditorController()
    scope.$digest()
    scope.httpBackend.flush()

    should.not.exist(scope.selectedAttribute)

    scope.selectAttribute scope.study.attributes[1]

    should.exist(scope.selectedAttribute)
    scope.selectedAttribute.should.have.property('name', 'patientId')

  ## Check we can modify a single attribute
  it 'should allow attributes to be modified', () ->

    $httpBackend
      .when 'PUT', '/api/studies/IMPACT/schema'
      .respond {
        "attributes": [
          {id:1, study_id:1, name:"dateEntered", label:"Date Entered", type:"date", rank:1, options:null},
          {id:2, study_id:1, name:"patientId", label:"Modified Patient ID", type:"string", rank:2, options: {unique:true, display:"pin_left"}},
          {id:3, study_id:1, name:"patientInitials", label:"Patient Initials", type:"string", rank:3, options:null}
        ],
        "studyId": 1,
        "studyName": "DEMO",
        "views": [
          {id:1, study_id:1, name:"manage"},
          {id:2, study_id:1, name:"track"}
        ]
      }

    studyEditorController()
    scope.$digest()
    scope.httpBackend.flush()

    should.not.exist(scope.selectedAttribute)

    scope.selectAttribute scope.study.attributes[1]

    should.exist(scope.selectedAttribute)
    scope.selectedAttribute.label = "Modified Patient ID"

    scope.save()
    scope.httpBackend.flush()

    scope.$digest()
    should.not.exist(scope.selectedAttribute)

    scope.selectAttribute scope.study.attributes[1]
    scope.selectedAttribute.label = "Modified Patient ID"


  ## Check we can delete a single attribute
  it 'should allow attributes to be deleted', () ->

    $httpBackend
      .when 'PUT', '/api/studies/IMPACT/schema'
      .respond {
        "attributes": [
          {id:1, study_id:1, name:"dateEntered", label:"Date Entered", type:"date", rank:1, options:null},
          {id:3, study_id:1, name:"patientInitials", label:"Patient Initials", type:"string", rank:3, options:null}
        ],
        "studyId": 1,
        "studyName": "DEMO",
        "views": [
          {id:1, study_id:1, name:"manage"},
          {id:2, study_id:1, name:"track"}
        ]
      }

    studyEditorController()
    scope.$digest()
    scope.httpBackend.flush()

    should.not.exist(scope.selectedAttribute)

    scope.selectAttribute scope.study.attributes[1]

    should.exist(scope.selectedAttribute)
    scope.deleteAttribute scope.selectedAttribute

    scope.study.attributes.should.be.instanceof(Array).and.have.lengthOf(2)

    scope.save()
    scope.httpBackend.flush()

    scope.$digest()
    should.not.exist(scope.selectedAttribute)

    scope.study.attributes.should.be.instanceof(Array).and.have.lengthOf(2)
