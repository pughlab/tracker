describe 'Grid services', ->

  describe 'renderHistory', ->

    renderHistory = null

    beforeEach module('tracker.grid')

    beforeEach inject ($injector) ->
      renderHistory = $injector.get('renderHistory')

    it 'should format an empty history as an empty string', () ->
      history = renderHistory []
      should.exist(history)
      history.should.equal('')

    it 'should format a more detailed history appropriately', () ->
      history = renderHistory [
        { active: true, value: 'breast', modified: 1415206085877, modified_by: 1, username: 'admin' },
        { active: false, value: 'other', modified: 1415206085859, modified_by: 2, username: 'anca' },
        { active: false, value: 'unknown', modified: 1415206085840, modified_by: 4, username: 'stuart' }
      ]
      should.exist(history)

      element = angular.element(history)
      element.length.should.equal(3)
      element.eq(0).text().should.equal "admin, Wed  5 Nov 11:48:05 2014: Changed from 'other' to 'breast'"
      element.eq(1).text().should.equal "anca, Wed  5 Nov 11:48:05 2014: Changed from 'unknown' to 'other'"
      element.eq(2).text().should.equal "stuart, Wed  5 Nov 11:48:05 2014: Changed from 'blank\' to 'unknown\'"

  describe 'gridColumnDefinitionGenerator', ->

    gridColumnDefinitionGenerator = null

    beforeEach module('tracker.grid')

    beforeEach inject ($injector) ->
      gridColumnDefinitionGenerator = $injector.get('gridColumnDefinitionGenerator')


    it 'should generate a string field appropriately', () ->

      columns = [{
        name: 'coffee',
        type: 'string',
        label: 'Coffee'
      }]

      definitions = gridColumnDefinitionGenerator(columns, {})
      definitions.should.be.instanceof(Array).and.have.lengthOf(1)
      definitions[0].should.have.property('field', 'coffee')
      definitions[0].should.have.property('displayName', 'Coffee')
      definitions[0].should.have.property('cellTemplate').and.match /cell-string/


    it 'should generate an option field appropriately', () ->

      columns = [{
        name: 'coffee',
        type: 'option',
        label: 'Coffee',
        options: {values: ['Americano', 'Latte', 'Mocha']}
      }]

      definitions = gridColumnDefinitionGenerator(columns, {})
      definitions.should.be.instanceof(Array).and.have.lengthOf(1)
      definitions[0].should.have.property('field', 'coffee')
      definitions[0].should.have.property('displayName', 'Coffee')
      definitions[0].should.have.property('cellTemplate').and.match /cell-option/
      definitions[0].should.have.property('options').and.eql ['Americano', 'Latte', 'Mocha']


    it 'should generate a boolean field appropriately', () ->

      columns = [{
        name: 'likeCoffee',
        type: 'boolean',
        label: 'Likes Coffee?'
      }]

      definitions = gridColumnDefinitionGenerator(columns, {})
      definitions.should.be.instanceof(Array).and.have.lengthOf(1)
      definitions[0].should.have.property('field', 'likeCoffee')
      definitions[0].should.have.property('displayName', 'Likes Coffee?')
      definitions[0].should.have.property('cellTemplate').and.match /cell-boolean/


    it 'should generate a date field appropriately', () ->

      columns = [{
        name: 'coffeeTime',
        type: 'date',
        label: 'Coffee Time'
      }]

      definitions = gridColumnDefinitionGenerator(columns, {})
      definitions.should.be.instanceof(Array).and.have.lengthOf(1)
      definitions[0].should.have.property('field', 'coffeeTime')
      definitions[0].should.have.property('displayName', 'Coffee Time')
      definitions[0].should.have.property('cellTemplate').and.match /cell-date/



