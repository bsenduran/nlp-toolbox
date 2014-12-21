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
* File Name: TwitterInjectHandler.java
* Author: daneshk
* Created Date: Jul 6, 2014
*/

package org.apache.synapse.protocol.twitter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InjectHandler;
import org.apache.synapse.mediators.base.SequenceMediator;

public class TwitterInjectHandler implements InjectHandler {


    private static final Log log = LogFactory.getLog(TwitterInjectHandler.class);

    private String injectingSeq;
    private String onErrorSeq;
    private SynapseEnvironment synapseEnvironment;

    /**
     * @param injectingSeq
     * @param onErrorSeq
     * @param synapseEnvironment
     */
    public TwitterInjectHandler(String injectingSeq, String onErrorSeq,
                                SynapseEnvironment synapseEnvironment) {
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.synapseEnvironment = synapseEnvironment;
    }

    /* (non-Javadoc)
     * @see org.apache.synapse.inbound.InjectHandler#invoke(java.lang.Object)
     */
    public boolean invoke(Object object) {
        OMElement element = (OMElement) object;
        try {
            org.apache.synapse.MessageContext msgCtx = createMessageContext();
            SOAPEnvelope soapEnvelope = TransportUtils.createSOAPEnvelope(element);

            msgCtx.setEnvelope(soapEnvelope);

            if (injectingSeq == null || injectingSeq.equals("")) {
                log.error("Sequence name not specified. Sequence : " + injectingSeq);
                return false;
            }
            SequenceMediator seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(injectingSeq);
            seq.setErrorHandler(onErrorSeq);
            log.debug("injecting message to sequence : " + injectingSeq);

            synapseEnvironment.injectAsync(msgCtx, seq);


        } catch (Exception e) {
            log.error("Error while processing the Twitter Message", e);
        }
        return true;
    }

    /**
     * Create the initial message context for the file
     */
    private org.apache.synapse.MessageContext createMessageContext() {
        org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
        // There is a discrepency in what I thought, Axis2 spawns a nes threads to
        // send a message is this is TRUE - and I want it to be the other way
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
        return msgCtx;
    }
}
