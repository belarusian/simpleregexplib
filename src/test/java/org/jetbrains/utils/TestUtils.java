package org.jetbrains.utils;

import org.apache.log4j.Logger;
import org.jetbrains.regexp.Matcher;
import org.jetbrains.regexp.Pattern;
import org.jetbrains.regexp.parser.RegexParser;
import org.jetbrains.regexp.parser.token.element.Token;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public final class TestUtils {

  private static Logger LOG = Logger.getLogger(TestUtils.class);
  
  public static void testMatches(String pattern, String text, SubstringFind[] expectations) throws Exception {
    Pattern compiledPattern = Pattern.compile(pattern);
    Matcher matcher = compiledPattern.matcher(text);
    List<SubstringFind> actualMatches = new LinkedList<SubstringFind>();
    while (matcher.find()) {
      SubstringFind substringFind = new SubstringFind(matcher.start(), matcher.end(), matcher.group());
      actualMatches.add(substringFind);
    }
    LOG.info("expected matches:" + actualMatches);
    assertArrayEquals(expectations, actualMatches.toArray());
  }

  public static void testMatches(Matcher matcher, Integer numberOfMatches) throws Exception {
    Integer count = 0;
    while (matcher.find()) count++;
    assertEquals(count, numberOfMatches);
  }

  public static <T> T[] list(T... items) {
    return items;
  }

  public static void testPattern(String pattern, String[] matches, String[] notMatches) throws ParseException {
    Pattern compiledPattern = Pattern.compile(pattern);
    for (String text : matches) {
      assertTrue("should have matched on " + text, compiledPattern.matches(text));
    }
    for (String text : notMatches) {
      assertFalse("should not have matched on " + text, compiledPattern.matches(text));
    }
  }

  public static void testParser(String pattern, Token[] expectedTokens) throws ParseException {
    assertArrayEquals(expectedTokens, new RegexParser(pattern).getTokens());
  }

  /**
  * @author sasha.malahov@here.com (Sasha Malahov)
  */
  public static final class SubstringFind {
    final int start;
    final int end;
    final String match;
  
    public SubstringFind(int start, int end, String match) {
      this.start = start;
      this.end = end;
      this.match = match;
    }
  
    @Override
    public String toString() {
      return "SubstringMatch{" +
          "start=" + start +
          ", end=" + end +
          ", match='" + match + '\'' +
          '}';
    }
  
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
  
      SubstringFind that = (SubstringFind) o;
  
      if (end != that.end) return false;
      if (start != that.start) return false;
      return match.equals(that.match);
    }
  
  }
}
