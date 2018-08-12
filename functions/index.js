const functions = require('firebase-functions');
const request = require('request-promise');

//Check below documentation for new version differences.
//https://firebase.google.com/docs/functions/beta-v1-diff

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

	
	