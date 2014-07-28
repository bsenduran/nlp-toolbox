package org.wso2.toolbox.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SiddhiExtension(namespace = "nlp", function = "calculate_sentiment")
public class SentimentFunctionExecutor extends FunctionExecutor {

    private static Logger logger = Logger.getLogger(SentimentFunctionExecutor.class);

    private StanfordCoreNLP pipeline;

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {

        for (Attribute.Type attributeType : types) {
            if (attributeType != Attribute.Type.STRING) {
                throw new QueryCreationException("Can not calculate Sentiment for non-string value");
            }
        }

        Properties props = new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");

        pipeline = new StanfordCoreNLP(props);

    }

    @Override
    protected Object process(Object obj) {
        return calculateSentiment(String.valueOf(obj));
    }

    @Override
    public void destroy() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.INT;
    }

    private int calculateSentiment(String text) {

        //Remove symbols from the text
        String symbolRemoved = removeSymbols(text);
        //Remove url from the text
        String urlRemoved = removeUrl(symbolRemoved);

        //neutral
        int mainSentiment = 2;

        int longest = 0;
        Annotation annotation = pipeline.process(urlRemoved);
        for (CoreMap coreMap : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = coreMap.get(SentimentCoreAnnotations.AnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            String partText = coreMap.toString();
            if (partText.length() > longest) {
                mainSentiment = sentiment;
                longest = partText.length();
            }

        }
        return mainSentiment;
    }

    /**
     * Remove smiley and other character from the text.
     * Icons should remove before calculate the sentiment value.
     *
     * @param message
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    private String removeSymbols(String message) {

        String removedStr = "";
        String symbolPattern = "[^\\x00-\\x7F]";
        if (message != null) {
            String utf8Message = "";
            byte[] utf8Bytes = message.getBytes();
            try {
                utf8Message = new String(utf8Bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("Error while reading the message", e);
            }
            Pattern unicodeOutliers = Pattern.compile(symbolPattern, Pattern.UNICODE_CASE |
                    Pattern.CANON_EQ |
                    Pattern.CASE_INSENSITIVE);
            Matcher unicodeOutlierMatcher = unicodeOutliers.matcher(utf8Message);
            removedStr = unicodeOutlierMatcher.replaceAll("");
        }
        return removedStr;
    }

    /**
     * Remove any url attached to the text.
     * url should remove before the sentiment calculation
     *
     * @param message
     * @return
     */
    private String removeUrl(String message) {
        String removedStr = message;
        String urlPattern =
                "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(message);
        while (m.find()) {
            removedStr = message.replace(m.group(), "").trim();
        }
        return removedStr;
    }

}
