angular
  .module 'tracker.grid'

  .directive 'trackerDateInput', () ->
    result =
      restrict: "A"
      require: '?ngModel'
      link: (scope, element, attrs, ngModel) ->
        return if ! ngModel

        dateFormat = scope.study?.options?.dateFormat || "YYYY-MM-DD"

        formatter = (v) ->
          if v?
            moment(v).format(dateFormat)
          else
            ""

        parser = (v) ->
          return undefined if !v? or v == ""
          value = moment(v, dateFormat)
          if value.isValid()
            value.format("YYYY-MM-DD")
          else
            undefined

        ngModel.$formatters.push(formatter)
        ngModel.$parsers.push(parser)
