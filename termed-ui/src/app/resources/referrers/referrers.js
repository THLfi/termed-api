(function(angular) {
  'use strict';

  angular.module('termed.resources.referrers', ['pascalprecht.translate', 'termed.rest'])

  .directive('thlResourceReferrers', function($translate, ReferenceAttributeList) {
    return {
      restrict: 'E',
      scope: {
        resource: '='
      },
      templateUrl: 'app/resources/referrers/referrers.html',
      controller: function($scope) {
        $scope.lang = $translate.use();

        $scope.resource.$promise.then(function(resource) {
          $scope.refAttrs = ReferenceAttributeList.query({
            schemeId: resource.scheme.id,
            classId: resource.type.id
          });
        });

        $scope.isEmpty = function (obj) {
          for (var i in obj) {
            if (obj.hasOwnProperty(i)) {
              return false;
            }
          }
          return true;
        };
      }
    };
  });

})(window.angular);
