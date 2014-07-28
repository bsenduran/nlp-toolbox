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
* Project Name: com.ner.location.table
* Package Name: com.ner.location.table
* File Name: LocationLoarder.java
* Author: daneshk
* Created Date: Jul 16, 2014
*/

package org.wso2.toolbox.nlp.table;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class CountryCodeLoader {
    private static Logger logger = Logger.getLogger(CountryCodeLoader.class);

    private static final String COUNTRIES_JSON = "countries.json";

    private CountryCodeLoader() {}

    public static Map<String, String> loadCountries() {
        Map<String, String> countryMap = new HashMap<String, String>();
        JSONParser parser = new JSONParser();

        try {
            InputStream inputStream = CountryCodeLoader.class.getClassLoader().getResourceAsStream(COUNTRIES_JSON);
            Object obj = null;
            obj = parser.parse(new InputStreamReader(inputStream));

            JSONArray jsonArray = (JSONArray) obj;

            Iterator<JSONObject> it = jsonArray.iterator();

            while (it.hasNext()) {
                JSONObject country = it.next();
                countryMap.put((String) country.get("name"), (String) country.get("code"));
            }

        } catch (ParseException e) {
            logger.error("Error while parsing file [countries.json]", e);
        }
        catch (IOException e) {
            logger.error("Error while reading file [countries.json]", e);
        }
        return countryMap;
    }
}
