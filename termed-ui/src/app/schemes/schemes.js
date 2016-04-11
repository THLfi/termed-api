(function (angular) { 'use strict';

angular.module('termed.schemes', ['ngRoute', 'termed.rest', 'termed.schemes.properties'])

.config(function($routeProvider) {
  $routeProvider

  .when('/schemes/', {
    templateUrl: 'app/schemes/scheme-list.html',
    controller: 'SchemeListCtrl',
    reloadOnSearch: false
  })

  .when('/schemes/:schemeId/edit', {
    templateUrl: 'app/schemes/scheme-edit.html',
    controller: 'SchemeEditCtrl'
  });
})

.controller('SchemeListCtrl', function($scope, $location, $translate, SchemeList, ResourceList) {

  $scope.lang = $translate.use();

  $scope.query = ($location.search()).q || "";
  $scope.max = 50;

  $scope.loadMoreResults = function() {
    $scope.max += 50;
    $scope.searchResources(($location.search()).q || "");
  };

  $scope.searchResources = function(query) {
    ResourceList.query({
      query: query,
      max: $scope.max,
      orderBy: ['prefLabel.' + $scope.lang + '.sortable']
    }, function(resources) {
      $scope.resources = resources;
      $location.search({
        q: $scope.query
      }).replace();
    });
  };

  $scope.schemes = SchemeList.query({
    orderBy: 'prefLabel.fi'
  });

  $scope.newScheme = function() {
    SchemeList.save({
      properties: {
        prefLabel: {
          fi: ['Uusi aineisto']
        }
      }
    }, function(scheme) {
      $location.path('/schemes/' + scheme.id + '/edit');
    });
  };

  $scope.searchResources(($location.search()).q || "");

})

.controller('SchemeEditCtrl', function($scope, $routeParams, $location, $translate, Scheme) {

  $scope.lang = $translate.use();

  $scope.scheme = Scheme.get({
    schemeId: $routeParams.schemeId
  });

  $scope.save = function() {
    $scope.scheme.$save(function() {
      $location.path('/schemes/' + $routeParams.schemeId + '/resources');
    }, function(error) {
      $scope.error = error;
    });
  };

  $scope.remove = function() {
    $scope.scheme.$delete({
      schemeId: $routeParams.schemeId
    }, function() {
      $location.path('/schemes');
    }, function(error) {
      $scope.error = error;
    });
  };

});

})(window.angular);
