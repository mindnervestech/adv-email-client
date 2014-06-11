
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
emailclient.service('BlacklistedDomains', function($resource){
    this.getDomains = $resource('/get-blacklisted-domains',
                           {alt:'json',callback:'JSON_CALLBACK'},{
                                     get: {method:'GET'}
                           }
    );
    });

emailclient.controller('AdminController',function($scope,$location,$http,usSpinnerService,BlacklistedDomains){
	console.log($location.path());
	$scope.predicate = 'sentDate';
	$scope.reverse=true;
	$scope.isadmin = true;
	$scope.blDomains;
	$scope.blAddresses;
	$scope.blKeywords;
	$scope.tabs = [
	               { title:'Black List',active: true, content:'/assets/app/views/blacklist.html' }
	              // ,{ title:'Upcomings', content:'/assets/app/views/upcoming.html'}
	               ];
	$scope.PieChart= {
			monthYear:'none',
			year:'none',
			stat:''
	}
	$scope.tabs.blacklist = [
	                          { title:'By Domain',active: true, content:'/assets/app/views/domain.html' },
	                          { title:'By Email Address', content:'/assets/app/views/address.html' },
	                          { title:'By Subject Keyword', content:'/assets/app/views/token.html' }
	                          ];
	$scope.tabs.stats = [
	                     {title:'Pie',active:true,content:'/assets/app/views/statsby.html'},
	                     {title:'Bar Chart',content:'/assets/app/views/barchart.html'}
	                     ];
	
	$scope.s1="";
	
	$scope.getChart = function(){
		var month=$scope.PieChart.monthYear;
		var year=$scope.PieChart.year;
		
		if(year=="all"){
			$scope.PieChart.stat="All";
			$http.get('/get-all-chart')
			.success(function(data, status, headers, config){
				$scope.dataAssingment(data);
				
			});
		}else if(year!="none" && month=="none"){
			if(year=="current"){
				year=new Date().getFullYear();
			} else {
				year=new Date().getFullYear()-1;
			}
			$scope.PieChart.stat=year;
			$http.get('/get-year-chart/'+year)
			.success(function(data, status, headers, config){
				$scope.dataAssingment(data);
			});
		}else if(year!="none" && month!="none"){
			if(year=="current"){
				year=new Date().getFullYear();
			} else {
				year=new Date().getFullYear()-1;
			}
			
			$scope.PieChart.stat=month+'-'+year;
			$http.get('/get-month-chart/'+year+'-'+month)
			.success(function(data, status, headers, config){
				switch(month){
				case "1":
					$scope.PieChart.stat="Jan "+year;
					break;
				case "2":
					$scope.PieChart.stat="Feb "+year;
					break;
				case "3":
					$scope.PieChart.stat="Mar "+year;
					break;
				case "4":
					$scope.PieChart.stat="Apr "+year;
					break;
				case "5":
					$scope.PieChart.stat="May "+year;
					break;
				case "6":
					$scope.PieChart.stat="Jun "+year;
					break;
				case "7":
					$scope.PieChart.stat="Jul "+year;
					break;
				case "8":
					$scope.PieChart.stat="Aug "+year;
					break;
				case "9":
					$scope.PieChart.stat="Sep "+year;
					break;
				case "10":
					$scope.PieChart.stat="Oct "+year;
					break;
				case "11":
					$scope.PieChart.stat="Nov "+year;
					break;
				case "12":
					$scope.PieChart.stat="Dec "+year;
					break;
				default:
					$scope.PieChart.stat=year;
				break;
				}
				$scope.dataAssingment(data);
			});
		}else {
			alert("Please Select Year!");
		}
	};
	$scope.dataAssingment = function(data){
		//console.log(data);
		
		 $('#pie-container').highcharts({
		        chart: {
		            plotBackgroundColor: null,
		            plotBorderWidth: null,
		            plotShadow: false,
		            width:1250,
		            height:500
		        },
		        title: {
		            text: 'Statics of '+$scope.PieChart.stat
		        },
		        tooltip: {
		            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
		        },
		        plotOptions: {
		            pie: {
		                allowPointSelect: true,
		                cursor: 'pointer',
		                dataLabels: {
		                    enabled: true,
		                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
		                    style: {
		                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
		                    }
		                }
		            }
		        },
		        series: [{
		            type: 'pie',
		            name: 'Domain',
		            data: data
		        }]
		    });
	}
	
	
	
		
	
	$scope.count=0;
	$scope.getChart1 = function(count){
		if($scope.count < 0){
			$scope.count=30;
		}
			$scope.PieChart.stat="All";
			$http.get('/get-all-chart1/'+$scope.count)
			.success(function(data, status, headers, config){
				$scope.dataAssingment1(data);
				$scope.count=$scope.count+30;
				
			});
	
	};
	
	
	$scope.dataAssingment1 = function(data){
		console.log(data);
        $('#container').highcharts({
            chart: {
                type: 'column',
                marginBottom: 140,
              width:1250
             
                	
            },
           
            title: {
                text: 'Status of Bar Chart'
            },
           
            xAxis: {
                type: 'category',
                labels: {
                    rotation: -45,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif'
                    }
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Numbers'
                }
            },
            legend: {
                enabled: false
            },
            tooltip: {
                pointFormat: '<b>{point.y} Domains</b>',
            },
            series: [{
                name: 'Domain',
                data: data,
                dataLabels: {
                    enabled: true,
                    rotation: -90,
                    color: '#FFFFFF',
                    align: 'right',
                    x: 4,
                    y: 10,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif',
                        textShadow: '0 0 3px black'
                    }
                }
            }]
        });
    }
    
	
	
	$scope.getChartprev = function(count){
		if($scope.count != 0){
			$scope.count=$scope.count-30;
		}
		
			$scope.PieChart.stat="All";
			$http.get('/get-all-chartprev/'+$scope.count)
			.success(function(data, status, headers, config){
				$scope.dataAssingment1(data);
				$scope.count=$scope.count-30;
				
			});
			
	};
	
	
	
	$scope.removeBLDomain= function(domainId)
	{
		$http.get('/remove-BLDomain/'+domainId)
		.success(function(data, status, headers, config){
			if(data)
			{
				$scope.getBlackList();
			}
		});
	};
	$scope.removeBLEmail=function(emailId)
	{
		$http.get('/remove-BLEmail/'+emailId)
		.success(function(data, status, headers, config){
			if(data)
			{
				$scope.getBlackList();
			}
		});
	};
	
	$scope.removeBLKeyword=function(keywordId)
	{
		$http.get('/remove-BLKeyword/'+keywordId)
		.success(function(data, status, headers, config){
			if(data)
			{
				$scope.getBlackList();
			}
		});
	};
	
	$scope.getBlackList=function()
	{
		$http.get('/get-blacklisted')
		.success(function(data, status, headers, config){
			$scope.blDomains = data.domainList;
			$scope.blAddresses = data.emailList;
			$scope.blKeywords =data.keywordList;
			//console.log($scope.blDomains);
			usSpinnerService.stop('loading...');
		});
	};
	
	$scope.formData = {
			domainToBeAdded : '',
			email:'',
			keyword : ''
	};
	
	$scope.addKeywordToBL = function () { 
		
		$http.get('/addKeywordToBL/' + $scope.formData.keyword)
		.success(function(data, status, headers, config){
			//alert(data.domainList[0]);
			//alert(data.list[0]!=null);
			$scope.formData.domainToBeAdded='';
			if(data.keywordList[0]!=null)
			{
				//alert("1");
				$scope.blKeywords.push(data.keywordList[0]);
			}
		});
	};
	
	$scope.addDomainToBL = function () { 
		$http.get('/addDomainToBL/' + $scope.formData.domainToBeAdded)
		.success(function(data, status, headers, config){
			$scope.formData.domainToBeAdded='';
			if(data.domainList[0]!=null)
			{
				$scope.blDomains.push(data.domainList[0]);
			}
		});
	};
	
	$scope.addEmailToBL = function () { 
		$http.get('/addEmailToBL/' + $scope.formData.email)
		.success(function(data, status, headers, config){
			$scope.formData.email='';
			if(data.emailList[0]!=null)
			{
				$scope.blAddresses.push(data.emailList[0]);
			}
		});
	}
	

});

