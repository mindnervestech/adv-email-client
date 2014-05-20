
'use strict';

var emailclient = angular.module('email-client');

emailclient.controller('ApplicationController',function($scope){

});


emailclient.filter('startFrom', function() {
    return function(input, start) {
        start = +start; 
        return input.slice(start);
    }
});

emailclient.controller('SearchController',function($scope, $http, $modal,$sce, usSpinnerService){
	
	/*  Below Section for modal  */
	  var modalInstance;
	  $scope.open = function () {
		    modalInstance = $modal.open({
		      templateUrl: '/assets/app/views/myModal.html',
		      scope : $scope
		    });
	  };
	  $scope.s1 = null;
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
	  $scope.trustSrc = function (url) {
		  return $sce.trustAsResourceUrl(url);
	  }
	  
	  $scope.currentPage = 0;
	    $scope.pageSize = 6;
	    $scope.data = [];
	    $scope.s1 = [];
	    for (var i=0; i<45; i++) {
	        $scope.data.push("Item11 "+i);
	    }
		
	  $scope.getlinkImageByID1=function (popUpId) {
		  $scope.searchForm.popUpId = popUpId;
		  usSpinnerService.spin('loading...');
		  $http.get('/get-link-image-by-id/'+popUpId, {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$scope.s1= data;
				$('.modal-bodyPopUp').append(data.htmlToShowMailPopUp);
				usSpinnerService.stop('loading...');
			});
		  modalInstance = $modal.open({
		      templateUrl: '/assets/app/views/iframe.html',
		      scope : $scope
		    });
	  };
	  
	  $scope.numberOfPages=function(){
	        return Math.ceil($scope.s1.length/$scope.pageSize);                
	    }

	
	  
	  
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
			  search(function(data){
					$scope.emails = data.emails;
					$scope.hasSearchResult = data.emails.length != 0;
					$scope.noOFPages = data.noOFPages;
					$scope.totalItems = data.noOFPages * $scope.searchForm.rowCount ; // Added for Pagination
					usSpinnerService.stop('loading...');
			  });
		  }
	});
	
	$scope.$watch('allChecked', function(value){
		if($scope.domainCounts) {
			for(var i=0 ; i < $scope.domainCounts.length; i++) {
				$scope.domainCounts[i].sel = value; 
			}
		}
	});
	  
	/*  Above Section for pagination  */  
	  
	$scope.noOfRows = 'Per Page';
	$scope.sortBy = 'Sort By';
	$scope.searchForm= {
				from : '',
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
				saveSearchName :"",
				levelOnekeyWord :""
	}
	
	$scope.submitSearch = function(count,sortText) {
		if(sortText == null){
			$scope.sortBy = 'Sort By';
		}else{
			$scope.sortBy = sortText;
		}
		
		if(count == null){
			$scope.noOfRows = 'Per Page';
			$scope.searchForm.rowCount = 10;
		}else{
			$scope.noOfRows = count;
			$scope.searchForm.rowCount = count;
		}
		$scope.isVisible = true;
		
		search(function(data) {
			
			$scope.emails = data.emails;
			$scope.hasSearchResult = data.emails.length != 0;
			$scope.domainCounts = data.domainCounts; 
			$scope.noOFPages = data.noOFPages;
			$scope.totalItems = data.noOFPages * $scope.searchForm.rowCount ; // Added for Pagination
			$scope.saveSearchSets = data.saveSearchSets;        // Added for saveSearchSet
			$scope.hasQuickSearch = data.saveSearchSets.length != 0;
			usSpinnerService.stop('loading...');
			$scope.allChecked = true;
		});
	}
	
	function search(callback) {
		usSpinnerService.spin('loading...');
		$scope.searchForm.domainChecked = "";
		$http.get('/searchForEmails', {params:$scope.searchForm})
		.success(function(data, status, headers, config){
			$("#test").click()
			if(callback) {
				callback(data);
			}
			
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
		search(function(data){
			$scope.emails = data.emails;
			$scope.hasSearchResult = data.emails.length != 0;
			$scope.noOFPages = data.noOFPages;
			$scope.totalItems = data.noOFPages * $scope.searchForm.rowCount ; // Added for Pagination
			usSpinnerService.stop('loading...');
		});
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
			$scope.allChecked = true;
		});
	}
});
