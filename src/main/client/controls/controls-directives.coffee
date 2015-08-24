angular
  .module 'tracker.controls'

  .directive 'selectizeTags', Array '$timeout', ($timeout) ->
    result =
      restrict: "A"
      replace: false
      scope:
        options: '='
        model: '='
        ngDisabled: '='
      link: (scope, iElement, iAttrs) ->

        selectize = undefined

        scope.$watch 'options', (options) ->
          select = jQuery(iElement).selectize
            maxItems: null
            valueField: 'text'
            labelField: 'text'
            searchField: 'text'
            options: options
            create: true
            createOnBlur: true
            persist: false

          selectize = select[0].selectize

          if scope.ngDisabled
            selectize.disable()

          selectize.on 'change', (v) ->
            v = undefined if v == "" or v == null
            scope.$evalAsync () ->
              scope.model = v

        ## Tags are a collection
        scope.$watchCollection 'model', (value, old) ->
          if selectize
            $timeout () ->
              if value && Array.isArray(value)
                selectize.clearOptions()
                for tag in value
                  selectize.addOption({text: tag})
                selectize.setValue(value)
                initialized = true

        scope.$watch 'ngDisabled', (disabled, old) ->
          if disabled != old && selectize
            $timeout () ->
              if disabled
                selectize.disable()
              else
                selectize.enable()
