angular
  .module 'tracker.admin'

  .directive 'selectizePermissions', Array '$timeout', ($timeout) ->
    result =
      restrict: "A"
      replace: false
      scope:
        model: '='
        ngDisabled: '='
      link: (scope, iElement, iAttrs) ->

        selectize = undefined
        initialized = false

        permissionLabel = (permission) ->
          "#{permission.resource_type}/#{permission.resource}: #{permission.permission}"

        select = jQuery(iElement).selectize
          maxItems: null
          valueField: 'text'
          labelField: 'text'
          searchField: 'text'
          persist: false
          create: (input, callback) ->
            input = input.trim()
            match = /^(\w+)\/(\w+):\s*(\w+)$/.exec(input)
            if match
              callback {
                text: match[0]
                permission: {
                  resource_type: match[1]
                  resource: match[2]
                  permission: match[3]
                }
              }
            else
              callback()

        selectize = select[0].selectize

        if scope.ngDisabled
          selectize.disable()

        selectize.on 'change', (v) ->
          v = [] if v == "" or v == null
          if initialized
            scope.$evalAsync () ->
              value = (selectize.options[e].permission for e in v)
              scope.model = value

        ## Tags are a collection
        scope.$watchCollection 'model', (value, old) ->
          value ?= []
          initialized = false
          $timeout () ->
            selectize.clearOptions()
            selectize.addOption({permission: permission, text: permissionLabel(permission)}) for permission in value
            selectize.setValue(permissionLabel(permission) for permission in value)
            initialized = true

        scope.$watch 'ngDisabled', (disabled, old) ->
          if disabled != old && selectize
            $timeout () ->
              if disabled
                selectize.disable()
              else
                selectize.enable()
