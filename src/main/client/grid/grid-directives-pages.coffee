angular
  .module 'tracker.grid'

  .directive 'trackerDateInput', () ->
    result =
      restrict: "A"
      require: '?ngModel'
      link: (scope, element, attrs, ngModel) ->
        console.log "Called trackerDateInput", ngModel, scope.study?.options?.dateFormat
        return if ! ngModel

        dateFormat = scope.study?.options?.dateFormat || "YYYY-MM-DD"

        formatter = (v) ->
          moment(v).format(dateFormat)

        parser = (v) ->
          value = moment(v, dateFormat)
          if value.isValid()
            value.format("YYYY-MM-DD")
          else
            undefined

        ngModel.$formatters.push(formatter)
        ngModel.$parsers.push(parser)
