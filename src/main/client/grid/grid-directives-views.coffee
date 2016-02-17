angular
  .module 'tracker.grid'

  ## Here we get some more generic tracker view directives, that allow us to
  ## render something other than a grid if we need to.

  .directive 'trackerView', Array '$compile', ($compile) ->
    result =
      restrict: "A"
      replace: true
      scope:
        study: '='
        view: '='
        attributes: '='
        permissions: '='
      link: (scope, element, attrs) ->

        scope.$watch 'view', (view, old) ->
          if view != old
            if view.body?
              element.html('<div tracker-page-view study="study" view="view" attributes="attributes" permissions="permissions"></div>')
            else
              element.html('<div tracker-table-view study="study" view="view" attributes="attributes" permissions="permissions"></div>')
            $compile(element.contents())(scope)


  .directive 'trackerPageView', Array '$compile', ($compile) ->
    result =
      restrict: "A"
      replace: true
      scope:
        study: '='
        view: '='
        attributes: '='
        permissions: '='
      controller: 'PageViewController'
      template:
        '<div>' +
        '  <uib-alert ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)">{{alert.message}}: <b><a href="#" ng-click="closeAlert($index); reset();">Clear and reset form</a></b></uib-alert>' +
        '  <div class="page-view-body"></div>' +
        '</div>'
      link: (scope, element, attrs) ->
        scope.$watch 'view', (view, old) ->
          if view?.body
            page = $(element).find(".page-view-body")
            page.html(view.body)
            $compile(page.contents())(scope)

  .directive 'trackerTableView', () ->
    result =
      restrict: "A"
      replace: true
      controller: 'ConnectedUserController'
      scope:
        study: '='
        view: '='
        attributes: '='
        permissions: '='
      template:
        '<div>' +
        '  <div search-control></div>' +
        '  <h3>' +
        '    <div ng-show="permissions.write" style="display:inline-block;">' +
        '      <toggle-switch ng-model="editingStatus" class="switch-danger switch-small" knob-label="Editing"><toggle-switch>' +
        '    </div>' +
        '    <small ng-show="currentUsers.slice(1).length">' +
        '      Connected:' +
        '      <span ng-repeat="user in currentUsers.slice(1)" class="badge" ng-class="&apos;editorUser-&apos; + $index">{{user}}</span></small>' +
        '  </h3>' +
        '  <div tracker-error error-message="errorMessage"></div>' +
        '  <div class="tracker-table-container">' +
        '    <div tracker-table study="study" view="view" attributes="attributes" editing-status="editingStatus" permissions="permissions"></div>' +
        '  </div>' +
        '  <div ng-controller="GridActionController">' +
        '    <button ng-show="permissions.download" class="btn btn-default" ng-click="export()">Export Spreadsheet</button>' +
        '  </div>' +
        '</div>'
