<?php
/**
 * @Author: prabhakar
 * @Date:   2016-03-18 23:13:45
 * @Last Modified by:   Prabhakar Gupta
 * @Last Modified time: 2016-10-19 19:12:11
 */

require_once '../../inc/connection.inc.php';
require_once '../../inc/constants.inc.php';
require_once '../../inc/function.inc.php';
require_once 'lib/twitteroauth.php';

$city_id = @(int)$_GET['city'];

if($city_id > 0){
	$query = "SELECT `WOEID` FROM `cities` WHERE `id`='$city_id' LIMIT 1";
	$woeid_city = mysqli_fetch_assoc(mysqli_query($connection, $query));
	if(isset($woeid_city)){
		$woeid_city = $woeid_city['WOEID'];

		$twitter_connection = new TwitterOAuth(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, TWITTER_OAUTH_TOKEN, TWITTER_OAUTH_TOKEN_SECRET);
		$response = $twitter_connection->get("https://api.twitter.com/1.1/trends/place.json?id=" . $woeid_city);

		if(isset($response->errors)){
			$response = array(
				'error'	=> true,
				'message' => $response->errors[0]->message,
			);
		} else {
			$response = $response[0]->trends;
		}
		echo json_encode($response);
	}
}
