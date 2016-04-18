(function (angular) { 'use strict';

angular.module('termed.schemes.properties', ['pascalprecht.translate', 'termed.rest'])

.directive('thlSchemePropertiesEdit', function($translate, PropertyList) {
  return {
    restrict: 'E',
    scope: {
      propertyMap: '='
    },
    templateUrl: 'app/schemes/properties/properties-edit.html',
    controller: function($scope) {

      $scope.properties = PropertyList.query();

      $scope.ensureProperty = function(properties, propertyId) {
        if (!properties[propertyId]) {
          properties[propertyId] = [];
        }

        var values = properties[propertyId];

        // remove all empty values (not including the last one)
        for (var i = 0; i < values.length - 1; i++) {
          if (values[i].lang === "" && values[i].value === "") {
            values.splice(i, 1);
            i--;
          }
        }

        // ensure that last value is empty
        if (values.length === 0 || values[values.length - 1].value !== "") {
          values.push({ lang:'', value:'' });
        }

        return true;
      };

    }
  };
});

})(window.angular);