emailclient.controller('SearchController',function($scope, $location,$http, $modal,$sce, usSpinnerService){
	$scope.predicate = 'sentDate';
	$scope.reverse=false;
	$scope.isHide = true;
	/*  Below Section for modal  */
	if($location.path()=="/admin")
	{
		$scope.isadmin = true;
	}
	else
	{
		$scope.isadmin = false;
	}
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
	  $scope.removeEmailData=function(id,indexId)
	  {
		  //alert("in remove "+id+" "+indexId);
		  $http.get('/remove-Email-Data/'+id+'/'+indexId)
			.success(function(data, status, headers, config){
				//alert("data "+data);
				$scope.submitSearch();
				/*if(data)
				{
					//alert("in if");
					$scope.getBlackList();
				}*/
			});
	  };
	  $scope.downloadPDF =function(popUpId) {
		  $http.get('/downloadPdf/'+popUpId, {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$('.modal-headerPopUp').append(data.htmlToShowMailPopUp);
				usSpinnerService.stop('loading...');
			});
	  };
	  $scope.showPopUpModal=function (popUpId) {
		  $scope.searchForm.popUpId = popUpId;
		  usSpinnerService.spin('loading...');
		  $http.get('/showPopUpModal', {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$('.modal-bodyPopUp').append(data.htmlToShowMailPopUp);
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
		  usSpinnerService.spin('loading...');
		  $http.get('/showPopUpForImages', {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$('.modal-headerPopUp').append(data.htmlToShowMailPopUp);
				usSpinnerService.stop('loading...');
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
		if($scope.toggle && !($scope.currentPage == 0 && $scope.searchForm.page == 0)) {
		$scope.searchForm.page =  $scope.currentPage ;
		
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
	
	$scope.subjectSort = function(reverse){
		$scope.predicate='subject';
		$scope.reverse=reverse;
		$scope.submitSearch();
	};
	$scope.dateSort = function(reverse){
		$scope.predicate='sentDate';
		$scope.reverse=reverse;
		$scope.submitSearch();
	};
	$scope.domainSort = function(reverse){
		$scope.predicate='domain';
		$scope.reverse=reverse;
		$scope.submitSearch();
	};
	
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
		$scope.searchForm.page = 0;
		$scope.currentPage = 0;
		$scope.toggle = true;
		$scope.soButton = true;
		
		search(function(data) {
			
			$scope.emails = data.emails;
			if($scope.emails.length==0){
				$scope.soButton = 0;
				}
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
		//alert($scope.predicate+'/'+$scope.reverse);
		$http.get('/searchForEmails/'+$scope.predicate+'/'+$scope.reverse, {params:$scope.searchForm})
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
