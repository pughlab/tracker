angular
  .module 'tracker.controls'


  .directive 'searchControl', () ->
    result =
      restrict: "A"
      replace: false
      controller: Array '$scope', ($scope) ->

        previousSearch = undefined

        $scope.search = (q) ->
          $scope.$broadcast 'table:search', q

        $scope.searchNavigation = (direction) ->
          $scope.$broadcast 'table:search-navigation', direction

      template: '<div>' +
                '  <form class="form-inline pull-right" role="form" ng-submit="search(filterText)">' +
                '    <div class="form-group form-group-sm">' +
                '      <label for="search">Search: </label>' +
                '      <input id="search" type="text" class="form-control input-sm" ng-model="filterText" search-button placeholder="Search text">' +
                '    </div>' +
                '    <div class="btn-group tracker-search-navigation" role="group" aria-label="search navigation">' +
                '    <button type="button" class="btn btn-default btn-sm tracker-search-previous" aria-label="Previous match">' +
                '      <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>' +
                '    </button>' +
                '    <button type="button" class="btn btn-default btn-sm tracker-search-next" aria-label="Next match">' +
                '      <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>' +
                '    </button>' +
                '    </div>' +
                '  </form>' +
                '</div>'
      link: (scope, iElement, iAttrs) ->

        jQuery(iElement).find(".tracker-search-previous").on 'click', (e) ->
          scope.searchNavigation "previous"

        jQuery(iElement).find(".tracker-search-next").on 'click', (e) ->
          scope.searchNavigation "next"



  .directive 'searchButton', () ->

    result =
      restrict: "A"
      replace: false
      require: 'ngModel'
      scope:
        ngModel: '='
      link: (scope, iElement, iAttrs) ->

        button = angular.element("<a role='button' class='clear-button' aria-label='Clear'><span class='glyphicon glyphicon-remove-circle'></span></a>")
        iElement.after button

        button.on 'click', (e) ->
          scope.$apply () ->
            scope.ngModel = ""
          iElement.trigger 'submit'


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
              if typeof value == 'undefined' and value != old
                selectize.clearOptions()
                selectize.setValue(value)
                initialized = true
              else if value && Array.isArray(value)
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
