(function (angular) { 'use strict';

angular.module('termed.filters', ['pascalprecht.translate'])

.filter('capitalize', function() {
  return function(input) {
    return input.charAt(0).toUpperCase() + input.slice(1).toLowerCase();
  };
})

.filter('limit', function() {
  return function(input, max) {
    return input.length > max ? input.substring(0, max) + "..." : input;
  };
})

.filter('localizeValue', function($translate) {
  return function(propertyValues) {
    if (!propertyValues || propertyValues.length === 0) {
      return "-";
    }

    var lang = $translate.use();

    for (var i=0;i<propertyValues.length;i++) {
      if (propertyValues[i].lang == lang) {
        return propertyValues[i].value;
      }
    }

    return propertyValues[0].value;
  };
});

})(window.angular);
