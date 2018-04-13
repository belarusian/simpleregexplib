package org.jetbrains.regexp.parser;

import org.jetbrains.regexp.parser.token.element.CharacterToken;
import org.jetbrains.regexp.parser.token.element.Token;
import org.jetbrains.regexp.parser.token.group.MatchingPair;
import org.jetbrains.regexp.parser.token.group.MatchingRange;
import org.jetbrains.regexp.parser.token.group.MatchingSet;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.jetbrains.utils.TestUtils.list;
import static org.jetbrains.utils.TestUtils.testParser;

public class RegexParserTest {

  @Test
  public void testParseBasic() throws Exception {
    testParser("ABCD", list(
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('A'), 
        new CharacterToken('B'),
        new CharacterToken('C'),
        new CharacterToken('D'),
        LanguageSymbols.CLOSE_PAREN)
    );
  }
  
  @Test
  public void testParseWrappingOuterExpression() throws Exception {
    Token[] expectedTokenList = list(
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('A'),
        LanguageSymbols.CLOSE_PAREN
    );
    testParser("(A)", expectedTokenList);
    testParser("A", expectedTokenList);
  }
  
  @Test
  public void testParseSet() throws Exception {
    Token[] list = list(
        new CharacterToken('A'),
        new CharacterToken('B'),
        new CharacterToken('C'),
        new CharacterToken('D'));
    List<Token> characterTokens = Arrays.asList(list);
    testParser("[ABCD]", list(
        LanguageSymbols.OPEN_PAREN,
        new MatchingSet(characterTokens, false),
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test
  public void testParseMultiSet() throws Exception {
    MatchingSet cdSet = getTupleSet('C', 'D');
    Token[] combinedList = list(
        new CharacterToken('A'),
        new CharacterToken('B'),
        cdSet
    );
    testParser("[AB[CD]]", list(
        LanguageSymbols.OPEN_PAREN,
        new MatchingSet(Arrays.asList(combinedList), false),
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test
  public void testParseRangeMultiSet() throws Exception {
    Token range = new MatchingRange(new CharacterToken('A'), new CharacterToken('B'));
    Token cdSet = getTupleSet('C', 'D');
    testParser("[A-B[CD]]", list(
        LanguageSymbols.OPEN_PAREN,
        new MatchingSet(Arrays.asList(range, cdSet), false),
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test
  public void testParseNotSet() throws Exception {
    Token[] list = list(
        new CharacterToken('a'),
        new CharacterToken('b'),
        new CharacterToken('c'));
    testParser("[^abc]", list(
        LanguageSymbols.OPEN_PAREN,
        new MatchingSet(Arrays.asList(list), true),
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test
  public void testParseNotRange() throws Exception {
    Token acRange = new MatchingRange(new CharacterToken('a'), new CharacterToken('k'));
    Token mzRange = new MatchingRange(new CharacterToken('m'), new CharacterToken('z'));
    testParser("[^a-k]", list(
        LanguageSymbols.OPEN_PAREN,
        new MatchingSet(Arrays.asList(acRange), true),
        LanguageSymbols.CLOSE_PAREN
    ));
    testParser("[^a-km-z]", list(
        LanguageSymbols.OPEN_PAREN,
        new MatchingSet(Arrays.asList(acRange, mzRange), true),
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test
  public void testParseRangeAndIntersection() throws Exception {
    Token azRange = new MatchingRange(new CharacterToken('a'), new CharacterToken('z'));
    MatchingSet abSet = getTupleSet('a', 'b');
    Token pair = new MatchingPair(azRange, abSet);
    testParser("[a-z&&[ab]]", list(
        LanguageSymbols.OPEN_PAREN,
        new MatchingSet(Arrays.asList(pair), false),
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test(expected = ParseException.class)
  public void testInvalidCharacterSet() throws Exception {
    new RegexParser("\\[ABCD]").getTokens();
  }

  @Test(expected = ParseException.class)
  public void testInvalidCharacterSet2() throws Exception {
    new RegexParser("[ABCD\\]").getTokens();
  }

  @Test(expected = ParseException.class)
  public void testInvalidCharacterSet3() throws Exception {
    new RegexParser("[ABCD").getTokens();
  }

  @Test
  public void testParseCharacterClosureCount() throws Exception {
    testParser("a{2}", list(
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'),
        new CharacterToken('a'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test
  public void testParseGroupClosureCount() throws Exception {
    testParser("(ab){2}", list(
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN
    ));  

  }

  @Test
  public void testParseOpenCharacterClosure() throws Exception {
    testParser("a{2,}", list(
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'),
        new CharacterToken('a'),
        new CharacterToken('a'),
        LanguageSymbols.STAR,
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN
    ));   
  }

  @Test
  public void testParseOpenGroupClosure() throws Exception {
    testParser("(ab){2,}", list(
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.STAR,
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN
    ));
  }

  @Test
  public void testParseRangedCharacterClosure() throws Exception {
    testParser("a{1,2}", list(
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'),
        LanguageSymbols.OR,
        new CharacterToken('a'),
        new CharacterToken('a'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN));
  }

  @Test
  public void testParseRangedGroupClosure() throws Exception {
    testParser("(ab){1,2}", list(
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.OR,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.OPEN_PAREN,
        new CharacterToken('a'), new CharacterToken('b'),
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN,
        LanguageSymbols.CLOSE_PAREN));
  }

  @Test(expected = ParseException.class)
  public void testParseSimpleClosedClosure() throws Exception {
    new RegexParser("a{,5}").getTokens();  
  }

  private MatchingSet getTupleSet(char first, char second) {
    Token[] arrayList = list(
        new CharacterToken(first),
        new CharacterToken(second));
    return new MatchingSet(Arrays.asList(arrayList), false);
  }


}
