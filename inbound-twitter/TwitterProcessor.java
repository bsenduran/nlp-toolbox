/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
* Project Name: com.wso2.stream.connector.protocol
* Package Name: com.wso2.stream.connector.protocol
* File Name: TwitterProcessor.java
* Author: daneshk
* Created Date: Jul 5, 2014
*/

package org.apache.synapse.protocol.twitter;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.PollingProcessor;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.Task;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskStartupObserver;


/**
 * @author daneshk
 *
 */
public class TwitterProcessor implements PollingProcessor, TaskStartupObserver {

    private static final Log log = LogFactory.getLog(TwitterProcessor.class.getName());


    private TwitterPollingConsumer pollingConsumer;
    private String name;
    private Properties twitterProperties;
    private long interval;
    private String injectingSeq;
    private String onErrorSeq;
    private SynapseEnvironment synapseEnvironment;
    private StartUpController startUpController;
    
    public TwitterProcessor(String name, Properties twitterProperties, long pollInterval, String injectingSeq, String onErrorSeq, SynapseEnvironment synapseEnvironment) {
        this.name = name;
        this.twitterProperties = twitterProperties;
        this.interval = pollInterval;
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.synapseEnvironment = synapseEnvironment;
    }

	/* (non-Javadoc)
	 * @see org.apache.synapse.task.TaskStartupObserver#update()
	 */
    public void update() {
		start();
    }

	/* (non-Javadoc)
	 * @see org.apache.synapse.inbound.PollingProcessor#init()
	 */
    public void init() {
        log.info("Initializing inbound Twitter listener for destination " + name);
        pollingConsumer = new  TwitterPollingConsumer(twitterProperties);
        pollingConsumer.registerHandler(new TwitterInjectHandler(injectingSeq, onErrorSeq, synapseEnvironment));
        start();
    }

	/* (non-Javadoc)
	 * @see org.apache.synapse.inbound.PollingProcessor#start()
	 */
    public void start() {
    	log.info("Inbound Twitter listener Started for destination " + name);
        try {
        	Task task = new TwitterTask(pollingConsumer);
        	TaskDescription taskDescription = new TaskDescription();
        	taskDescription.setName(name + "-TWITTER-EP");
        	taskDescription.setTaskGroup("TWITTER-EP");
        	taskDescription.setInterval(interval);
        	taskDescription.setIntervalInMs(true);
        	taskDescription.addResource(TaskDescription.INSTANCE, task);
        	taskDescription.addResource(TaskDescription.CLASSNAME, task.getClass().getName());
        	startUpController = new StartUpController();
        	startUpController.setTaskDescription(taskDescription);
        	startUpController.init(synapseEnvironment);
        	
        } catch (Exception e) {
            log.error("Could not start Twitter Processor. Error starting up scheduler. Error: " + e.getLocalizedMessage());
        } 
    }

	/* (non-Javadoc)
	 * @see org.apache.synapse.inbound.PollingProcessor#destroy()
	 */
    public void destroy() {
        log.info("Inbound Twitter listener ending operation on destination " + name);
        startUpController.destroy();
    }  
}
