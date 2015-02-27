describe 'Filters', ->

  $filter = null

  beforeEach module('tracker.filters')

  beforeEach inject ($injector) ->
    $filter = $injector.get('$filter')

  describe 'keys', ->

    it 'should return an empty list for an empty object', () ->

      keys = $filter('keys')
      keys({}).should.be.instanceof(Array).and.have.lengthOf(0)


    it 'should return a complete list for a detailed object', () ->

      keys = $filter('keys')
      keys({a: 1, b: 2, c: 3}).should.be.instanceof(Array).and.eql(['a', 'b', 'c'])


    it 'should return a complete list for a detailed object', () ->

      keys = $filter('keys')
      keys({a: 1, b: 2, c: 3}).should.be.instanceof(Array).and.eql(['a', 'b', 'c'])


  describe 'mapProperty', ->

    it 'should return an empty list for an empty list', () ->

      mapProperty = $filter('mapProperty')
      mapProperty([]).should.be.instanceof(Array).and.have.lengthOf(0)


    it 'should return a complete list for a list of objects', () ->

      mapProperty = $filter('mapProperty')
      mapProperty([{a: '1'}, {a: '2'}], 'a').should.be.instanceof(Array).and.eql(['1', '2'])


