package org.wso2.toolbox.nlp;

import edu.stanford.nlp.util.Triple;
import org.apache.log4j.Logger;
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
    private static Logger logger = Logger.getLogger(CountryFunctionExecutor.class);

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

    @Override
    public void destroy() {

    }

    @Override
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
