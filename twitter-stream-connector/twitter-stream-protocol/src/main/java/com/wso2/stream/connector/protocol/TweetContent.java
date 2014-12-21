/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import javax.xml.namespace.QName;

public class TweetContent {

    private QName qTweet;
    private QName qText;
    private QName qCreatedAt;
    private QName qLatitude;
    private QName qLongitude;
    private QName qCountry;
    private QName qCountryCode;
    private QName qLocation;
    private QName qHasTags;

    public TweetContent() {
        this.qTweet = new QName("tweet");
        this.qText = new QName("text");
        this.qCreatedAt=new QName("createdAt");
        this.qLatitude = new QName("latitude");
        this.qLongitude = new QName("longitude");
        this.qCountry = new QName("country");
        this.qCountryCode = new QName("countryCode");
        this.qLocation = new QName("location");
        this.qHasTags = new QName("hashTags");

    }

    public OMElement createBodyContent(OMFactory omFactory, Status status) {
        OMElement tweet = omFactory.createOMElement(qTweet);

        OMElement text = omFactory.createOMElement(qText);
        tweet.addChild(text);
        text.addChild(omFactory.createOMText(status.getText()));


        OMElement createdAt = omFactory.createOMElement(qCreatedAt);
        tweet.addChild(createdAt);
        createdAt.addChild(omFactory.createOMText(status.getCreatedAt().toString()));

        OMElement latitude = omFactory.createOMElement(qLatitude);
        tweet.addChild(latitude);
        OMElement longitude = omFactory.createOMElement(qLongitude);
        tweet.addChild(longitude);
        if (status.getGeoLocation() != null) {
            latitude.addChild(omFactory.createOMText(String.valueOf(status.getGeoLocation().getLatitude())));
            longitude.addChild(omFactory.createOMText(String.valueOf(status.getGeoLocation().getLongitude())));
        }

        OMElement country = omFactory.createOMElement(qCountry);
        tweet.addChild(country);
        OMElement countryCode = omFactory.createOMElement(qCountryCode);
        tweet.addChild(countryCode);

        if (status.getPlace() != null) {
            country.addChild(omFactory.createOMText(status.getPlace().getCountry()));
            countryCode.addChild(omFactory.createOMText(status.getPlace().getCountryCode()));
        }

        OMElement location = omFactory.createOMElement(qLocation);
        tweet.addChild(location);
        if (status.getUser() != null) {
            location.addChild(omFactory.createOMText(status.getUser().getLocation()));
        }

        OMElement hashTags = omFactory.createOMElement(qHasTags);
        tweet.addChild(hashTags);

        if (status.getHashtagEntities().length > 0) {
            String tags = "";
            for (HashtagEntity h : status.getHashtagEntities()) {
                tags += h.getText() + ";";
            }
            tags = tags.substring(0, tags.length() - 1);
            hashTags.addChild(omFactory.createOMText(tags));
        }

        return tweet;

    }

}
