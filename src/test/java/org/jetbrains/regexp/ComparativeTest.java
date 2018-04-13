package org.jetbrains.regexp;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author sasha.malahov@here.com (Sasha Malahov)
 * 
 */
@RunWith(Parameterized.class)
public class ComparativeTest {
  
  private static Logger LOG = Logger.getLogger(ComparativeTest.class);
  private final String pattern;
  private final String text;
  private Matcher javaMatcher;
  private org.jetbrains.regexp.Matcher myMatcher;

  public ComparativeTest(String pattern,
                         String text) throws ParseException {
    this.pattern = pattern;
    this.text = text;
    Pattern javaPattern = Pattern.compile(pattern);
    org.jetbrains.regexp.Pattern myPattern = org.jetbrains.regexp.Pattern.compile(pattern);
    javaMatcher = javaPattern.matcher(text);
    myMatcher = myPattern.matcher(text);
    LOG.info("testing: " + pattern + ", " + text);
  }

  @Parameterized.Parameters
  public static Collection patternAndText() {
    return Arrays.asList(new Object[][]{
        {"a{3}", "aa"},
        {"a{3}", "aaa"},
        {"a{3}", "aaaa"},
        {"a{3}", "aaaaaaaaa"},
        {"a{3,}", "aaaaaaaaa"},
        {"a{3,6}", "aaaaaaaaa"},
        {"(dog){3}", "dogdogdogdogdogdog"},
        {"dog{3}", "dogdogdogdogdogdog"},
        {"abc{3}",   "abccabaaaccbbbc"},
        {".*foo", "xfooxxxxxxfoo"},
        {"sa*", "sasha"},
        {"sa+", "sasha"},
        {"a?", ""},
        {"a+", ""},
        {"a?", "a"},
        {"a+", "a"},
        {"a?", "aaaaa"},
        {"a+", "aaaaa"},
        {"a?",  "ababaaaab"},
        {"a+",   "ababaaaab"},
        {"a.+b?a?", "ababaaaab"},
        {"[abc]{3}",  "abccabaaaccbbbc"},
        {"[bcr]at", "bat"},
        {"[bcr]at", "cat"},
        {"[bcr]at", "hat"},
        {"[^bcr]at", "bat"},
        {"[a-c]", "a"},
        {"foo[1-5]", "foo1"},
        {"foo[^1-5]", "foo1"},
        {"foo[^1-5]", "foo6"},
        {"[0-4[6-8]]", "0"},
        {"[0-4[6-8]]", "5"},
        {"[0-4[6-8]]", "6"},
        {"[0-4[6-8]]", "8"},
        {"[0-4[6-8]]", "9"},
        {"[0-9&&[345]]", "3"},
        {"[0-9&&[345]]", "4"},
        {"[0-9&&[345]]", "5"},
        {"[0-9&&[345]]", "2"},
        {"[0-9&&[345]]", "6"},
        {"[2-8&&[4-6]]", "3"},
        {"[2-8&&[4-6]]", "4"},
        {"[2-8&&[4-6]]", "5"},
        {"[2-8&&[4-6]]", "6"},
        {"[2-8&&[4-6]]", "7"},
        {"[0-9&&[^345]]", "2"},
        {"[0-9&&[^345]]", "3"},
        {"[0-9&&[^345]]", "4"},
        {"[0-9&&[^345]]", "5"},
        {"[0-9&&[^345]]", "6"},
        {"[0-9&&[^345]]", "9"},
        {"[a-zA-Z_0-9]", "9"},
        {"[a-zA-Z_0-9]", "A"},
        {"[a-zA-Z_0-9]", "z"},
        {"[a-zA-Z_0-9]", "_"}
    });
  }

  @Test
  public void compareMatchAndFind() throws Exception {
    assertEquals(getMessage(), javaMatcher.matches(), myMatcher.matches());
  }
  
  @Test
  public void compareTheFind() throws Exception {
    boolean found = false;
    while (javaMatcher.find()) {
      assertTrue(getMessage(), myMatcher.find());
      assertEquals(getMessage(), javaMatcher.group(), myMatcher.group());
      assertEquals(getMessage(), javaMatcher.start(), myMatcher.start());
      assertEquals(getMessage(), javaMatcher.end(), myMatcher.end());
      found = true;
    }
    if(!found) {
      assertFalse(getMessage(), myMatcher.find());
    }
  }

  private String getMessage() {
    return "found problem in pattern:" + pattern + " text:" + text;
  }
 
}
