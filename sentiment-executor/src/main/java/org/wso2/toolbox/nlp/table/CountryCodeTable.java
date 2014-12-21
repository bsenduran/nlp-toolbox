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

package org.wso2.toolbox.nlp.table;


import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public final class CountryCodeTable {

	private static CountryCodeTable self;
	private Map<String, String> countryCodeMap;
    private AbstractSequenceClassifier<CoreLabel> classifier;
    private List<String> countryCodeList;
    private static Logger logger = Logger.getLogger(CountryCodeTable.class);

	private CountryCodeTable() {
		self = this;
        try {
            classifier = CRFClassifier.getClassifier("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
            countryCodeList = new ArrayList<String>(Arrays.asList(Locale.getISOCountries()));
            countryCodeMap = CountryCodeLoader.loadCountries();

        } catch (ClassNotFoundException e) {
            logger.error("Error in loading classifier", e);
        } catch (IOException e) {
            logger.error("Error while loading the classifier model [english.all.3class.distsim.crf.ser.gz]", e);
        }
	}

	public static CountryCodeTable getSharedInstance() {
		if (self == null) {
            synchronized (CountryCodeTable.class) {
                if (self == null) {
                    self = new CountryCodeTable();
                }
            }
		}
		return self;
	}

	public Object getCountryCode(String key) {
		return countryCodeMap.get(key);
	}

    public List<String> getCountryCodeList(){
        return countryCodeList;
    }

    public AbstractSequenceClassifier<CoreLabel> getClassifier() {
        return classifier;
    }
}
