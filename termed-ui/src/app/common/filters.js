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
  return function(propertyValues, defaultValue) {
    if (!propertyValues || propertyValues.length === 0) {
      return defaultValue || '-';
    }

    var lang = $translate.use();

    for (var i=0;i<propertyValues.length;i++) {
      if (propertyValues[i].lang == lang && propertyValues[i].value.length > 0) {
        return propertyValues[i].value;
      }
    }

    if (propertyValues[0].value.length > 0) {
      var langInfo = propertyValues[0].lang ? " (" + propertyValues[0].lang + ")" : "";
      return propertyValues[0].value + langInfo;
    }

    return defaultValue || '-';
  };
});

})(window.angular);
