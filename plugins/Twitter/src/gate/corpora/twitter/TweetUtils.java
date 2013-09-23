/*
 *  TweetUtils.java
 *
 *  Copyright (c) 1995-2013, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  $Id$
 */
package gate.corpora.twitter;

import gate.*;

import java.io.IOException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;


// Jackson API
// http://wiki.fasterxml.com/JacksonHome

// Standard: RFC 4627
// https://tools.ietf.org/html/rfc4627

public class TweetUtils  {
  
  public static final String PATH_SEPARATOR = ":";
  public static final String MIME_TYPE = "text/x-json-twitter";

  public static List<Tweet> readTweets(String string) throws IOException {
    if (string.startsWith("[")) {
      return readTweetList(string, null, null);
    }
  
    // implied else
    return readTweetLines(string, null, null);
  }


  public static List<Tweet> readTweets(String string, List<String> contentKeys, List<String> featureKeys) throws IOException {
    if (string.startsWith("[")) {
      return readTweetList(string, contentKeys, featureKeys);
    }

    // implied else
    return readTweetLines(string, contentKeys, featureKeys);
  }
  
  
  public static List<Tweet>readTweetLines(String string, List<String> contentKeys, List<String> featureKeys) throws IOException {
    // just not null, so we can use it in the loop
    // What does that mean?
    String[] lines = string.split("[\\n\\r]+");
    return readTweetStrings(lines, contentKeys, featureKeys);
  }
  

  public static List<Tweet>readTweetStrings(String[] lines, List<String> contentKeys, List<String> featureKeys) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    
    for (String line : lines) {
      if (line.length() > 0) {
        JsonNode jnode = mapper.readTree(line);
        tweets.add(Tweet.readTweet(jnode, contentKeys, featureKeys));
      }
    }
    
    return tweets;
  }

  
  public static List<Tweet>readTweetStrings(List<String> lines, List<String> contentKeys, List<String> featureKeys) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    
    for (String line : lines) {
      if (line.length() > 0) {
        JsonNode jnode = mapper.readTree(line);
        tweets.add(Tweet.readTweet(jnode, contentKeys, featureKeys));
      }
    }
    
    return tweets;
  }

  
  public static List<Tweet> readTweetList(String string, List<String> contentKeys, List<String> featureKeys) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    ArrayNode jarray = (ArrayNode) mapper.readTree(string);
    for (JsonNode jnode : jarray) {
      tweets.add(Tweet.readTweet(jnode, contentKeys, featureKeys));
    }
    return tweets;
  }


  public static Object process(JsonNode node) {
    /* JSON types: number, string, boolean, array, object (dict/map),
     * null.  All map keys are strings.
     */

    if (node.isBoolean()) {
      return node.asBoolean();
    }
    if (node.isDouble()) {
      return node.asDouble();
    }
    if (node.isInt()) {
      return node.asInt();
    }
    if (node.isTextual()) {
      return node.asText();
    }
      
    if (node.isNull()) {
      return null;
    }
    
    if (node.isArray()) {
      List<Object> list = new ArrayList<Object>();
      for (JsonNode item : node) {
        list.add(process(item));
      }
      return list;
    }

    if (node.isObject()) {
      FeatureMap map = Factory.newFeatureMap();
      Iterator<String> keys = node.fieldNames();
      while (keys.hasNext()) {
        String key = keys.next();
        map.put(key, process(node.get(key)));
      }
      return map;
    }

    return node.toString();
  }

  

  public static FeatureMap process(JsonNode node, List<String> keepers) {
    FeatureMap found = Factory.newFeatureMap();
    for (String keeper : keepers) {
      String[] keySequence = StringUtils.split(keeper, PATH_SEPARATOR);
      Object value = dig(node, keySequence, 0);
      if (value != null) {
        found.put(keeper, value);
      }
    }
    return found;
  }
  
  
  /**
   * Dig through a JSON object, key-by-key (recursively).
   * @param node
   * @param keySequence
   * @return the value held by the last key in the sequence; this will
   * be a FeatureMap if there is further nesting
   */
  public static Object dig(JsonNode node, String[] keySequence, int index) {
    if ( (index >= keySequence.length) || (node == null) ) {
      return null;
    }
    
    if (node.has(keySequence[index])) {
      JsonNode value = node.get(keySequence[index]); 
      if (keySequence.length == (index + 1)) {
        // Found last key in sequence; convert the JsonNode
        // value to a normal object (possibly FeatureMap)
        return process(value);
      }
      else if (value instanceof JsonNode){
        // Found current key; keep digging for the rest
        return dig(value, keySequence, index + 1);
      }
    }
    
    return null;
  }

  

}
