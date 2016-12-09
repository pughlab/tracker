angular
  .module 'tracker.grid'

  ## This module defines a set of components that we'll be able to use to manage
  ## cell-level editing in DataTables. We'd like to use the editor really, but it's
  ## commercial. We'll need to attach some data to the table, so we can manage the
  ## basic interface sensibly. We don't need a huge set of editing controls.

  .factory 'clearSelection', () ->
    return () ->
      if document.selection && document.selection.empty
        document.selection.empty()
      else if window.getSelection
        sel = window.getSelection()
        sel.removeAllRanges()


  .factory 'tableEditor', Array '$log', ($log) ->

    ## Mix in a kind of EventEmitter, which makes it a bit easier for the various components
    ## of the different editing types to talk to one another.

    class AbstractCellEditor

      @editor: undefined
      @cellElement: undefined
      @cell: undefined

      constructor: (@editor, @cellElement, @cell) ->

      open: () ->
        currentText = jQuery(@cellElement).text()
        jQuery(@cellElement).data('editorOriginalText', currentText)

      close: () ->
        newText = jQuery(@cellElement).data('editorOriginalText')
        jQuery(@cellElement).empty().text(newText)


    class InlineTextCellEditor extends AbstractCellEditor

      handleMove: (e, offsets, wrap) ->
        e.preventDefault()
        e.stopPropagation()
        @editor.emit 'moveCell', e, offsets, wrap

      handleKeydown: (e) =>
        switch e.keyCode
          when 9
            if e.shiftKey
              @handleMove e, [-1, 0], true
            else
              @handleMove e, [1, 0], true
          when 40 then @handleMove e, [0, 1], false
          when 38 then @handleMove e, [0, -1], false

      open: () ->
        super()
        currentText = jQuery(@cellElement).text()
        jQuery(@cellElement).empty()
        editorElement = jQuery("<input type='text'></input>")
        jQuery(@cellElement).append(editorElement)
        editorElement.val(currentText)
        editorElement.focus()

        editorElement.on 'keydown', (e) =>
          @handleKeydown e

      close: () ->
        super()


    class TableEditor

      @events: {}
      @dataTable: undefined
      @cellBounds: undefined
      @editUrl: undefined

      constructor: (options) ->
        @events = {}
        for own k, v of options
          @[k] = v

        $log.debug "Constructor", @dataTable

      emit: (event, args...) ->
        return false unless @events[event]
        listener args... for listener in @events[event]
        return true

      addListener: (event, listener) ->
        @emit 'newListener', event, listener
        (@events[event]?=[]).push listener
        return @

      on: (evt, handler) =>
        @addListener(evt, handler)

      once: (event, listener) ->
        fn = =>
          @removeListener event, fn
          listener arguments...
        @on event, fn
        return @

      removeListener: (event, listener) ->
        return @ unless @events[event]
        @events[event] = (l for l in @events[event] when l isnt listener)
        return @

      removeAllListeners: (event) ->
        delete @events[event]
        return @

    ## Now build the editor, and add in some of the basic behaviours.

    (options) ->

      editor = new TableEditor(options)

      editor.on 'moveCell', (e, args...) ->
        $log.debug 'moveCell', e, args

      ## Various actions are required.
      editor.on 'closeCellEditor', (e, cancel) ->
        previous = editor.dataTable.data('currentEditor')
        previous.close()


      editor.on 'openCellEditor', (e, cell) ->
        $log.debug "openCellEditor", e, cell
        previous = editor.dataTable.data('currentEditor')
        editor.emit 'closeCellEditor' if previous

        cellEditor = e.toElement
        editorElement = new InlineTextCellEditor(editor, cellEditor, cell)
        editorElement.open()

        editor.dataTable.data('currentEditor', editorElement)

      editor
