/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.stream.connector.protocol;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2.stream.connector.protocol.TweetContent;
import com.wso2.stream.connector.protocol.TwitterConstant;
import com.wso2.stream.connector.protocol.TwitterInjectHandler;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterPollingConsumer {

	private static final Log logger = LogFactory
			.getLog(TwitterPollingConsumer.class.getName());

	// twitter auth
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessSecret;

	private Queue<Status> twitterQueue;

	private TwitterInjectHandler injectHandler;
	private String[] filterTags;

	private OMFactory omFactory;
	private TweetContent tweetContent;

	/**
	 * TwitterPollingConsumer constructor
	 * This is call when initializing the polling processor
	 * @param twitterProperties
	 */
	public TwitterPollingConsumer(Properties twitterProperties) {
		loadCredentials(twitterProperties);
		String tags = twitterProperties.getProperty(TwitterConstant.FILTER_TAG);

		this.filterTags = tags.split(";");

		twitterQueue = new LinkedList<Status>();
		omFactory = OMAbstractFactory.getOMFactory();
		tweetContent = new TweetContent();

		//Establishing connection with twitter streaming api
		setupConnection();
		logger.info("Twitter connection setup successfully.");
	}

	/**
	 * Load credentials from the Twitter end-point property file.
	 * @param properties
	 */
	private void loadCredentials(Properties properties) {
		this.consumerKey = properties.getProperty(TwitterConstant.CONSUMER_KEY);
		this.consumerSecret = properties
				.getProperty(TwitterConstant.CONSUMER_SECRET);
		this.accessSecret = properties
				.getProperty(TwitterConstant.ACCESS_SECRET);
		this.accessToken = properties.getProperty(TwitterConstant.ACCESS_TOKEN);
	}

	public void registerHandler(TwitterInjectHandler twitterInjectHandler) {
		this.injectHandler = twitterInjectHandler;
	}

	/**
	 * Setting up a connection with Twitter Stream API with the given credentials
	 */
	private void setupConnection() {
		StatusListener listener = new StatusListenerImpl();

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setDebugEnabled(true)
				.setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessSecret);

		TwitterStream twitterStream = new TwitterStreamFactory(
				configurationBuilder.build()).getInstance();
		twitterStream.addListener(listener);

		FilterQuery query = new FilterQuery();
		query.language(new String[] { "en" });
		query.track(filterTags);
		twitterStream.filter(query);
	}

	public void run() {
		poll();
	}

	public void execute() {
		poll();
	}

	/**
	 * This is invoked each time when Inbound-endpoint triggers
	 * @return
	 */
	public Object poll() {
		try {
			while (twitterQueue.size() > 0) {
				Status status = twitterQueue.poll();
				OMElement bodyContent = tweetContent.createBodyContent(
						omFactory, status);
				injectHandler.invoke(bodyContent);
			}

		} catch (Exception e) {
			logger.error(
					"Error while receiving Twitter message. " + e.getMessage(),
					e);
		}
		return null;
	}

	/**
	 * Twitter Stream Listener Impl
	 * onStatus will invoke whenever new twitter come. 
	 * New Twitter is store in queue until it is inbound-endpoint poll
	 *
	 */
	class StatusListenerImpl implements StatusListener {
		public void onStatus(Status status) {
			if (status.getRetweetedStatus() == null) {
				twitterQueue.add(status);
			}
		}

		public void onException(Exception ex) {
			logger.error("Twitter source threw an exception", ex);
		}

		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			logger.debug("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
		}

		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			logger.debug("Got track limitation notice: " +numberOfLimitedStatuses);
		}

		public void onScrubGeo(long userId, long upToStatusId) {
			logger.debug("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
		}

		public void onStallWarning(StallWarning warning) {
			logger.debug("Got stall warning:" + warning);
		}

	}

}
