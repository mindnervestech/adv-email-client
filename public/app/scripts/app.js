'use strict';

angular.module('email-client', [
  'infinite-scroll',
  'ngResource',
  'ngRoute',
  'xeditable',
  'ngAnimate',
  'ui.bootstrap',
  'ui.bootstrap.tpls',
  'angularFileUpload',
  'ui.bootstrap.datetimepicker',
  'validator',
  'validator.rules',
  'angularSpinner' ,
  'ngSanitize'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '/assets/app/views/home.html',
        controller: 'SearchController'	
      })
      .when('/admin', {
        templateUrl: '/assets/app/views/home.html',
        controller: 'AdminController'	
      })
      .when('/adminBL', {
        templateUrl: '/assets/app/views/admin.html',
        controller: 'AdminController'	
      })
      .when('/stats',{
    	  templateUrl: '/assets/app/views/stats.html',
          controller: 'AdminController'
      })
      .when('/statictical',{
    	  templateUrl: '/assets/app/views/statictical.html',
          controller: 'AdminController'
      })
      .when('/userstatictical',{
    	  templateUrl: '/assets/app/views/statictical.html',
          controller: 'SearchController'
      })
      .otherwise({
          redirectTo: '/'
      });
  })
  .run(function(editableOptions) {
     editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

