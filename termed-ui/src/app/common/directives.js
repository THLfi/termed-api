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
})

.directive('thlSortable', function() {
  return {
    scope: {
      'elements': "="
    },
    link: function(scope, elem, attrs) {
      elem.sortable({
        handle: ".handle",
        start: function(event, ui) {
          ui.item.data('start', ui.item.index());
        },
        update: function(event, ui) {
          var start = ui.item.data('start');
          var end = ui.item.index();

          scope.$apply(function() {
            scope.elements.splice(end, 0, scope.elements.splice(start, 1)[0]);
          });
        }
      });
    }
  }
});

})(window.angular);
