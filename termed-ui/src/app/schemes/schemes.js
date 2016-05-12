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
      query: query ? query.split(' ').map(function(q) { return q + "*"; }).join(' ') : query,
      max: $scope.max
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
        prefLabel: [
          {
            lang: "fi",
            value: "Uusi aineisto"
          }
        ]
      }
    }, function(scheme) {
      $location.path('/schemes/' + scheme.id + '/edit');
    });
  };

  $scope.searchResources(($location.search()).q || "");

})

.controller('SchemeEditCtrl', function($scope, $routeParams, $location, $translate, Scheme, PropertyList) {

  $scope.lang = $translate.use();

  $scope.scheme = Scheme.get({
    schemeId: $routeParams.schemeId
  });

  $scope.properties = PropertyList.query();

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

  $scope.newClass = function() {
    if (!$scope.scheme.classes) {
      $scope.scheme.classes = [];
    }
    $scope.scheme.classes.unshift({
      id: "NewClass",
      properties: {
        prefLabel: [
          {
            lang: "fi",
            value: "Uusi luokka"
          }
        ]
      },
      textAttributes: [
        {
          id: "prefLabel",
          regex: "(?s)^.*$",
          properties: {
            prefLabel: [
              {
                lang: "fi",
                value: "Nimike"
              }
            ]
          }
        }
      ]
    });
  };

  $scope.selectClass = function(cls) {
    $scope.cls = cls;
  };

  $scope.removeClass = function(cls) {
    var i = $scope.scheme.classes.indexOf(cls);
    $scope.scheme.classes.splice(i, 1);
  };

  $scope.newTextAttribute = function(cls) {
    if (!cls.textAttributes) {
      cls.textAttributes = [];
    }
    cls.textAttributes.push({
      id: "newTextAttribute",
      regex: "(?s)^.*$",
      properties: {
        prefLabel: [
          {
            lang: "fi",
            value: "Uusi tekstiattribuutti"
          }
        ]
      }
    });
  };

  $scope.selectTextAttribute = function(textAttribute) {
    $scope.textAttribute = textAttribute;
  };

  $scope.removeTextAttribute = function(cls, textAttribute) {
    var i = cls.textAttributes.indexOf(textAttribute);
    cls.textAttributes.splice(i, 1);
  };

  $scope.newReferenceAttribute = function(cls) {
    if (!cls.referenceAttributes) {
      cls.referenceAttributes = [];
    }
    cls.referenceAttributes.push({
      id: "newReferenceAttribute",
      properties: {
        prefLabel: [
          {
            lang: "fi",
            value: "Uusi viiteattribuutit"
          }
        ]
      }
    });
  };

  $scope.selectReferenceAttribute = function(referenceAttribute) {
    $scope.referenceAttribute = referenceAttribute;
  };

  $scope.removeReferenceAttribute = function(cls, referenceAttribute) {
    var i = cls.referenceAttributes.indexOf(referenceAttribute);
    cls.referenceAttributes.splice(i, 1);
  };

});

})(window.angular);
