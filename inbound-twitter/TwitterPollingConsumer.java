/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * 
 * Project Name: com.wso2.stream.connector.protocol
 * Package Name: com.wso2.stream.connector.protocol
 * File Name: TwitterPollingConsumer.java
 * Author: daneshk
 * Created Date: Jul 5, 2014
 */

package org.apache.synapse.protocol.twitter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InjectHandler;
import org.apache.synapse.inbound.PollingConsumer;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TwitterPollingConsumer implements Runnable, PollingConsumer {

    private static final Log logger = LogFactory.getLog(TwitterPollingConsumer.class.getName());

    // twitter auth
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessSecret;

    private Queue<Status> twitterQueue;

    private InjectHandler injectHandler;
    private String[] filterTags;

    private  OMFactory omFactory;
    private TweetContent tweetContent;

    /**
     * @param twitterProperties
     */
    public TwitterPollingConsumer(Properties twitterProperties) {
        loadCredentials(twitterProperties);
        String tags = twitterProperties.getProperty(TwitterConstant.FILTER_TAG);

        this.filterTags = tags.split(";");

        twitterQueue = new LinkedList<Status>();
        omFactory = OMAbstractFactory.getOMFactory();
        tweetContent = new TweetContent();


        // Create a new BasicClient. By default gzip is enabled.

        logger.info("Creating client ............");
        setupConnection();
        logger.info("Client created successfully ............");
    }

    /**
     */
    private void loadCredentials(Properties properties) {
        this.consumerKey = properties.getProperty(TwitterConstant.CONSUMER_KEY);
        this.consumerSecret = properties.getProperty(TwitterConstant.CONSUMER_SECRET);
        this.accessSecret = properties.getProperty(TwitterConstant.ACCESS_SECRET);
        this.accessToken = properties.getProperty(TwitterConstant.ACCESS_TOKEN);
    }

    /**
     */
    public void registerHandler(TwitterInjectHandler twitterInjectHandler) {
        this.injectHandler = twitterInjectHandler;
    }

    private void setupConnection() {
        StatusListener listener = new StatusListenerImpl();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessSecret);

        TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        twitterStream.addListener(listener);

        FilterQuery query = new FilterQuery();
        query.language(new String[]{"en"});
        query.track(filterTags);
        twitterStream.filter(query);
    }


    public void run() {
        poll();
    }

    public void execute() {
        poll();
    }

    public Object poll() {
        System.out.println("################ calling poll ########################"+ System.currentTimeMillis());

        try {
            while (twitterQueue.size() > 0) {
                Status status = twitterQueue.poll();
                    OMElement bodyContent = tweetContent.createBodyContent(omFactory, status);
                    injectHandler.invoke(bodyContent);
            }

        } catch (Exception e) {
            logger.error("Error while receiving Twitter message. " + e.getMessage(), e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.synapse.inbound.PollingConsumer#registerHandler(org.apache
     * .synapse.inbound.InjectHandler)
     */
    public void registerHandler(InjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }


class StatusListenerImpl implements StatusListener {
    public void onStatus(Status status) {
        if (status.getRetweetedStatus() == null) {
            twitterQueue.add(status);
        }
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

    }

    public void onScrubGeo(long userId, long upToStatusId) {

    }

    public void onStallWarning(StallWarning warning) {

    }

    public void onException(Exception ex) {

    }
}

}
