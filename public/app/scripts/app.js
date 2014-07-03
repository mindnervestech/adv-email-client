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
  'ngSanitize',
  'angularTreeview',
  'ngDragDrop'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '/assets/app/views/home.html',
        controller: 'SearchController'	
      })
      .when('/admin', {
        templateUrl: '/assets/app/views/home.html',
        controller: 'SearchController'	
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
      .when('/list',{
    	  templateUrl: '/assets/app/views/list.html',
          controller: 'AdminController'
      })
      .otherwise({
          redirectTo: '/'
      });
  })
  .run(function(editableOptions) {
     editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});


(function(f){f.module("angularTreeview",[]).directive("treeModel",function($compile){return{restrict:"A",link:function(b,h,c){var a=c.treeId,g=c.treeModel,e=c.nodeLabel||"label",d=c.nodeChildren||"children",e='<ul><li data-ng-repeat="node in '+g+'"><i class="collapsed" data-ng-show="node.'+d+'.length && node.collapsed" data-ng-click="'+a+'.selectNodeHead(node)"></i><i class="expanded" data-ng-show="node.'+d+'.length && !node.collapsed" data-ng-click="'+a+'.selectNodeHead(node)"></i><i class="normal" data-ng-hide="node.'+
	d+'.length"></i> <span data-ng-class="node.selected" ng-click="loadSubSubscription(node.subscriptionId)">{{node.'+e+'}}{{node.count!=null ? " ("+node.count+")" : ""}}</span><div data-ng-hide="node.collapsed" data-tree-id="'+a+'" data-tree-model="node.'+d+'" data-node-id='+(c.nodeId||"id")+" data-node-label="+e+" data-node-children="+d+"></div></li></ul>";a&&g&&(c.angularTreeview&&(b[a]=b[a]||{},b[a].selectNodeHead=b[a].selectNodeHead||function(a){a.collapsed=!a.collapsed},b[a].selectNodeLabel=b[a].selectNodeLabel||function(c){b[a].currentNode&&b[a].currentNode.selected&&
	(b[a].currentNode.selected=void 0);c.selected="selected";b[a].currentNode=c}),h.html('').append($compile(e)(b)))}}})})(angular);
