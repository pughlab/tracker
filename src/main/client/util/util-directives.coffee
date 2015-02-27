angular
  .module 'tracker.util'

  .directive 'trackerError', () ->
    result =
      restrict: "A"
      replace: true
      scope:
        errorMessage: '='
      template: '<div ng-show="errorMessage" class="alert alert-danger" role="alert">' +
                '  <button type="button" class="close" ng-click="errorMessage = undefined"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>' +
                '  <strong>Error!</strong> <span class="tracker-error-message">{{errorMessage}}</span>' +
                '</div>'
