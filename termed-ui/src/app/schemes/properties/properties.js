(function (angular) { 'use strict';

angular.module('termed.schemes.properties', ['pascalprecht.translate', 'termed.rest'])

.directive('thlSchemePropertiesEdit', function($translate, PropertyList) {
  return {
    restrict: 'E',
    scope: {
      schemeProperties: '='
    },
    templateUrl: 'app/schemes/properties/properties-edit.html',
    controller: function($scope) {
      $scope.languages = ['fi', 'sv', 'en'];
      $scope.lang = $translate.use();

      $scope.properties = PropertyList.query(function() {
        ensureProperties();
      });

      $scope.$watch('schemeProperties', function() {
        ensureProperties();
      }, true);

      function ensureProperties() {
        $scope.properties.forEach(function(prop) {
          $scope.languages.forEach(function(lang) {
            ensureProperty($scope.schemeProperties, prop.id, lang);
          });
        });
      }

      function ensureProperty(schemeProperties, propertyId, lang) {
        // not yet loaded
        if (!schemeProperties) {
          return;
        }

        if (!schemeProperties[propertyId]) {
          schemeProperties[propertyId] = {};
        }
        if (!schemeProperties[propertyId][lang]) {
          schemeProperties[propertyId][lang] = [""];
        }

        var values = schemeProperties[propertyId][lang];

        // remove all empty values (not including the last one)
        for (var i = 0; i < values.length - 1; i++) {
          if (values[i] === "") {
            values.splice(i, 1);
            i--;
          }
        }

        // ensure that last value is empty
        if (values.length === 0 || values[values.length - 1] !== "") {
          values.push("");
        }
      }
    }
  };
});

})(window.angular);
