describe 'GridController', ->
  gridController = null
  gridElement = null
  scope = null
  $timeout = null
  $httpBackend = null
  socketEventEmitter = null
  gridCellFinder = null

  makeRecords = (n) ->
    n ?= 2
    pad = (n) -> n = ("000" + n).slice(-2) 
    makeRecord = (n) -> {id: n, "patientId": "DEMO-" + pad(n), "dateEntered": makeDate(n)}
    makeDate = (n) -> (new Date(2014, 9, n)).toISOString().substring(0, 10)

    return (makeRecord(i) for i in [1..n])

  makeResponse = (records, count) ->
    count ?= records.length
    result =
      attributes: [
        {"id":1, "study_id":1, "name":"dateEntered", "label":"Date Entered", "type":"date", "rank":0, "options":null}
        {"id":2, "study_id":1, "name":"patientId", "label":"Patient ID", "type":"string", "rank":1, "options":null}
      ]
      counts: {total: count}
      records: records
      study: {
        studyId: 1, 
        studyName: "DEMO", 
        viewId: 1, 
        viewName: "manage", 
        identifierAttribute: "patientId", 
        identifierAttributeLabel: "Patient ID"
      }

  httpGridData = (n) ->
    $httpBackend
      .when 'GET', '/api/studies/DEMO/views/track?page=1&pageSize=50'
      .respond makeResponse makeRecords(n)


  beforeEach module('tracker')

  beforeEach inject ($injector) ->
    scope = $injector.get('$rootScope')
    $httpBackend = $injector.get('$httpBackend')
    $timeout = $injector.get('$timeout')
    $compile = $injector.get('$compile')
    gridCellFinder = $injector.get('gridCellFinder')
    mockEventEmitter = $injector.get('mockEventEmitter')
    socketEventEmitter = new mockEventEmitter()
    socketFactory = () -> socketEventEmitter

    gridController = ->
      $injector.get('$controller')('GridController', $scope: scope, $stateParams: {studyName: 'DEMO', viewName: 'track'}, socketFactory: socketFactory)

    $httpBackend
      .when 'GET', '/api/authentication/ping'
      .respond {"data" : {"user" : {"username" : "guest"}}}

    gridElement = ->
      element = angular.element('<div class="tracker-trid grid-style" ui-grid="gridOptions" ui-grid-pinning ui-grid-cellnav ui-grid-edit ui-grid-resize-columns ui-grid-infinite-scroll></div>')
      template = $compile(element)(scope)
      scope.$digest()
      element

    scope.httpBackend = $httpBackend


  it 'should initialize the grid correctly from a service endpoint', () ->

    httpGridData()
    gridController()
    element = gridElement()
    scope.$digest()
    scope.httpBackend.flush()
    
    should.exist(scope.gridColumns)
    scope.gridColumns.should.be.instanceof(Array).and.have.lengthOf(2)
    scope.gridColumns[0].field.should.equal("dateEntered")
    scope.gridColumns[0].displayName.should.equal("Date Entered")
    scope.gridColumns[0].enableCellEdit.should.equal(true)

    should.exist(scope.gridData)
    scope.gridData.should.be.instanceof(Array).and.have.lengthOf(2)
    scope.gridData[0].patientId.should.equal("DEMO-01")
    scope.gridData[0].id.should.equal(1)
    scope.gridData[0].dateEntered.should.equal("2014-10-01")


  it 'should forward a filtered request server-side', () ->

    httpGridData()
    $httpBackend
      .when 'GET', '/api/studies/DEMO/views/track?page=1&pageSize=50&q=imp-01'
      .respond makeResponse [
          {"id":1, "dateEntered":"2014-08-20", "patientId": "DEMO-01"}
      ], 2 

    gridController()
    element = gridElement()
    scope.$digest()
    scope.filterText = 'DEMO-01'
    scope.$digest()
    $timeout.flush(1000)
    scope.httpBackend.flush()
    should.exist(scope.gridColumns)


  it 'should notify the service on cell editing', () ->

    httpGridData()
    $httpBackend
      .when 'PUT', '/api/studies/DEMO/views/track/entities/1/patientId', '{"value":"DEMO-03"}'
      .respond makeResponse [
          {"id":2, "dateEntered":"2014-08-24", "patientId": "DEMO-03"}
      ], 2 


    gridController()
    gridElement()
    scope.$digest()
    scope.httpBackend.flush()
    
    should.exist(scope.gridApi)
    scope.gridData[0].patientId = "DEMO-03"
    scope.gridApi.edit.raise.afterCellEdit scope.gridData[0], scope.gridColumns[1], "DEMO-03", "DEMO-01"

    scope.httpBackend.flush()


  it 'should correctly respond to the welcome event', () ->

    httpGridData()
    gridController()
    gridElement()
    scope.$digest()
    scope.httpBackend.flush()

    gotJoinEvent = null

    socketEventEmitter.on 'join', (object) ->
      gotJoinEvent = object

    socketEventEmitter.emit 'welcome', {}

    should.exist(gotJoinEvent)
    gotJoinEvent.should.have.property('scope', 'DEMO')


  it 'should correctly respond to the userConnected event', () ->

    httpGridData()
    gridController()
    scope.$digest()

    socketEventEmitter.emit 'userConnected', {name: 'stuart'}

    scope.currentUsers.should.be.instanceof(Array).and.have.lengthOf(1)
    scope.currentUsers[0].should.equal('stuart')


  it 'should correctly respond to the userDisconnected event', () ->

    httpGridData()
    gridController()
    scope.$digest()

    scope.currentUsers = ['guest', 'stuart', 'other']

    socketEventEmitter.emit 'userDisconnected', {name: 'stuart'}

    scope.currentUsers.should.be.instanceof(Array).and.have.lengthOf(2)
    scope.currentUsers[0].should.equal('guest')
    scope.currentUsers[1].should.equal('other')


  it 'should correctly respond to the userConnected event without duplicates', () ->

    httpGridData()
    gridController()
    scope.$digest()

    scope.currentUsers = ['other', 'stuart']

    socketEventEmitter.emit 'userConnected', {name: 'stuart'}

    scope.currentUsers.should.be.instanceof(Array).and.have.lengthOf(2)
    scope.currentUsers[0].should.equal('other')
    scope.currentUsers[1].should.equal('stuart')


  it 'should correctly respond to the gridEdit event on a cell', () ->

    httpGridData()
    $httpBackend
      .when 'GET', '/api/studies/DEMO/views/track/entities/2'
      .respond makeResponse [
          {"id":2, "dateEntered":"2014-08-24", "patientId": "DEMO-09"}
      ], 2 

    gridController()
    gridElement()
    scope.$digest()
    scope.httpBackend.flush()

    scope.currentUsers = ['guest', 'stuart', 'other']

    socketEventEmitter.emit 'gridEdit', {type: 'cell', user: 'stuart', params: {id: 2, field: 'patientId'}}
    scope.httpBackend.flush()

    ## Immediately after this request, the grid element class should be set
    scope.gridData.should.be.instanceof(Array).and.have.lengthOf(2)
    scope.gridData[1].should.have.property 'patientId', 'DEMO-09'

    ## The cell element class should also have changed to show which user made the change
    element = gridCellFinder  scope.gridApi.grid, 2, 'patientId'
    element.hasClass('editedCellUser-1').should.be.true

    ## However, after a timeout, that should go away
    $timeout.flush(5000)
    element.hasClass('editedCellUser-1').should.equal(false)


  it 'should ignore a gridEdit event on a record when the entity is not retrieved', () ->

    httpGridData()
    $httpBackend
      .when 'GET', '/api/studies/DEMO/views/track?page=1&pageSize=50&pages=1'
      .respond makeResponse [
          {"id":1, "dateEntered":"2014-08-20", "patientId": "DEMO-01"}, 
          {"id":2, "dateEntered":"2014-08-24", "patientId": "DEMO-02"}
      ], 3 

    gridController()
    gridElement()
    scope.$digest()
    scope.httpBackend.flush()

    scope.currentUsers = ['guest', 'stuart', 'other']

    socketEventEmitter.emit 'gridEdit', {type: 'record', user: 'stuart', params: {id: 3}}
    scope.httpBackend.flush()

    scope.gridData.should.be.instanceof(Array).and.have.lengthOf(2)
    scope.gridData[0].should.have.property 'id', 1
    scope.gridData[1].should.have.property 'id', 2


  it 'should correctly respond to the gridEdit event on a page boundary', () ->

    httpGridData(50)
    modifiedRecords = makeRecords(50)
    modifiedRecords.pop()
    modifiedRecords.unshift {"id":99, "patientId": "DEMO-99"}
    modifiedResponse = makeResponse modifiedRecords, 51

    $httpBackend
      .when 'GET', '/api/studies/DEMO/views/track?page=1&pageSize=50&pages=1'
      .respond modifiedResponse

    gridController()
    gridElement()
    scope.$digest()
    scope.httpBackend.flush()

    scope.currentUsers = ['guest', 'stuart', 'other']

    socketEventEmitter.emit 'gridEdit', {type: 'record', user: 'stuart', params: {id: 99}}
    scope.httpBackend.flush()

    ## Key test, we should have lost items here...
    scope.gridData.should.be.instanceof(Array).and.have.lengthOf(50)
    scope.gridData[0].should.have.property 'id', 99
    scope.gridData[1].should.have.property 'id', 1
    scope.gridData[2].should.have.property 'id', 2

    scope.gridData[0].should.have.property 'patientId', 'DEMO-99'


## The next step is to test with infinite scrolling. When we get a new record notification we should request a block the same
## size as what we have currently in view. That may push out an item currently displayed. That we also need to test, because if
## we don't, we'll end up getting an element twice, potentially. This is a bit harder as we need a bigger and more realistic
## sample test data set to start with. 


## We also have to be cautious when handling new records during editing. 

