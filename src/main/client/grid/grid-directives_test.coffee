describe 'Grid directives', ->

  element = null
  scope = null
  $compile = null

  beforeEach module('tracker.grid')

  beforeEach inject ($injector) ->
    scope = $injector.get('$rootScope')
    $compile = $injector.get('$compile')


  describe 'cellNotes', ->

    it 'should set classes from tags', () ->
      element = "<div class='ui-grid-cell-contents' cell-notes='entity.notes'></div>"
      element = $compile(element)(scope)

      scope.entity = {notes: {tags: ['label2', 'label3']}}
      scope.$digest()

      element.hasClass('label1').should.be.false
      element.hasClass('label2').should.be.true
      element.hasClass('label3').should.be.true


  describe 'cellString', ->

    it 'should set content for a value', () ->
      element = "<div class='ui-grid-cell-contents' cell-string cell-data='entity.field'></div>"
      element = $compile(element)(scope)

      scope.entity = {field: "IMP-345"}
      scope.$digest()

      element.text().should.be.equal("IMP-345")


  describe 'cellOption', ->

    it 'should set content for a value', () ->
      element = "<div class='ui-grid-cell-contents' cell-option cell-data='entity.field'></div>"
      element = $compile(element)(scope)

      scope.entity = {field: "St. Michael's"}
      scope.$digest()

      element.text().should.be.equal("St. Michael's")


  describe 'cellBoolean', ->

    it 'should set content for a true value', () ->
      element = "<div class='ui-grid-cell-contents' cell-boolean cell-data='entity.field'></div>"
      element = $compile(element)(scope)

      scope.entity = {field: true}
      scope.$digest()

      element.text().should.be.equal("Yes")


    it 'should set content for a false value', () ->
      element = "<div class='ui-grid-cell-contents' cell-boolean cell-data='entity.field'></div>"
      element = $compile(element)(scope)

      scope.entity = {field: false}
      scope.$digest()

      element.text().should.be.equal("No")


    it 'should set content for a null value', () ->
      element = "<div class='ui-grid-cell-contents' cell-boolean cell-data='entity.field'></div>"
      element = $compile(element)(scope)

      scope.entity = {field: null}
      scope.$digest()

      element.text().should.be.equal("")


  describe 'cellDate', ->

    it 'should set content for a value', () ->
      element = "<div class='ui-grid-cell-contents' cell-date cell-data='entity.field'></div>"
      element = $compile(element)(scope)

      scope.entity = {field: "2014-01-25"}
      scope.$digest()

      element.text().should.be.equal("2014-01-25")


    it 'should set content for a null value', () ->
      element = "<div class='ui-grid-cell-contents' cell-date cell-data='entity.field'></div>"
      element = $compile(element)(scope)

      scope.entity = {field: ""}
      scope.$digest()

      element.text().should.be.equal("")

