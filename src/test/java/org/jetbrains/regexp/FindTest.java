package org.jetbrains.regexp;

import org.jetbrains.utils.TestUtils;
import org.junit.Test;

import static org.jetbrains.utils.TestUtils.list;
import static org.jetbrains.utils.TestUtils.testMatches;

public class FindTest {

  @Test
  public void testFind() throws Exception {
    Pattern pattern = Pattern.compile("dog");
    Matcher matcher = pattern.matcher("my dog is the best dog");
    testMatches(matcher, 2);
  }

  @Test
  public void testGreedyFindWith() throws Exception {
    Pattern pattern = Pattern.compile(".*th.*");
    Matcher matcher = pattern.matcher("my dog is the best dog");
    testMatches(matcher, 1);
  }

  @Test
  public void testMatchAllSubStrings() throws Exception {
    Pattern pattern = Pattern.compile(".*the.*");
    Matcher matcher = pattern.matcher("my dog is the best dog");
    matcher.match(true);
    testMatches(matcher, 110);
  }

  @Test
  public void testFindAnotherSimplePattern() throws Exception {
    testMatches("a*", "ha", list(
      new TestUtils.SubstringFind(0, 0, ""),
      new TestUtils.SubstringFind(1, 2, "a"),
      new TestUtils.SubstringFind(2, 2, "")
    ));
  }

  @Test
  public void testFindSimplePattern() throws Exception {
    testMatches("a*", "sasha", list(
      new TestUtils.SubstringFind(0, 0, ""),
      new TestUtils.SubstringFind(1, 2, "a"),
      new TestUtils.SubstringFind(2, 2, ""),
      new TestUtils.SubstringFind(3, 3, ""),
      new TestUtils.SubstringFind(4, 5, "a"),
      new TestUtils.SubstringFind(5, 5, "")
    ));
  }

  
}
