(function (angular) { 'use strict';

angular.module('termed.translations', ['pascalprecht.translate', 'ngSanitize'])

.config(function($translateProvider) {
  $translateProvider

  .preferredLanguage('fi')

  .useSanitizeValueStrategy('escapeParameters')

  .translations('fi', {
    termed: 'Termieditori',
    allResources: 'kaikki tietueet',
    resources: 'tietueet',
    resource: 'tietue',
    type: 'tyyppi',
    label: 'nimike',
    source: 'lähde',
    scheme: 'aineisto',
    schemes: 'aineistot',
    hierarchy: 'hierarkia',
    list: 'lista',
    tree: 'puu',
    example: 'esimerkki',
    concept: 'käsite',
    description: 'kuvaus',
    searchHelp: 'hakuohje',
    referrers: 'viittaukset',
    parentHierarchies: 'hierarkiat',
    searchResources: 'etsi tietueita',
    searchSchemes: 'etsi aineistoja',
    addNewScheme: 'lisää uusi aineisto',
    addResource: 'lisää tietue',
    addChildResource: 'lisää alatietue',
    showMoreResults: 'näytä lisää hakutuloksia',
    searchValue: 'etsi ja valitse',
    addNewValue: 'lisää uusi',
    add: 'lisää',
    addNew: 'lisää uusi',
    save: 'tallenna',
    remove: 'poista',
    edit: 'muokkaa',
    download: 'lataa',
    updated: 'muokkattu',
    added: 'lisätty',
    warnSlowAllResourcesView: 'Huom. näkymän avautuminen voi aineiston koosta riippuen kestää useita minuutteja.'
  })

  .translations('en', {
    termed: 'Termed',
    allResources: 'all resources',
    resources: 'resources',
    resource: 'resource',
    type: 'type',
    label: 'label',
    source: 'source',
    scheme: 'scheme',
    schemes: 'schemes',
    hierarchy: 'hierarchy',
    list: 'list',
    tree: 'tree',
    example: 'example',
    concept: 'concept',
    description: 'description',
    searchHelp: 'search help',
    referrers: 'referrers',
    parentHierarchies: 'parent hierarchies',
    searchResources: 'search resources',
    searchSchemes: 'search schemes',
    addNewScheme: 'add new scheme',
    addResource: 'add resource',
    addChildResource: 'add child resource',
    showMoreResults: 'show more results',
    searchValue: 'search and select',
    addNewValue: 'add new',
    add: 'add',
    addNew: 'add new',
    save: 'save',
    remove: 'remove',
    edit: 'edit',
    download: 'download',
    updated: 'updated',
    added: 'added',
    warnSlowAllResourcesView: 'Note that opening all resources view might take several minutes.'
  });

});

})(window.angular);
