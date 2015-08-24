angular
  .module 'tracker.admin'

  .controller 'AdminAuditController', Array '$scope', '$http', '$stateParams', ($scope, $http, $stateParams) ->

    $scope.study = {name: $stateParams.studyName}
    $scope.totalItems = 1
    $scope.currentPage = 1
    $scope.page = []
    $scope.pagination = {
      page: 1
      pageSize: 10
    }

    handlePaginationChange = (pagination) ->
      $http
        .get "/api/studies/#{encodeURIComponent($stateParams.studyName)}/audit", {params: pagination}
        .success (result) ->
          $scope.page = result.audit
          $scope.totalItems = result.counts.total
        .error (error) ->
          console.log "Error", error

    handlePaginationChange $scope.pagination

    $scope.$watch 'currentPage', (newValue) ->
      if newValue?
        handlePaginationChange {page: newValue}
