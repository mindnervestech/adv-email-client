
'use strict';

var emailclient = angular.module('email-client');

emailclient.controller('ApplicationController',function($scope){

});

emailclient.controller('SearchController',function($scope, $http, $modal, usSpinnerService){
		$scope.allChecked = true;
	/*  Below Section for modal  */
	  var modalInstance;
	  $scope.open = function () {
		    modalInstance = $modal.open({
		      templateUrl: '/assets/app/views/myModal.html',
		      scope : $scope
		    });
	  };
		  
	  $scope.ok = function () {
		  usSpinnerService.spin('loading...');
		  $http.get('/saveEmailSearchSet',{params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$scope.saveSearchSets = data.saveSearchSets;
				usSpinnerService.stop('loading...');
			});
	    modalInstance.close();
	  };
	  $scope.cancel = function () {
	    modalInstance.dismiss('cancel');
	  };
    /*  Above Section for modal  */  
	 
	  /*  below Section for email popup modal  */ 
	  $scope.showPopUpModal=function (popUpId) {
		  $scope.searchForm.popUpId = popUpId;
		  usSpinnerService.spin('loading...');
		  $http.get('/showPopUpModal', {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$('.modal-headerPopUp').append(data.htmlToShowMailPopUp);
				usSpinnerService.stop('loading...');
			});
		  modalInstance = $modal.open({
		      templateUrl: '/assets/app/views/myEmailModal.html',
		      scope : $scope
		    });
	  };
    /*  Above Section for email popup modal  */ 
	
	  /*  below Section for email images modal  */ 
	  $scope.showPopUpImages=function (eIdForImages) {
		  $scope.searchForm.eIdForImages = eIdForImages;
		  $http.get('/showPopUpForImages', {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$('.modal-headerPopUp').append(data.htmlToShowMailPopUp);
			});
		  modalInstance = $modal.open({
		      templateUrl: '/assets/app/views/myEmailModal.html',
		      scope : $scope
		    });
	  };
    /*  Above Section for email images modal  */ 
	  
	  /*  below Section for email links modal  */ 
	  $scope.showPopUpLinks=function (eIdForLinks) {
		  $scope.searchForm.eIdForLinks = eIdForLinks;
		  $http.get('/showPopUpForLinks', {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$('.modal-headerPopUp').append(data.htmlToShowMailPopUp);
			});
		  modalInstance = $modal.open({
		      templateUrl: '/assets/app/views/myEmailModal.html',
		      scope : $scope
		    });
	  };
    /*  Above Section for email links modal  */ 
	  
	  
	/*  Below Section for pagination  */
	$scope.totalItems = 0;
	$scope.currentPage = 0;
	$scope.prevPage = -1;
	$scope.maxSize = 5;
	
	
	$scope.$watch('currentPage', function(newPage){
		  if($scope.prevPage != newPage && newPage != 0) {
			  $scope.searchForm.page = newPage - 1 ;
			  $scope.submitSearch();
		  }
	});
	  
	/*  Above Section for pagination  */  
	  
	$scope.searchForm= {
				from : "",
				to : new Date(),
				domain : [],
				domainChecked:"",
				cntKeyWord : "",
				subKeyWord : "",
				page : 0,
				saveSearchId: 0,
				rowCount : 10,
				url:"",
				popUpId:0,
				eIdForImages :0,
				eIdForLinks : 0,
				saveSearchName :""
	}
	
	$scope.submitSearch = function() {
		usSpinnerService.spin('loading...');
		$http.get('/searchForEmails', {params:$scope.searchForm})
		.success(function(data, status, headers, config){
			$scope.emails = data.emails;
			$scope.hasSearchResult = data.emails.length != 0;
			$scope.domainCounts = data.domainCounts; 
			$scope.noOFPages = data.noOFPages;
			$scope.totalItems = data.noOFPages * $scope.searchForm.rowCount ; // Added for Pagination
			$scope.saveSearchSets = data.saveSearchSets;        // Added for saveSearchSet
			$scope.hasQuickSearch = data.saveSearchSets.length != 0;
			usSpinnerService.stop('loading...');
		});
	}
	$scope.getTimes=function(n){
		var arr = [];
		for (var i = 0; i < n; i++){
		      arr.push(i);
		}
	     return arr;
	};
	
	$scope.emailPaging = function() {
		$scope.searchForm.page++;
		$scope.submitSearch();
	}
	
	$scope.openEmailModal = function(emailID) {
		
	}
	$scope.requestPager= function(pageNo) {
		usSpinnerService.spin('loading...');
		$scope.searchForm.page = pageNo;
		$http.get('/searchForEmails', {params:$scope.searchForm})
		.success(function(data, status, headers, config){
			usSpinnerService.stop('loading...');
			$scope.emails = data.emails;
			$scope.saveSearchSets = data.saveSearchSets; 
			$scope.noOFPages = data.noOFPages;
			$scope.totalItems = data.noOFPages * $scope.searchForm.rowCount ; // Added for Pagination
		});
	}
	  $scope.addDomain = function(domain) {
		    if ($scope.checked_domains.indexOf(domain) != -1) return;
		    $scope.checked_domains.push(domain);
		  };
	$scope.showSearchResult= function(url)
	{
		usSpinnerService.spin('loading...');
		$http.get('/searchForEmails?'+url)
		.success(function(data, status, headers, config){
			usSpinnerService.stop('loading...');
			$scope.emails = data.emails;
			$scope.domainCounts = data.domainCounts; 
			$scope.noOFPages = data.noOFPages;
			$scope.totalItems = data.noOFPages * $scope.searchForm.rowCount ; // Added for Pagination
			$scope.saveSearchSets = data.saveSearchSets;        // Added for saveSearchSet
		});
	}
		  
		  
	$scope.filterEmailSearch= function() {
		$scope.searchForm.domainChecked = "";
		usSpinnerService.spin('loading...');
		for(var i=0 ; i < $scope.domainCounts.length; i++) {
			if($scope.domainCounts[i].sel) {
				$scope.searchForm.domainChecked += $scope.domainCounts[i].name+",";
			}
		}
		$http.get('/filterSearch',{params:$scope.searchForm})
		.success(function(data, status, headers, config){
			usSpinnerService.stop('loading...');
			$scope.emails = data.emails;
			$scope.saveSearchSets = data.saveSearchSets; 
			$scope.noOFPages = data.noOFPages;
			$scope.totalItems = data.noOFPages * $scope.searchForm.rowCount ; // Added for Pagination
		});
	}
});
