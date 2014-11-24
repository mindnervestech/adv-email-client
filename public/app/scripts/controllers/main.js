
'use strict';

var emailclient = angular.module('email-client');

emailclient.controller('ApplicationController',function($scope){
	$scope.isadmin = false;
	$scope.initData = function(isAdmin) {
		$scope.isadmin = isAdmin;
	}
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

emailclient.controller('AdminController',function($scope,$location,$http,$modal,usSpinnerService,BlacklistedDomains){
	console.log($location.path());
	$scope.selectedIndex = -1;
	$scope.predicate = 'relevance';
	$scope.reverse=true;
	//$scope.isadmin = false;
	$scope.isData=false;
	$scope.isDivShow=false;
	$scope.totalEmails;
	$scope.blDomains;
	$scope.blAddresses;
	$scope.blKeywords;
	$scope.domainBar;
	$scope.emailBar;
	$scope.keywordBar;
	$scope.databaseSize;
	$scope.mailFolderSize;
	$scope.subscription;
	$scope.dragElementId;
	$scope.isSubscription=false;
	$scope.subscriptionSelected=null;
	$scope.dragFrom;
	$scope.parentClicked;
	$scope.unAssigned;
	$scope.parent;
	$scope.child;
	$scope.upload = {
		file:''
	};
	
	$scope.getWordCloudById = function (id) {
		 $scope.wordCloudId = id;
		   //$scope.searchForm.popUpId = popUpId;
		  usSpinnerService.spin('loading...');
		  $http.get('/get-word-cloud-by-id/'+id)
			.success(function(data, status, headers, config) {
				
				//$('#wordcloudimage').attr('src','data:image/svg+xml,'+data);
				$scope.wc = 'data:image/svg+xml,'+data;
				usSpinnerService.stop('loading...');
				modalInstance = $modal.open({
				      templateUrl: '/assets/app/views/wordcloud.html',
				      scope : $scope
				    });
			});
		  
	  };
	
	
	// List Start
	
	$scope.roleList ;
	$scope.list1 = [];
	$scope.parentDomain = {
			domain:''
		};
	
	$scope.list2 = [];
	$scope.list3 = [];
	  
	$scope.draglist1 = function (e) {
		console.log(e.currentTarget.id);
		  $scope.dragFrom = "1";
		  $scope.dragElementId = e.currentTarget.id; 
	};
	 
	$scope.draglist2 = function (e) {
		//console.log(id);
		$scope.dragElementId = e.currentTarget.id;
		  $scope.dragFrom = "2";
	};
	  
	$scope.draglist3 = function (e) {
		//console.log(id);
		$scope.dragElementId = e.currentTarget.id;
		  $scope.dragFrom = "3";
	};

	  
	$scope.setSelectedIndex = function (index) {
		  $scope.selectedIndex = index;
	}
	  
	$scope.unsetSelectedIndex = function () {
		  $scope.selectedIndex = null;
		  $scope.subscriptionSelected = null;
	}
	  
	$scope.addparentpopup = function () {
		modalInstance = $modal.open({
			templateUrl: '/assets/app/views/addparentsub.html',
			scope : $scope
		});
	};
	  
	$scope.addparentmanually = function() {
		$http.get('/addparentdomain/'+$scope.parentDomain.domain)
		.success(function(data, status, headers, config) {
			$scope.subscriptionSelected = null;
			$scope.selectedIndex = -1;
			$scope.loadLists();
			$scope.dragFrom=0;
			modalInstance.close();
		});
	};
	  
	$scope.dragTo = 0;
	$scope.optionsList1 = {
		accept: function(dragEl) {
			if($scope.dragFrom == "2" || $scope.dragFrom == "3") {
			    return true;
			} else {
				return false;
			}
		}
	};
	  
	$scope.optionsList2 = {
		accept: function(dragEl) {
			if($scope.dragFrom == "1") {
				return true;
			} else {
				return false;
			}
		} 	
	};
	
	$scope.list1Drop = function() {
		if($scope.dragFrom == "2") {
			//console.log("in list 1 drop dragfrom=2");
			$http.get('/removeparentsubscription/'+$scope.dragElementId)
			.success(function(data, status, headers, config) {
				if($scope.subscriptionSelected == $scope.dragElementId) {
					$scope.subscriptionSelected = null;
					$scope.selectedIndex = -1;
				}
				$scope.loadLists();
				$scope.dragFrom=0;
			});
	     } else if($scope.dragFrom == "3") {
	    	 console.log("in list 1 drop dragfrom=3");
	    	 $http.get('/removechildsubscription/'+$scope.dragElementId)
	    	 .success(function(data, status, headers, config) {
	    		 $scope.dragFrom=0;
	    		 $scope.loadLists();
	    	 });
	     }
	};
	
	$scope.list2Drop = function() {
		$http.get('/addparentsubscription/'+$scope.dragElementId)
		.success(function(data, status, headers, config) {
			$scope.dragFrom=0;
			$scope.loadLists();
		});
	};
	
	$scope.list3Drop = function() {
		  
		  //console.log("in drop function list 3");
		$http.get('/addchildsubscription/'+$scope.dragElementId+'/'+$scope.subscriptionSelected)
		.success(function(data, status, headers, config) {
			$scope.dragFrom=0;
			$scope.loadLists();
		});
	};
	 
	$scope.optionsList3 = {
		accept: function(dragEl) {
			if($scope.dragFrom == "1" && $scope.subscriptionSelected != null) {
				return true;
			} else if($scope.dragFrom == "1" && $scope.subscriptionSelected == null){
				//console.log("in list 3 accept dragfrom=0");
				//alert("Please Select Parent Before Adding Child!");
				return false;
			} else {
				return false;
			}
		}	    
	};
	  
	$scope.loadLists = function () {
		usSpinnerService.spin('loading...');
		$http.get('/loadlists')
		.success(function(data, status, headers, config) {
			$scope.list1 = data.unAssignedList;
			$scope.list2 = data.assignedList;
			$scope.list3 = data.assignedChildList;
			$scope.unAssigned = data.totalUnAssignedNumber;
			$scope.parent = data.totalParentNumber;
			$scope.child = data.totalChildNumber;
			$scope.dragFrom=0;
			usSpinnerService.stop('loading...');
		});
	};
	  
	$scope.getChildSubscription = function(id) {
		 // $scope.dragElementId = id;
		$scope.subscriptionSelected = id;
		usSpinnerService.spin('loading...');
		$http.get('/loadchildlist/'+id)
		.success(function(data, status, headers, config) {
			$scope.list3 = data.assignedChildList;
			usSpinnerService.stop('loading...');
		});
	};
	
	// List End
	/*if($location.path()=="/statictical" || $location.path()=="/adminBL"||$location.path()=="/admin") {
		$scope.isadmin = true;
	}*/
	
	$scope.feedbackSuccess = false;
	$scope.tabs = [
	               { title:'Black List',active: true, content:'/assets/app/views/blacklist.html' }
	              // ,{ title:'Upcomings', content:'/assets/app/views/upcoming.html'}
	               ];
	$scope.PieChart= {
			monthYear:'none',
			year:'none',
			stat:'',
			fromMonthYear:'',
			toMonthYear:''
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
	var modalInstance;
	$scope.open = function () {
	    modalInstance = $modal.open({
	      templateUrl: '/assets/app/views/myModal.html',
	      scope : $scope
	    });
	};
	$scope.cancel = function () {
	    modalInstance.dismiss('cancel');
	};
	$scope.getDataSize =function() {
		usSpinnerService.spin('loading...');
		$http.get('/get-data-size')
		.success(function(data, status, headers, config) {
			if(data[0]<1000) {
				$scope.databaseSize=data[0].toFixed(2)+' MB\'s';
			} else {
				$scope.databaseSize=(data[0]/1024.00).toFixed(2)+' GB\'s';
			}
			if(data[1]<1000) {
				$scope.mailFolderSize=data[1].toFixed(2)+' MB\'s';
			} else {
				$scope.mailFolderSize=(data[1]/1024).toFixed(2)+' GB\'s';
			}
			usSpinnerService.stop('loading...');
		});
	};
	$scope.getDomainStats = function() {
		var fromMonthYear=$scope.PieChart.fromMonthYear;
		var toMonthYear=$scope.PieChart.toMonthYear;
		//alert(fromMonthYear);
		//alert(toMonthYear);
		if(fromMonthYear == '' || toMonthYear == '') {
			alert("Please Select Start and End Month!");
		} else {
			var fromArr=$scope.PieChart.fromMonthYear.split("-");
			var toArr=$scope.PieChart.toMonthYear.split("-");
			var fromMonth=fromArr[0];
			var fromYear=fromArr[1];
			var toMonth=toArr[0];
			var toYear=toArr[1];
			//alert(fromArr);
			//alert(toArr);
			if(fromYear>=toYear && fromMonth>toMonth){
				alert("please Select valid Period!");
			} else {
				usSpinnerService.spin('loading...');
				$http.get('/get-period-stats/'+fromMonthYear+'/'+toMonthYear)
				.success(function(data, status, headers, config){
					console.log(data);
					if(data.length>0) {
						$scope.totalEmails=data[0].total;
						$scope.isData=true;
						$scope.statisticData=data;
					} else {
						$scope.isData=false;
					}
					usSpinnerService.stop('loading...');
				});
			}
		}
	}
		$scope.getChart = function(){
		var fromMonthYear=$scope.PieChart.fromMonthYear;
		var toMonthYear=$scope.PieChart.toMonthYear;
		if(fromMonthYear == '' || toMonthYear == '') {
			alert("Please Select Start and End Month!");
		} else {
			var fromArr=$scope.PieChart.fromMonthYear.split("-");
			var toArr=$scope.PieChart.toMonthYear.split("-");
			var fromMonth=fromArr[0];
			var fromYear=fromArr[1];
			var toMonth=toArr[0];
			var toYear=toArr[1];
			if(fromYear>=toYear && fromMonth>toMonth){
				alert("please Select valid Period!");
			} else {
				usSpinnerService.spin('loading...');
				$http.get('/get-month-chart/'+fromMonthYear+'/'+toMonthYear)
				.success(function(data, status, headers, config){
					switch(fromMonth) {
						case "01":
							$scope.PieChart.stat="Jan "+fromYear;
							break;
						case "02":
							$scope.PieChart.stat="Feb "+fromYear;
							break;
						case "03":
							$scope.PieChart.stat="Mar "+fromYear;
							break;
						case "04":
							$scope.PieChart.stat="Apr "+fromYear;
							break;
						case "05":
							$scope.PieChart.stat="May "+fromYear;
							break;
						case "06":
							$scope.PieChart.stat="Jun "+fromYear;
							break;
						case "07":
							$scope.PieChart.stat="Jul "+fromYear;
							break;
						case "08":
							$scope.PieChart.stat="Aug "+fromYear;
							break;
						case "09":
							$scope.PieChart.stat="Sep "+fromYear;
							break;
						case "10":
							$scope.PieChart.stat="Oct "+fromYear;
							break;
						case "11":
							$scope.PieChart.stat="Nov "+fromYear;
							break;
						case "12":
							$scope.PieChart.stat="Dec "+fromYear;
							break;
						default:
							$scope.PieChart.stat=fromYear;
							break;
					}
					switch(toMonth) {
						case "01":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Jan "+toYear;
							break;
						case "02":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Feb "+toYear;
							break;
						case "03":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Mar "+toYear;
							break;
						case "04":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Apr "+toYear;
							break;
						case "05":
							$scope.PieChart.stat=$scope.PieChart.stat+" to May "+toYear;
							break;
						case "06":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Jun "+toYear;
							break;
						case "07":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Jul "+toYear;
							break;
						case "08":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Aug "+toYear;
							break;
						case "09":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Sep "+toYear;
							break;
						case "10":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Oct "+toYear;
							break;
						case "11":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Nov "+toYear;
							break;
						case "12":
							$scope.PieChart.stat=$scope.PieChart.stat+" to Dec "+toYear;
							break;
						default:
							$scope.PieChart.stat=$scope.PieChart.stat+" to "+toYear;
						break;
					}
					$scope.dataAssingment(data);
					usSpinnerService.stop('loading...');
				});
				 modalInstance = $modal.open({
				      templateUrl: '/assets/app/views/statsby.html',
				      scope : $scope
				    });
			}
		}
	};
	
	$scope.dataAssingment = function(data){
		//console.log(data);
		
		 $('#pie-container').highcharts({
		        chart: {
		            plotBackgroundColor: null,
		            plotBorderWidth: null,
		            plotShadow: false,
		            width:1050,
		            height:400
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
	
	$scope.removeBLDomainFromStat =function(domainId) {
		//$scope.removeBLDomain(domainId);
		$http.get('/remove-BLDomain/'+domainId)
		.success(function(data, status, headers, config){
		});
		$scope.getDomainStats()
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
			if(data.domainList.length>0) {
				$scope.domainBar=true;
			} else {
				$scope.domainBar=false;
			}
			$scope.blAddresses = data.emailList;
			if(data.emailList.length>0) {
				$scope.emailBar=true;
			} else {
				$scope.emailBar=false;
			}
			$scope.blKeywords =data.keywordList;
			if(data.keywordList.length>0) {
				$scope.keywordBar=true;
			} else {
				$scope.keywordBar=false;
			}
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
				$scope.keywordBar=true;
			}
		});
	};
	$scope.addBLDomainFromStat = function(domainName) {
		if(confirm("Are you sure you want to put "+domainName.toUpperCase()+" to Blacklist? Yes/No")) {
			$scope.formData.domainToBeAdded=domainName;
			$http.get('/addDomainToBL/' + $scope.formData.domainToBeAdded)
			.success(function(data, status, headers, config){
			});
			$scope.getDomainStats();
		}
	};
	$scope.addDomainToBL = function () { 
		$http.get('/addDomainToBL/' + $scope.formData.domainToBeAdded)
		.success(function(data, status, headers, config){
			$scope.formData.domainToBeAdded='';
			if(data.domainList[0]!=null)
			{
				$scope.blDomains.push(data.domainList[0]);
				$scope.domainBar=true;
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
				$scope.emailBar=true;
			}
		});
	}
	$scope.isStatics = false;
	$scope.currentIndex = -1;
	
	$scope.showPopUpModalStatics = function(popUpId) {
		$scope.isStatics = true;
		$scope.showPopUpModal(popUpId);
	};
	
	$scope.showPopUpModal=function (popUpId) {
		  $scope.searchForm.popUpId = popUpId;
		  console.log($scope.searchForm.popUpId);
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
			$scope.subjectReverse=reverse;
			$scope.reverse=reverse;
			$scope.submitSearch();
		};
		$scope.dateSort = function(reverse){
			$scope.predicate='sentDate';
			$scope.dateReverse=reverse;
			$scope.reverse=reverse;
			$scope.submitSearch();
		};
		$scope.domainSort = function(reverse){
			$scope.predicate='domain';
			$scope.domainReverse=reverse;
			$scope.reverse=reverse;
			$scope.submitSearch();
		};
		$scope.relevanceSort = function(){
			$scope.predicate='relevance';
			$scope.reverse;
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
});

emailclient.controller('SearchController',function($scope, $location,$http, $modal,$sce, usSpinnerService){
	$scope.predicate = 'relevance';
	$scope.reverse=false;
	$scope.dateReverse=true;
	$scope.subjectReverse=true;
	$scope.dateReverse=true;
	$scope.isHide = true;
	$scope.DivShow=false;
	$scope.feedbackSuccess = false;
	console.log($scope.DivShow);

	$scope.databaseSize;
	$scope.mailFolderSize;
	$scope.isStatics = false;
	$scope.seeMailInfo = [];
	
	$scope.init = function(index) {
			$(".div"+index).hide();
			$(".numDiv"+index).show();
	};
	
	$scope.divShow = function (i,divShow, data) {
		$scope.DivShow = divShow;
		if($(".div"+i).is(':visible')) {
			$(".div"+i).hide();
			$(".numDiv"+i).show();
		} else {
			
			$(".div"+i).show();
			$(".numDiv"+i).hide();
			
		}
	};
	
	$scope.mailInfo = function (data) {
		console.log(data);
		$http.get('/getEmailInfo/'+data)
		.success(function(data, status, headers, config) {
			console.log(data);
			$scope.seeMailInfo = {
					channel_name:data.channel_name,
					history:data.history,
					last_renewed:data.last_renewed,
					media_kit_url:data.media_kit_url,
					username:data.username,
					password:data.password,
					subscriber:data.subscriber,
					publisher_url:data.publisher_url,
					notes:data.notes,
					publisher:data.publisher
					
				}
			modalInstance = $modal.open({
			      templateUrl: 'manage-edit-user.html',
			      scope : $scope
			    });
		});
		
	
	};
	$scope.saveMailInfo = function (col_name, record, channel_name) {
		$http.get('/saveEmailInfo/'+col_name+'/'+record+'/'+channel_name)
		.success(function(data, status, headers, config) {
			
		});
	};
	$scope.saveRenewedDate = function (col_name, record, channel_name) {
		if(record != null){
			$http.get('/saveRenewedDate/'+col_name+'/'+record+'/'+channel_name)
				.success(function(data, status, headers, config) {
			
			});
		}
	};
	/*  Below Section for modal  */
	/*if($location.path()=="/admin") {
		$scope.isadmin = true;
	}*/
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
	  $scope.getWordCloudById = function (id) {
		 $scope.wordCloudId = id;
		   //$scope.searchForm.popUpId = popUpId;
		  usSpinnerService.spin('loading...');
		  $http.get('/get-word-cloud-by-id/'+id)
			.success(function(data, status, headers, config) {
				
				//$('#wordcloudimage').attr('src','data:image/svg+xml,'+data);
				$scope.wc = 'data:image/svg+xml,'+data;
				usSpinnerService.stop('loading...');
				modalInstance = $modal.open({
				      templateUrl: '/assets/app/views/wordcloud.html',
				      scope : $scope
				    });
			});
		  
	  };
	  $scope.removeEmailData=function(id,index)
	  {
		  //alert("in remove "+id+" "+indexId);
		  if(confirm("Are you sure you want to delete this mail? Yes/No")) {
		  $http.get('/deletemailbyid/'+id)
			.success(function(data, status, headers, config){
				$scope.emails.splice(index,1);
				//alert("data "+data);
				//$scope.submitSearch();
				/*if(data)
				{
					//alert("in if");
					$scope.getBlackList();
				}*/
			});
		  }
	  };
	  $scope.downloadPDF =function(popUpId) {
		  $http.get('/downloadPdf/'+popUpId, {params:$scope.searchForm})
			.success(function(data, status, headers, config){
				$('.modal-headerPopUp').append(data.htmlToShowMailPopUp);
				usSpinnerService.stop('loading...');
			});
	  };
	  $scope.currentIndex = -1;
	  $scope.showPopUpModal=function (popUpId,index) {
		  $scope.currentIndex = index;
		  console.log("$scope.currentIndex :"+$scope.currentIndex);
		  $scope.searchForm.popUpId = popUpId;
		  $scope.openPopup();
		  
	  };
	  $scope.isFromShowTab = false;
	  $scope.isHiddenMail = false;
	  $scope.openPopup = function() {
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
	  
	  $scope.showPopUpModalShowTab = function(popUpId,status) {
		  $scope.isFromShowTab = true;
		  if($("#emailId"+popUpId).attr('class')=='tip-bottom') {
			  $scope.isHiddenMail = false;
		  } else {
			  $scope.isHiddenMail = true;
		  }
		  
		  $scope.searchForm.popUpId = popUpId;
		  $scope.openPopup();
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
		$scope.subjectReverse=reverse;
		$scope.reverse=reverse;
		$scope.submitSearch();
	};
	$scope.dateSort = function(reverse){
		$scope.predicate='sentDate';
		$scope.dateReverse=reverse;
		$scope.reverse=reverse;
		$scope.submitSearch();
	};
	$scope.domainSort = function(reverse){
		$scope.predicate='domain';
		$scope.domainReverse=reverse;
		$scope.reverse=reverse;
		$scope.submitSearch();
	};
	$scope.relevanceSort = function(){
		$scope.predicate='relevance';
		$scope.reverse;
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
	};
	var protocol = $location.protocol();
	var host = $location.host();
	var port = $location.port();
	$scope.hideEmail = function(id,index) {
		$http({method:'GET',url:protocol+'://'+host+':'+port+'/hideEmail/'+id}).success(function(data) {
			if($scope.isFromShowTab) {
				$scope.isHiddenMail = true;
				$("#emailId"+id).attr('class', 'tip-bottom mailHidden');
			} else {
				$scope.emails[index].isHidden = true;
			}
		});
	};
	
	$scope.showEmail = function(id,index) {
		$http({method:'GET',url:protocol+'://'+host+':'+port+'/showEmail/'+id}).success(function(data) {
			if($scope.isFromShowTab) {
				$("#emailId"+id).attr('class', 'tip-bottom');
				$scope.isHiddenMail = false;
			} else {
				$scope.emails[index].isHidden = false;
			}
		});
	};
	$scope.item = {};
	$scope.sendFeedback = function() {
		$http({method:'POST',url:'/feedback',data:$scope.item}).success(function(data) {
			$scope.item = {};
			$scope.feedbackSuccess = true;
			//$location.path("auth#/");
		});
	};
});
