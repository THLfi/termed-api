(function(angular) {
  'use strict';

  angular.module('termed.resources.references', ['pascalprecht.translate', 'termed.rest'])

  .directive('thlResourceReferences', function($translate, ReferenceAttributeList) {
    return {
      restrict: 'E',
      scope: {
        resource: '='
      },
      templateUrl: 'app/resources/references/references.html',
      controller: function($scope) {
        $scope.lang = $translate.use();

        $scope.resource.$promise.then(function(resource) {
          $scope.refAttrs = ReferenceAttributeList.query({
            schemeId: resource.scheme.id,
            classId: resource.type.id
          });
        });
      }
    };
  })

  .directive('thlResourceReferencesEdit', function($translate, ReferenceAttributeList, ResourceList) {
    return {
      restrict: 'E',
      scope: {
        resource: '='
      },
      templateUrl: 'app/resources/references/references-edit.html',
      controller: function($scope) {
        $scope.lang = $translate.use();

        $scope.resource.$promise.then(function(resource) {
          $scope.refAttrs = ReferenceAttributeList.query({
            schemeId: resource.scheme.id,
            classId: resource.type.id
          });
        });

        $scope.newRefAttrValue = function(resourceRefs, refAttr) {
          ResourceList.save({
            scheme: refAttr.range.scheme,
            type: refAttr.range
          }, function(resource) {
            resourceRefs[refAttr.id] = resourceRefs[refAttr.id] || [];
            resourceRefs[refAttr.id].push(resource);
          }, function(error) {
            $scope.error = error;
          });
        };
      }
    };
  })

  .directive('thlSelectResource', function($q, $timeout, $translate, Resource, ResourceList) {
    return {
      scope: {
        'ngModel': "=",
        'refAttr': '='
      },
      link: function(scope, elem, attrs) {

        function getLocalizedPrefLabel(properties) {
          var lang = $translate.use();

          if (properties.prefLabel && properties.prefLabel.length > 0) {
            for (var i=0; i<properties.prefLabel.length; i++) {
              if (properties.prefLabel[i].lang == lang) {
                return properties.prefLabel[i].value;
              }
            }
            return properties.prefLabel[0].value;
          }

          return "-";
        }

        elem.select2({
          allowClear: true,
          multiple: !!attrs.multiple,
          query: function(query) {
            ResourceList.query({
              schemeId: scope.refAttr.range.scheme.id,
              typeId: scope.refAttr.range.id,
              query: query.term
            }, function(results) {
              query.callback({
                results: results
              });
            });
          },
          formatResult: function(result) {
            return getLocalizedPrefLabel(result.properties);
          },
          formatSelection: function(result) {
            return getLocalizedPrefLabel(result.properties);
          }
        });

        elem.select2("container").find("ul.select2-choices").sortable({
          containment: 'parent',
          start: function() {
            elem.select2("onSortStart");
          },
          update: function() {
            elem.select2("onSortEnd");
          }
        });

        elem.on('change', function() {
          scope.$apply(function() {
            if (!elem.select2('data')) {
              scope.ngModel = "";
            } else {
              scope.ngModel = elem.select2('data');
            }
          });
        });

        scope.$watch('ngModel', function(ngModel) {
          if (!ngModel) {
            if (elem.select2('data')) {
              // defer clean to avoid element change inside $watch
              $timeout(function() {
                elem.select2('data', '');
              });
            }
            return;
          }

          if (attrs.multiple) {
            var promiseGet = function(idObject) {
              var d = $q.defer();
              Resource.get({
                schemeId: scope.refAttr.range.scheme.id,
                typeId: scope.refAttr.range.id,
                id: idObject.id
              }, function(result) {
                d.resolve(result);
              });
              return d.promise;
            };

            var promises = [];
            for (var i = 0; i < ngModel.length; i++) {
              promises.push(promiseGet(ngModel[i]));
            }

            // wait for all Resource.gets
            $q.all(promises).then(function(data) {
              elem.select2('data', data);
            });
          } else {
            Resource.get({
              schemeId: scope.refAttr.range.scheme.id,
              typeId: scope.refAttr.range.id,
              id: ngModel.id
            }, function(resource) {
              elem.select2('data', resource);
            });
          }
        }, true);
      }
    };
  });

})(window.angular);
