const functions = require('firebase-functions');
const request = require('request-promise');

//Check below documentation for new version differences. Old version method "event" and "event.val()" was not working now.
//https://firebase.google.com/docs/functions/beta-v1-diff
//1. When a new onWrite event is triggered in RealTime database, this function copies the data to ElasticSearch.Process is called (I think) indexing.
//2. Before that, we created a similar index in ElasticSearch repository using put method using PostMan software.
//3. elasticSearchUrl is created when configuration is saved within FireBase Functions console.
//4. Note: Only new events are saved in ElasticSearch, because the above documentation had the method change.after.val() made more sense to me.

exports.indexPostsToElastic = functions.database.ref('/posts/{post_id}')
	.onWrite((change, context) => {
		
		console.log('onWrite Functions has started');
		
		//let postData = event.data.val(); //Before (<= v0.9.1) Cloud Functions = data after the write
		let postData = change.after.val(); //Now (>= v1.0.0) Cloud Functions = data after the write
		let post_id = context.params.post_id;
		
		console.log('Indexing post:', postData);
		
		let elasticSearchConfig = functions.config().elasticsearch;
		let elasticSearchUrl = elasticSearchConfig.url + 'posts/post/' + post_id;
		let elasticSearchMethod = postData ? 'POST' : 'DELETE';
		
		let elasticSearchRequest = {
			method: elasticSearchMethod,
			url: elasticSearchUrl,
			auth:{
				username: elasticSearchConfig.username,
				password: elasticSearchConfig.password,
			},
			body: postData,
			json: true
		  };
		  
		  return request(elasticSearchRequest).then(response => {
			 console.log("ElasticSearch response", response);
		  });
	});

	
	