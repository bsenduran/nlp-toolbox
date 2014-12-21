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

package org.wso2.toolbox.nlp;

import edu.stanford.nlp.util.Triple;
import org.wso2.toolbox.nlp.table.CountryCodeTable;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.util.List;

@SiddhiExtension(namespace = "nlp", function = "recognize_country")
public class CountryFunctionExecutor extends FunctionExecutor {

    private CountryCodeTable countryCodeTable;

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        for (Attribute.Type attributeType : types) {
            if (attributeType != Attribute.Type.STRING) {
                throw new QueryCreationException("Can not calculate Sentiment for non-string value");
            }
        }
        countryCodeTable = CountryCodeTable.getSharedInstance();

    }

    @Override
    protected Object process(Object obj) {
        return recognizeEntity(String.valueOf(obj));
    }

    public void destroy() {
    }

    public Attribute.Type getReturnType() {
        return Attribute.Type.STRING;
    }

    public String recognizeEntity(String locationStr) {


        String countryCode = null;

        if (locationStr != null) {

            String locationUpperStr = locationStr.toUpperCase();

            List<Triple<String, Integer, Integer>> items = countryCodeTable.getClassifier().classifyToCharacterOffsets(locationUpperStr);

            for (Triple<String, Integer, Integer> item : items) {
                if ("Location".equalsIgnoreCase(item.first())) {
                    String countryName = locationUpperStr.substring(item.second, item.third);

                    if (countryCodeTable.getCountryCodeList().contains(countryName)) {
                        countryCode = countryName;
                    } else {
                        countryCode = (String) countryCodeTable.getCountryCode(countryName);
                    }
                }
                if (countryCode != null) {
                    break;
                }
            }
        }


        if (countryCode != null) {
            return countryCode;
        } else {
            return "";
        }
    }
}
