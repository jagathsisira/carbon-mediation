/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.sink;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.nio.charset.Charset;

public class EventSink {
	private static final Log log = LogFactory.getLog(EventSink.class);
	private String name;
	private String receiverUrlSet;
	private String authenticationUrlSet;
	private String username;
	private String password;
	private DataPublisher dataPublisher;

	public EventSink(){};

	public EventSink(String name, String username,String password,String receiverUrlSet,String authenticationUrlSet){
		this.setName(name);
		this.setUsername(username);
		this.setPassword(password);
		this.setReceiverUrlSet(receiverUrlSet);
		this.setAuthenticationUrlSet(authenticationUrlSet);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getReceiverUrlSet() {
		return receiverUrlSet;
	}

	public void setReceiverUrlSet(String urlSet) {
		this.receiverUrlSet = urlSet;
	}

	public String getAuthenticationUrlSet() {
		return authenticationUrlSet;
	}

	public void setAuthenticationUrlSet(String urlSet) {
		this.authenticationUrlSet = urlSet;
	}

	public DataPublisher getDataPublisher() {
		return dataPublisher;
	}

	public void setDataPublisher(DataPublisher loadBalancingDataPublisher) {
		this.dataPublisher = loadBalancingDataPublisher;
	}

	/**
	 * Generates an event sink from XML configuration element
	 *
	 * @param eventSinkElement XML configuration element of event sink
	 * @param name             Name to be set for the created event sink
	 * @return Generated event sink
	 * @throws EventSinkException
	 */
	public static EventSink createEventSink(OMElement eventSinkElement, String name) throws EventSinkException {

		EventSink eventSink = new EventSink();

		OMElement receiverUrl = eventSinkElement.getFirstChildWithName(EventSinkConstants.RECEIVER_URL_Q);
		if (receiverUrl == null || "".equals(receiverUrl.getText())) {
			throw new EventSinkException(
					EventSinkConstants.RECEIVER_URL_Q.getLocalPart() + " is missing in thrift endpoint config");
		}
		eventSink.setReceiverUrlSet(receiverUrl.getText());

		OMElement authenticatorUrl = eventSinkElement.getFirstChildWithName(EventSinkConstants.AUTHENTICATOR_URL_Q);
		if (authenticatorUrl != null) {
			eventSink.setAuthenticationUrlSet(authenticatorUrl.getText());
		}

		OMElement userName = eventSinkElement.getFirstChildWithName(EventSinkConstants.USERNAME_Q);
		if (userName == null || "".equals(userName.getText())) {
			throw new EventSinkException(
					EventSinkConstants.USERNAME_Q.getLocalPart() + " is missing in thrift endpoint config");
		}
		eventSink.setUsername(userName.getText());

		OMElement password = eventSinkElement.getFirstChildWithName(EventSinkConstants.PASSWORD_Q);
		if (password == null || "".equals(password.getText())) {
			throw new EventSinkException(
					EventSinkConstants.PASSWORD_Q.getLocalPart() + " attribute missing in thrift endpoint config");
		}

		try {
			eventSink.setPassword(
					new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(password.getText()),
					           Charset.forName("UTF-8")));
		} catch (CryptoException e) {
			throw new EventSinkException("Failed to decrypt password");
		}
		eventSink.setName(name);

//		ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
//		List<String> receiverUrlGroups = DataPublisherUtil.getReceiverGroups(eventSink.getReceiverUrlSet());
//
//		ArrayList<String> authenticatorUrlGroups = null;
//		if (eventSink.getAuthenticationUrlSet() != null && eventSink.getAuthenticationUrlSet().length() > 0) {
//			authenticatorUrlGroups = DataPublisherUtil.getReceiverGroups(eventSink.getAuthenticationUrlSet());
//			if (authenticatorUrlGroups.size() != receiverUrlGroups.size()) {
//				throw new EventSinkException("Receiver URL group count is not equal to Authenticator URL group count." +
//				                             " Receiver URL groups: " + eventSink.getReceiverUrlSet() +
//				                             " & Authenticator URL " +
//				                             " groups: " + eventSink.getAuthenticationUrlSet());
//			}
//		}
//
//		for (int i = 0; i < receiverUrlGroups.size(); ++i) {
//			String receiverGroup = receiverUrlGroups.get(i);
//			String[] receiverUrls = receiverGroup.split(",");
//			String[] authenticatorUrls = new String[receiverUrls.length];
//
//			if (authenticatorUrlGroups != null) {
//				String authenticatorGroup = authenticatorUrlGroups.get(i);
//				authenticatorUrls = authenticatorGroup.split(",");
//				if (receiverUrls.length != authenticatorUrls.length) {
//					throw new EventSinkException("Receiver URL count is not equal to Authenticator URL count. Receiver"
//					                             + " URL group: " + receiverGroup + ", authenticator URL group: " +
//					                             authenticatorGroup);
//				}
//			}
//
//			ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
//			for (int j = 0; j < receiverUrls.length; ++j) {
//				DataPublisherHolder holder = new DataPublisherHolder(authenticatorUrls[j], receiverUrls[j],
//				                                                     eventSink.getUsername(), eventSink.getPassword());
//				dataPublisherHolders.add(holder);
//			}
//			ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
//			allReceiverGroups.add(group);
//		}

        try {
            eventSink.setDataPublisher(new DataPublisher(eventSink.getReceiverUrlSet(), eventSink.getUsername(),
                                                         eventSink.getPassword()));
        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException
                | DataEndpointAuthenticationException | TransportException e) {
            throw new EventSinkException("Error creating DataPublisher for the event sink ", e);
        }

        return eventSink;
	}

	/**
	 * Stop DataPublisher before removing the Event Sink
     *
     * @param eventSink name of the Event Sink which is going to be removed
     */
    public static void stopDataPublisher(EventSink eventSink) throws EventSinkException {
        if (eventSink != null && eventSink.getDataPublisher() != null) {
            try {
                eventSink.getDataPublisher().shutdownWithAgent();
            } catch (DataEndpointException e) {
                throw new EventSinkException("Error shutting down the publisher for the event sink ", e);
            }
        }
    }
}