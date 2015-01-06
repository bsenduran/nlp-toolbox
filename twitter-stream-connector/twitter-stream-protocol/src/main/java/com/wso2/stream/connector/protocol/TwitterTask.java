/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*/

package com.wso2.stream.connector.protocol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.task.Task;

public class TwitterTask implements Task, ManagedLifecycle {
    private static final Log logger = LogFactory.getLog(TwitterTask.class.getName());

    private TwitterPollingConsumer twitterPollingConsumer;
    
    public TwitterTask(TwitterPollingConsumer twitterPollingConsumer) {
    	this.twitterPollingConsumer = twitterPollingConsumer;
    }

    public void execute() {
    	logger.debug("Execute twitter polling task");
    	twitterPollingConsumer.execute();
    }


    public void init(SynapseEnvironment synapseEnvironment) {
    	logger.debug("Initialize the twitter polling task");
    }

    public void destroy() {
    	logger.debug("Destroy the twitter polling task");
    }
}
