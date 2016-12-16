(function(a){a.bingSearch=function(c){var f="https://api.datamarket.azure.com/Bing/Search/v1/Web";var e=a.extend({pageNumber:1,pageSize:10,debug:false,urlBase:f},c);if(!e.appKey&&e.urlBase===f){console.error("Bing Search: Missing app key which is required if you are not overriding urlBase")}if(!e.query){console.error("Bing Search: Missing query")}if(!(e.fail||e.beforeSearchResults||e.searchResultIterator||e.afterSearchResults)){console.error("Bing Search: Missing at least one result function")}if((e.appKey||e.urlBase!==f)&&e.query&&(e.fail||e.beforeSearchResults||e.searchResultIterator||e.afterSearchResults)){if(e.limitToSite){e.query+=" site:"+e.limitToSite}var b=e.urlBase+"?$format=json&$top="+parseInt(e.pageSize,10)+"&$Skip="+(parseInt(e.pageSize,10)*(e.pageNumber-1))+"&Query='"+e.query+"'";var d="Basic "+a.base64.encode(e.appKey+":"+e.appKey);if(e.debug){console.log("Bing Search: Using URL: "+b);console.log("Bing Search: Sending Authorization header: "+d)}a.support.cors=true;a.ajax({beforeSend:function(g){g.setRequestHeader("Authorization",d)},url:b,error:function(i,h,g){if(e.fail){e.fail(g.message)}else{console.error(g.message)}},dataType:"json",success:function(m){if(!m||!m.d||!m.d.results){var k="Bing Search: Data was empty or didn't contain results.";if(e.fail){e.fail(k)}else{console.error(k)}return}var j={hasMore:m.d.__next!==undefined,resultBatchCount:m.d.results.length};if(e.beforeSearchResults){e.beforeSearchResults(j)}if(e.searchResultIterator){for(var l=0;l<m.d.results.length;l++){var n=m.d.results[l];var h=null;if(n.__metadata&&n.__metadata.type){h={ResultType:n.__metadata.type}}var g={ID:n.ID,Title:n.Title,Description:n.Description,Url:n.Url,DisplayUrl:n.DisplayUrl,Metadata:h};if(e.debug){console.log("Bing Search: Result: \nID: "+(g.ID||"null")+"\nTitle: "+(g.Title||"null")+"\nDescription: "+(g.Description||"null")+"\nUrl: "+(g.Url||"null")+"\nDisplayUrl: "+(g.DisplayUrl||"null")+"\nMetadata.ResultType: "+(g.Metadata&&g.Metadata.ResultType?g.Metadata.ResultType:"null"))}e.searchResultIterator(g)}}if(e.afterSearchResults){e.afterSearchResults(j)}}})}return this}}(jQuery));