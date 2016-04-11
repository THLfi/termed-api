(function (angular) { 'use strict';

angular.module('termed.directives', [])

.directive('tooltip', function($rootScope) {
  return {
    restrict: 'A',
    link: function(scope, elem, attrs) {

      $rootScope.$on('$translateChangeEnd', function() {
        elem.tooltip('destroy');
        elem.tooltip({
          title: attrs.title
        });
      });

      elem.hover(function() {
        elem.tooltip('show');
      }, function() {
        elem.tooltip('hide');
      });
    }
  };
});

})(window.angular);
