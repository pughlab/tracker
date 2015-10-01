TrackerHandsontableColumnSorting = () ->

  plugin = @

  @init = (source) ->
    instance = this
    sortingSettings = instance.getSettings()
    [sortingColumn, sortingOrder] = [undefined, undefined]

    instance.trackerSortingEnabled = sortingSettings.trackerColumnSorting == true

    if instance.trackerSortingEnabled
      instance.sortIndex = []

      loadedSortingState = loadSortingState.call(instance)

      if typeof loadedSortingState != 'undefined'
        sortingColumn = loadedSortingState.sortColumn
        sortingOrder = loadedSortingState.sortOrder

      plugin.sortByColumn.call(instance, sortingColumn, sortingOrder)

      instance.sort = () ->
        args = Array.prototype.slice.call(arguments)
        plugin.sortByColumn.apply(instance, args)

      if typeof instance.getSettings().observeChanges == 'undefined'
        enableObserveChangesPlugin.call(instance)

      if source == 'afterInit'
        bindColumnSortingAfterClick.call(instance)

        instance.addHook('afterCreateRow', plugin.afterCreateRow)
        instance.addHook('afterRemoveRow', plugin.afterRemoveRow)
        instance.addHook('afterLoadData', plugin.init)

    else
      delete instance.sort

      instance.removeHook('afterCreateRow', plugin.afterCreateRow)
      instance.removeHook('afterRemoveRow', plugin.afterRemoveRow)
      instance.removeHook('afterLoadData', plugin.init)


  @setSortingColumn = (col, order) ->
    instance = this

    if typeof col == 'undefined'
      delete instance.sortColumn
      delete instance.sortOrder
      return
    else if instance.sortColumn == col && typeof order == 'undefined'
      instance.sortOrder =
        switch instance.sortOrder
          when undefined then true
          when true then false
          when false
            col = undefined
            undefined
    else
      instance.sortOrder = if typeof order != 'undefined' then order else true

    instance.sortColumn = col


  @sortByColumn = (col, order) ->
    instance = this

    plugin.setSortingColumn.call(instance, col, order)

    Handsontable.hooks.run(instance, 'beforeColumnSort', instance.sortColumn, instance.sortOrder)

    plugin.sort.call(instance)
    instance.render()

    saveSortingState.call(instance)

    Handsontable.hooks.run(instance, 'afterColumnSort', instance.sortColumn, instance.sortOrder)


  saveSortingState = () ->
    instance = this

    sortingState = {}

    if typeof instance.sortColumn != 'undefined'
      sortingState.sortColumn = instance.sortColumn

    if typeof instance.sortOrder != 'undefined'
      sortingState.sortOrder = instance.sortOrder

    if sortingState.hasOwnProperty('sortColumn') || sortingState.hasOwnProperty('sortOrder')
      Handsontable.hooks.run(instance, 'persistentStateSave', 'columnSorting', sortingState)


  loadSortingState = () ->
    instance = this
    storedState = {}
    Handsontable.hooks.run(instance, 'persistentStateLoad', 'columnSorting', storedState)

    storedState.value


  ## We should also be able to "unsort", i.e., revert back to a default ordering provided
  ## by the basic identifiers. Actually, we should probably also be using identifiers to
  ## make the sort actually stable, but enough of that for now.

  bindColumnSortingAfterClick = () ->
    instance = this

    countRowHeaders = () ->
      THs = instance.view.TBODY.querySelector('tr').querySelectorAll('th')
      THs.length

    getColumn = (target) ->
      TH = Handsontable.Dom.closest(target, 'TH')
      Handsontable.Dom.index(TH) - countRowHeaders()

    eventManager = Handsontable.eventManager(instance)
    eventManager.addEventListener instance.rootElement, 'click', (e) ->
      if Handsontable.Dom.hasClass(e.target, 'columnSorting')
        col = getColumn(e.target)
        plugin.sortByColumn.call(instance, col)


  enableObserveChangesPlugin = () ->
    instance = this
    updater = () ->
      instance.updateSettings({observeChanges: true})

    instance._registerTimeout(setTimeout(updater, 0))


  ## Math.sign isn't implemented in Safari :-(
  compareIdentifiers = (a, b) ->
    return 1 if !a
    return -1 if !b
    difference = a - b
    if difference == 0
      0
    else if difference > 0
      1
    else
      -1

  compareStrings = (a, b, sortOrder) ->
    return 0 if a == b
    return 1 if !a
    return -1 if !b

    a = a.toLowerCase()
    b = b.toLowerCase()

    value = naturalSort a, b
    value = -value if sortOrder == false
    value


  compareNumbers = (a, b, sortOrder) ->
    return 0 if Math.abs(a - b) < Number.EPSILON
    return 1 if a > b
    return -1


  compareDates = (aDate, bDate, sortOrder) ->

    return 0 if aDate == bDate
    return 1 if aDate == null or aDate == ""
    return -1 if bDate == null or bDate == ""

    aDate = new Date(aDate).valueOf()
    bDate = new Date(bDate).valueOf()

    if aDate < bDate
      if sortOrder then -1 else 1
    else if aDate > bDate
      if sortOrder then 1 else -1
    else
      0


  defaultSort = (sortOrder)  ->
    return (a, b) ->
      console.log "Sorting", a, b
      return -1 if a[2] == 0
      return 1 if b[2] == 0
      return 0 if a[2] == 0 and b[2] == 0
      value = compareStrings a[1], b[1], sortOrder
      value = compareIdentifiers a[2], b[2] if value == 0
      value


  dateSort = (sortOrder) ->
    return (a, b) ->
      return -1 if a == 0
      return 1 if b == 0
      return 0 if a == 0 and b == 0
      value = compareDates a[1], b[1], sortOrder
      value = compareIdentifiers a[2], b[2] if value == 0
      value


  numberSort = (sortOrder) ->
    return (a, b) ->
      return -1 if a == 0
      return 1 if b == 0
      return 0 if a == 0 and b == 0
      value = compareNumbers a[1], b[1], sortOrder
      value = compareIdentifiers a[2], b[2] if value == 0
      value


  @sort = ()  ->
    instance = this

    ## Undefined should be a natural, i.e., an identifier based order,
    ## which reflects when objects were added.

    @sortIndex.splice(0, @sortIndex.length)

    colOffset = @colOffset()

    console.log '@sort', @sortColumn, @sortIndex, colOffset

    columnData = @getDataAtCol(@sortColumn + colOffset)
    ilen = @countRows() - instance.getSettings()['minSpareRows']
    for i in [0..(if ilen == 0 then 0 else ilen - 1)]
      @sortIndex.push([i, columnData[i], instance.getSourceDataAtRow(i)?.id])

    colMeta = instance.getCellMeta(0, instance.sortColumn)
    sortFunction = defaultSort
    switch colMeta.type
      when 'date' then sortFunction = dateSort
      when 'number' then sortFunction = numberSort

    @sortIndex.sort(sortFunction(instance.sortOrder))

    ##Append spareRows
    for i in [@sortIndex.length..instance.countRows() - 1]
      @sortIndex.push([i, columnData[i], instance.getSourceDataAtRow(i)?.id])


  @translateRow = (row) ->
    instance = this


    if instance.trackerSortingEnabled && instance.sortIndex && instance.sortIndex.length && instance.sortIndex[row]
      return instance.sortIndex[row][0]

    return row


  @untranslateRow = (row) ->
    instance = this
    if instance.trackerSortingEnabled && instance.sortIndex && instance.sortIndex.length
      for element, i in instance.sortIndex
        return i if element[0] == row


  @getColHeader = (col, TH) ->
    instance = @
    if col >= 0
      Handsontable.Dom.addClass(TH.querySelector('.colHeader'), 'columnSorting')
      if instance.sortColumn != undefined && col == instance.sortColumn
        Handsontable.Dom.addClass(TH, 'column-sorted')
        arrow = document.createElement('div')
        Handsontable.Dom.addClass(arrow, 'table-arrow')
        if instance.sortOrder == false
          Handsontable.Dom.addClass(arrow, 'down')

        firstChild = TH.firstChild
        firstChild.appendChild(arrow)

  isSorted = (instance) ->
    typeof instance.sortColumn != 'undefined'


  @afterCreateRow = (index, amount) ->
    instance = this

    return if !isSorted(instance)

    for row in instance.sortIndex
      if row[0] >= index
        row[0] += amount

    for i in [0..amount - 1]
      instance.sortIndex.splice(index+i, 0, [index+i, instance.getData()[index+i][instance.sortColumn + instance.colOffset()]])

    saveSortingState.call(instance);


  @afterRemoveRow =(index, amount) ->
    instance = this

    return if !isSorted(instance)

    physicalRemovedIndex = plugin.translateRow.call(instance, index)

    instance.sortIndex.splice(index, amount)

    for row in instance.sortIndex
      if row[0] > physicalRemovedIndex
        row[0] -= amount

    saveSortingState.call(instance)


  @afterChangeSort = (changes)  ->
    instance = this
    sortColumnChanged = false
    selection = {}
    return if !changes

    for change in changes
      if change[1] == instance.sortColumn
        sortColumnChanged = true
        selection.row = plugin.translateRow.call(instance, changes[i][0])
        selection.col = changes[i][1]
        break

    if sortColumnChanged
      sorter = () ->
        plugin.sort.call(instance)
        instance.render()
        instance.selectCell(plugin.untranslateRow.call(instance, selection.row), selection.col)

      instance._registerTimeout(setTimeout(sorter, 0))

  @


thtSortColumn = new TrackerHandsontableColumnSorting()

Handsontable.hooks.add 'afterInit', ()  -> thtSortColumn.init.call(this, 'afterInit')
Handsontable.hooks.add 'afterUpdateSettings', () -> thtSortColumn.init.call(this, 'afterUpdateSettings')
Handsontable.hooks.add 'modifyRow', thtSortColumn.translateRow
Handsontable.hooks.add 'afterGetColHeader', thtSortColumn.getColHeader
