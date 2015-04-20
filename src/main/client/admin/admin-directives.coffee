angular
  .module 'tracker.admin'

  .directive 'auditRecordDate', () ->
    result =
      restrict: "A"
      replace: false
      scope:
        auditRecordDate: '='

      link: (scope, iElement, iAttrs) ->

        scope.$watch 'auditRecordDate', (newValue) ->
          dateString = if newValue.event_time
            date = new Date(newValue.event_time)
            date.toString()
          else
            "N/A"
          iElement.empty().append dateString


  .directive 'auditRecord', Array 'renderAuditRecord', (renderAuditRecord) ->
    result =
      restrict: "A"
      replace: false
      scope:
        auditRecord: '='
      link: (scope, iElement, iAttrs) ->

        scope.$watch 'auditRecord', (newValue) ->
          description = renderAuditRecord(newValue)
          iElement.empty().append description


  .directive 'selectize', Array '$timeout', ($timeout) ->
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
            maxItems: 1
            valueField: 'id'
            labelField: 'text'
            searchField: 'text'
            options: options
            create: false

          selectize = select[0].selectize

          if scope.ngDisabled
            selectize.disable()

          selectize.on 'change', (v) ->
            v = undefined if v == ""
            $timeout () ->
              scope.model = v

        scope.$watch 'model', (value, old) ->
          if selectize
            $timeout () ->
              selectize.setValue(value)
          else
            selectedValue = value

        scope.$watch 'ngDisabled', (disabled, old) ->
          if disabled != old && selectize
            $timeout () ->
              if disabled
                selectize.disable()
              else
                selectize.enable()


  .directive 'selectizeList', Array '$timeout', ($timeout) ->
    result = 
      restrict: "A"
      replace: false
      scope:
        model: '='
        ngDisabled: '='

      link: (scope, iElement, iAttrs) ->

        selectize = undefined

        select = jQuery(iElement).selectize
          create: true
          createOnBlur: true
          maxItems: null
          persist: false

        selectize = select[0].selectize

        selectize.on 'change', (v) ->
          v = undefined if v == ""
          scope.$evalAsync () ->
            scope.model = v unless ! v? && scope.model == undefined

        scope.$watch 'ngDisabled', (disabled, old) ->
          if disabled != old && selectize
            $timeout () ->
              if disabled
                selectize.disable()
              else
                selectize.enable()

        scope.$watchCollection 'model', (value, old) ->
          if selectize
            $timeout () ->
              values = (value or [])
              selectize.clearOptions()
              for value in values
                selectize.addOption {value: value, text: value}
              selectize.setValue(values)



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
            valueField: 'id'
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
              selectize.setValue(value)

        scope.$watch 'ngDisabled', (disabled, old) ->
          if disabled != old && selectize
            $timeout () ->
              if disabled
                selectize.disable()
              else
                selectize.enable()


  .directive 'selectizeUsers', Array '$timeout', '$http', ($timeout, $http) ->
    result =
      restrict: "A"
      replace: false
      scope:
        model: '='
        ngDisabled: '='
      link: (scope, iElement, iAttrs) ->

        selectize = undefined
        initialized = false

        select = jQuery(iElement).selectize
          maxItems: null
          valueField: 'username'
          labelField: 'username'
          searchField: 'username'
          persist: false
          create: false
          load: (query, callback) ->
            username = query.trim()
            $http
              .get "/api/authentication/user/#{encodeURIComponent(username)}/exists"
              .success (response) ->
                callback [response]
              .error (response) ->
                callback []

        selectize = select[0].selectize

        if scope.ngDisabled
          selectize.disable()

        selectize.on 'change', (v) ->
          v = [] if v == "" or v == null
          if initialized
            scope.$evalAsync () ->
              scope.model = (selectize.options[e] for e in v)

        ## Tags are a collection
        scope.$watchCollection 'model', (value, old) ->
          value ?= []
          initialized = false
          $timeout () ->
            selectize.clearOptions()
            selectize.addOption(user) for user in value
            selectize.setValue(user.username for user in value)
            initialized = true

        scope.$watch 'ngDisabled', (disabled, old) ->
          if disabled != old && selectize
            $timeout () ->
              if disabled
                selectize.disable()
              else
                selectize.enable()


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


  .directive 'filterEditor', Array '$timeout', ($timeout) ->
    result =
      restrict: "A"
      replace: true
      scope:
        model: '='
        attributes: '='
      template: '<form class="form-horizontal">' +
                '  <div class="form-group">' +
                '    <label for="filterFieldName" class="col-md-2 control-label">Filter field</label>' +
                '    <div class="col-md-4"><select id="filterFieldName" class="form-control padded-dropdown"></select></div>' +
                '    <label for="filterFieldValue" class="col-md-2 control-label">Filter value</label>' +
                '    <div class="col-md-4"><input id="filterFieldValue" class="form-control" type="text" placeholder="Filter value"></div>' +
                '  </div>' +
                '</form>'

      link: (scope, iElement, iAttrs) ->

        selectize = undefined
        selectedValue = undefined

        handleChange = (v) ->
          if v == ''
            jQuery("#filterFieldValue").prop('disabled', true)
            scope.model = []
          else
            jQuery("#filterFieldValue").prop('disabled', false)
            scope.model = [{attribute: v, value: jQuery("#filterFieldValue").val()}]
          scope.$evalAsync () ->
            scope.$emit 'setModified'

        jQuery("#filterFieldValue").on 'input', (x) ->
          scope.$evalAsync () ->
            scope.model = [{attribute: selectedValue, value: jQuery("#filterFieldValue").val()}]
            scope.$emit 'setModified'

        scope.$watch 'model', (value, old) ->
          if ! selectize

            ## Initialize the widgets
            control = iElement.find("select")
            select = control.selectize
              maxItems: 1
              valueField: 'name'
              labelField: 'label'
              searchField: 'label'
              sortField: 'sId'
              options: []
              create: false

            selectize = select[0].selectize
            selectize.on 'change', handleChange

          if value && value.length > 0
            selectedValue = value[0].attribute
            jQuery("#filterFieldValue").val(value[0].value)
          else
            selectedValue = ''
            jQuery("#filterFieldValue").val('')

        scope.$watchCollection 'attributes', (value) ->
          if Array.isArray value
            selectize.off 'change'
            selectize.clearOptions()
            selectize.addOption {sId: -1, name: '', label: 'None'}
            for option, i in value
              option.sId = i
              selectize.addOption(option)
            $timeout () ->
              selectize.setValue selectedValue, true
              selectize.on 'change', handleChange


