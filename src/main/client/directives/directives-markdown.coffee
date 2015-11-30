angular
  .module 'tracker.directives'

  .directive 'markdown', Array '$sanitize', ($sanitize) ->
    result =
      restrict: "A"
      replace: false
      scope:
        markdown: '='
      link: (scope, iElement, iAttrs) ->

        scope.$watch 'markdown', (newValue, oldValue) ->
          if newValue?
            converter = new showdown.Converter()
            html = converter.makeHtml(newValue)
            html = $sanitize(html)
            iElement.html(html)
