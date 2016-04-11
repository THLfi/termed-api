(function(angular) {
  'use strict';

  angular.module('termed.resources.properties', ['pascalprecht.translate', 'termed.rest'])

  .directive('thlResourceProperties', function($translate, TextAttributeList) {
    return {
      restrict: 'E',
      scope: {
        resource: '='
      },
      templateUrl: 'app/resources/properties/properties.html',
      controller: function($scope) {
        $scope.lang = $translate.use();

        $scope.resource.$promise.then(function(resource) {
          $scope.textAttrs = TextAttributeList.query({
            schemeId: resource.scheme.id,
            classId: resource.type.id
          });
        });
      }
    };
  })

  .directive('thlResourcePropertiesEdit', function($translate, TextAttributeList) {
    return {
      restrict: 'E',
      scope: {
        resource: '='
      },
      templateUrl: 'app/resources/properties/properties-edit.html',
      controller: function($scope) {
        $scope.lang = $translate.use();

        $scope.resource.$promise.then(function(resource) {
          TextAttributeList.query({
            schemeId: resource.scheme.id,
            classId: resource.type.id
          }, function(textAttrs) {
            $scope.textAttrs = textAttrs;

            // start watching properties for changes and update form accordingly
            $scope.$watch('resource.properties', function() {
              ensureProperties();
            }, true);
          });
        });

        function ensureProperties() {
          $scope.textAttrs.forEach(function(textAttr) {
            ensureProperty($scope.resource.properties, textAttr.id);
          });
        }

        function ensureProperty(resourceProperties, propertyId) {
          if (!resourceProperties[propertyId]) {
            resourceProperties[propertyId] = [];
          }

          var values = resourceProperties[propertyId];

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
        }
      }
    };
  });

})(window.angular);
