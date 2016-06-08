(function (angular) { 'use strict';

angular.module('termed.resources', ['ngRoute', 'termed.rest', 'termed.resources.references', 'termed.resources.referrers', 'termed.resources.properties'])

.config(function($routeProvider) {
  $routeProvider

  .when('/schemes/:schemeId/resources', {
    templateUrl: 'app/resources/resource-list.html',
    controller: 'ResourceListCtrl',
    reloadOnSearch: false
  })

  .when('/schemes/:schemeId/classes/:typeId/resources/:id', {
    templateUrl: 'app/resources/resource.html',
    controller: 'ResourceCtrl'
  })

  .when('/schemes/:schemeId/classes/:typeId/resources/:id/edit', {
    templateUrl: 'app/resources/resource-edit.html',
    controller: 'ResourceEditCtrl'
  });
})

.controller('ResourceListCtrl', function($scope, $route, $location, $routeParams, $translate, Scheme, SchemeResourceList, ResourceList, Resource) {

  $scope.lang = $translate.use();

  $scope.query = ($location.search()).q || "";
  $scope.max = 50;

  $scope.scheme = Scheme.get({
    schemeId: $routeParams.schemeId
  });

  $scope.loadMoreResults = function() {
    $scope.max += 50;
    $scope.searchResources(($location.search()).q || "");
  };

  $scope.searchResources = function(query) {
    $scope.query = query;
    SchemeResourceList.query({
      schemeId: $routeParams.schemeId,
      query: query ? query.split(' ').map(function(q) { return q + "*"; }).join(' ') : query,
      max: $scope.max,
      orderBy: query ? [''] : ['code', 'prefLabel.' + $scope.lang + '.sortable']
    }, function(resources) {
      $scope.resources = resources;
      $location.search({
        q: query
      }).replace();
    });
  };

  $scope.newResource = function(type) {
    ResourceList.save({
      scheme: $scope.scheme,
      type: type
    }, function(resource) {
      $location.path('/schemes/' + resource.scheme.id + '/classes/' + resource.type.id + '/resources/' + resource.id + '/edit');
    }, function(error) {
      $scope.error = error;
    });
  };

  $scope.searchResources(($location.search()).q || "");

})

.controller('ResourceCtrl', function($scope, $routeParams, $location, $translate, Resource, ResourcePaths, ResourceList, Class) {

  $scope.lang = $translate.use();

  $scope.resource = Resource.get({
    schemeId: $routeParams.schemeId,
    typeId: $routeParams.typeId,
    id: $routeParams.id
  });

  $scope.type = Class.get({
    schemeId: $routeParams.schemeId,
    classId: $routeParams.typeId
  });
})

.controller('ResourceEditCtrl', function($scope, $routeParams, $location, $translate, Resource) {

  $scope.lang = $translate.use();

  $scope.resource = Resource.get({
    schemeId: $routeParams.schemeId,
    typeId: $routeParams.typeId,
    id: $routeParams.id
  });

  $scope.save = function() {
    $scope.resource.$update({
      schemeId: $routeParams.schemeId,
      typeId: $routeParams.typeId,
      id: $routeParams.id
    }, function(resource) {
      $location.path('/schemes/' + resource.scheme.id + '/classes/' + resource.type.id + '/resources/' + resource.id);
    }, function(error) {
      $scope.error = error;
    });
  };

  $scope.remove = function() {
    $scope.resource.$delete({
      schemeId: $routeParams.schemeId,
      typeId: $routeParams.typeId,
      id: $routeParams.id
    },function() {
      $location.path('/schemes/' + $routeParams.schemeId + '/resources');
    }, function(error) {
      $scope.error = error;
    });
  };

})

.directive('thlResourceTree', function($rootScope, $location, $q, $translate) {
  return {
    scope: {
      resource: '=',
      type: '='
    },
    link: function(scope, elem, attrs) {

      function propVal(props, propertyId, defaultValue) {
        if (props[propertyId] && props[propertyId].length > 0) {
          return props[propertyId][0].value;
        }
        return defaultValue;
      }

      var lang = $translate.use();

      $rootScope.$on('$translateChangeEnd', function() {
        lang = $translate.use();
        elem.jstree("refresh");
      });

      $q.all([scope.resource.$promise, scope.type.$promise]).then(function() {

        var treeAttributeId = propVal(scope.type.properties, "configTreeAttributeId", "broader");
        var treeInverted = propVal(scope.type.properties, "configTreeInverted", "true");
        var treeSort = propVal(scope.type.properties, "configTreeSort", "true");

        elem.jstree({
          core: {
            themes: {
              variant: "small"
            },
            data: {
              url: function(node) {
                var resourceSchemeId;
                var resourceTypeId;
                var resourceId;

                if (node.id === '#') {
                  resourceSchemeId = scope.resource.scheme.id;
                  resourceTypeId = scope.resource.type.id;
                  resourceId = scope.resource.id;
                } else {
                  resourceSchemeId = node.li_attr.resourceSchemeId;
                  resourceTypeId = node.li_attr.resourceTypeId;
                  resourceId = node.li_attr.resourceId;
                }

                return 'api/schemes/' + resourceSchemeId +
                       '/classes/' + resourceTypeId +
                       '/resources/' + resourceId +
                       '/trees' +
                       '?attributeId=' + treeAttributeId +
                       '&context=true' +
                       '&jstree=true' +
                       '&referrers=' + treeInverted +
                       '&lang=' + lang;
              },
              data: function(node) {
                return node;
              }
            }
          },
          plugins: [treeSort == "true" ? "sort" : ""]
        });
      });

      elem.on('activate_node.jstree', function(e, data) {
        scope.$apply(function() {
          $location.path(data.node.a_attr.href);
        });
      });
    }
  };
});

})(window.angular);
