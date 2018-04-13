package org.jetbrains.regexp;

import org.junit.Test;


import static org.jetbrains.utils.TestUtils.list;
import static org.jetbrains.utils.TestUtils.testPattern;

public class MatchesTest {

  @Test
  public void testMatchesStarOnCharacterInGroup() throws Exception {
    testPattern("(A*B|AC)D", list("AAAABD","BD","ACD"), list("AAAAC","D"));
  }

  @Test
  public void testMatchesStarOnGroup() throws Exception {
    testPattern("(a|(bc)*d)*", list("abcbcd", "abcbcbcdaaaabcbcdaaaddd"), list("b"));
  }

  @Test
  public void testMatchesOnCharacterAndGroup() throws Exception {
    testPattern("ab*|(bc)*", list("", "ab", "bcbc", "abbbbbb"), list("ac"));
  }

  @Test
  public void testMatchesGroups() throws Exception {
    testPattern("(A|B)(C|D)", list("AC", "AD", "BC", "BD"), list("CD", ""));
  }

  @Test
  public void testMatchesOnStartsEverywhere() throws Exception {
    testPattern("A*|(A*BA*BA*)*", list("AAA"), list("B"));
  }

  @Test
  public void testMatchesMultiwayOr() throws Exception {
    testPattern("A|B|(CD*)", list("A", "B", "CDDD"), list("D"));
  }

  @Test
  public void testMatchesCharacterSet() throws Exception {
    testPattern("[AB]", list("A", "B"), list("C"));
  }

  @Test
  public void testMatchesCharacterRange() throws Exception {
    testPattern("[A-C]B", list("AB", "BB"), list("C"));
  }

  @Test
  public void testMatchesLongerCharacterRange() throws Exception {
    testPattern("[a-zA-Z]", list("z", "Z"), list("+"));
  }

  @Test
  public void testMatchesWithEscapeSequence() throws Exception {
    testPattern("\\+[a-zA-Z]", list("+z", "+Z"), list("+", "a"));  
  }

  @Test
  public void testMatchesCharacterUnion1() throws Exception {
    testPattern("[a-dm-p][a-dm-p]", list("am", "ap"), list("a", "p"));
  }

  @Test
  public void testMatchesCharacterUnion2() throws Exception {
    testPattern("[a-d[m-p]][a-d[m-p]]", list("am", "ap"), list("a", "p"));
  }

  @Test
  public void testCharacterRangeIntersection() throws Exception {
    testPattern("[a-z&&[ab]]", list("a", "b"), list("c", "z"));
  }

  @Test
  public void testCharacterRangeNotIntersection() throws Exception {
    testPattern("[a-z&&[^bc]]", list("a", "z"), list("b", "c"));
  }

  @Test
  public void testIntersectionNotRanges() throws Exception {
    testPattern("[a-z&&[^m-p]]", list("a", "z"), list("m", "n", "p"));
  }

  @Test
  public void testNumberRangesIntersection() throws Exception {
    testPattern("[0-9&&[45]]", list("4", "5"), list("8", "9", "0"));
  }

  @Test
  public void testMatchesCharacterCount() throws Exception {
    testPattern("a{2}", list("aa"), list("a"));
  }

  @Test
  public void testMatchesGroupCount() throws Exception {
    testPattern("(ab){2}", list("abab"), list("ab"));
  }

  @Test
  public void testMatchesCharacterMinClosure() throws Exception {
    testPattern("a{2,}", list("aa","aaa","aaaa"), list("a"));  
  }

  @Test
  public void testMatchesGroupMinClosure() throws Exception {
    testPattern("(aa){2,}", list("aaaa","aaaaaa"), list("a","aa","aaa"));
  }

  @Test
  public void testMatchesCharacterMinMaxClosure() throws Exception {
    testPattern("a{2,3}", list("aa","aaa"), list("aaaa","a"));
  }

  @Test
  public void testMatchesGroupMinMaxClosure() throws Exception {
    testPattern("(ab){2,3}", list("abab","ababab"), list("ab","abababab"));
  }
  
}
