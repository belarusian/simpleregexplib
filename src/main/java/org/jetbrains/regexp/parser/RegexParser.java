package org.jetbrains.regexp.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.regexp.parser.token.element.CharacterToken;
import org.jetbrains.regexp.parser.token.element.Token;
import org.jetbrains.regexp.parser.token.group.MatchingPair;
import org.jetbrains.regexp.parser.token.group.MatchingRange;
import org.jetbrains.regexp.parser.token.group.MatchingSet;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Parses a regular expression into an array of Tokens.
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class RegexParser {

  private static final Logger LOG = Logger.getLogger(RegexParser.class);

  public static final String FAIL_MESSAGE = "failed to parse";
  private static final String BAD_CHARACTER_RANGE = "bad character range";
  private static final String BAD_COUNT_EXPRESSION = "bad count expression";
  private static final String BAD_CLOSURE_EXPRESSION = "bad closure expression";
  private static final String BAD_EXPRESSION = "bad && expression";
  private static final String STACK_PROBLEM = "unmatched operators on the stack";

  private final LanguageSymbols languageSymbols;
  private final String expression;
  private final List<Token> regExTokens = new LinkedList<Token>();
  private final Stack<Integer> operators = new Stack<Integer>();
  private final Token[] tokens;

  public RegexParser(String expression) throws ParseException {
    this.languageSymbols = new LanguageSymbols();
    this.expression = expression;
    this.tokens = parse();
  }

  public Token[] getTokens() {
    return tokens;
  }

  /**
   * Converts an expression string into token, giving special attention to positive and negative character ranges, 
   * count ranges and closure shortcuts. 
   * 
   * @return tokens in regular expression language 
   */
  private Token[] parse() throws ParseException {
    //algorithm in Pattern class needs the outer expression to be wrapped in parenthesis
    boolean wrap = !expression.startsWith("(") || !expression.endsWith(")");
    operators.clear();
    if (wrap) {
      regExTokens.add(LanguageSymbols.OPEN_PAREN);  
    }
    for (int i = 0; i < expression.length(); i++) {
      i = consumeToken(i, false);
    }
    if (!operators.empty()) {
      throw new ParseException(STACK_PROBLEM, expression.length());
    }
    if (wrap) {
      regExTokens.add(LanguageSymbols.CLOSE_PAREN);
    }
    LOG.info(regExTokens);
    Token[] output = new Token[regExTokens.size()];
    return regExTokens.toArray(output); 
  }

  /**
   * consumes one or more characters. writes into regExTokens the constructed language tokens.  
   * 
   * @param i the index of the to be consumed character
   * @param literal if this token is expected to be of a literal character value           
   * @return the index of the consumed character
   */
  private int consumeToken(int i, boolean literal) throws ParseException {
    final char character = expression.charAt(i);
    if (literal) {
      regExTokens.add(languageSymbols.getCharacterToken(character));  
    } else if (character == '\\') {
      return consumeToken(i + 1, true);
    } else if (character == '.') {
      regExTokens.add(LanguageSymbols.ANY);  
    } else if (character == '?') {
      regExTokens.add(LanguageSymbols.OPTIONAL);  
    } else if (character == '+') {
      regExTokens.add(LanguageSymbols.PLUS);
    } else if (character == '[') {
      pushOperator(LanguageSymbols.OPEN_BRACKET);
    } else if (character == '^') {
      validateStateAndPop(i, LanguageSymbols.OPEN_BRACKET);
      pushOperator(LanguageSymbols.NOT);
    } else if (character == '-') {
      final CharacterToken currentToken = getCurrentCharacter(i);
      i = consumeToken(i + 1, false);
      final CharacterToken nextToken = getCurrentCharacter(i);
      if (currentToken.getValue() >= nextToken.getValue()) {
        throw new ParseException(BAD_CHARACTER_RANGE, i);
      }
      removeTokensFromTo(regExTokens.size() - 2, regExTokens.size() - 1);
      regExTokens.add(new MatchingRange(currentToken, nextToken));
    } else if (character == '(') {
      regExTokens.add(LanguageSymbols.OPEN_PAREN);
      pushOperator(LanguageSymbols.OPEN_PAREN);
    } else if (character == ')') {
      regExTokens.add(LanguageSymbols.CLOSE_PAREN);
      validateStateAndPop(i, LanguageSymbols.OPEN_PAREN);
    } else if (character == '*') {
      regExTokens.add(LanguageSymbols.STAR);
    } else if (character == '|') {
      regExTokens.add(LanguageSymbols.OR);
    } else if (character == '&') {
      if (hasNext(i) && expression.charAt(i + 1) == '&') {
        regExTokens.add(LanguageSymbols.AND);
        i++;
      }
    } else if (character == ']') {
      processClosingBracket(i);
    } else if (character == '{') {
      pushOperator(LanguageSymbols.OPEN_BRACE);
    } else if (character == '}') {
      processClosingBrace(i);
    } else {
      regExTokens.add(languageSymbols.getCharacterToken(character));
    }
    return i;
  }

  private void processClosingBrace(int i) throws ParseException {
    final int index = validateStateAndPop(i, LanguageSymbols.OPEN_BRACE);
    final String inBetween = charactersBetweenBraces(i, index);
    int comma = StringUtils.indexOf(inBetween, ",");
    final List<Token> toReplicate = itemsToReplicate(index);
    if (comma == -1) {
      processCount(i, Integer.parseInt(inBetween), false, toReplicate);
    } else {
      processClosure(i, comma, toReplicate, inBetween); 
    }
  }

  private String charactersBetweenBraces(int j, int index) throws ParseException {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = index; i < regExTokens.size(); i++) {
      final Token token = regExTokens.get(i);
      if (token instanceof CharacterToken) {
        CharacterToken characterToken = (CharacterToken) token;
        stringBuilder.append(characterToken.getValue());  
      } else {
        throw new ParseException(BAD_CLOSURE_EXPRESSION, j);
      }
      
    }
    return stringBuilder.toString();
  }

  private void processClosure(int i, int comma, List<Token> toReplicate, String between) throws ParseException {
    String beforeComma = between.substring(0, comma);
    String afterComma = between.substring(comma + 1);
    final boolean blankBeforeComma = StringUtils.isBlank(beforeComma);
    final boolean blankAfterComma = StringUtils.isBlank(afterComma);
    if (!blankBeforeComma && !blankAfterComma) {
      final int[] counts = getCountNumbers(i, beforeComma, afterComma);
      writeClosureTokens(toReplicate, counts[0], counts[1]);
    } else if (!blankBeforeComma){
      final int[] counts = getCountNumbers(i, beforeComma, beforeComma);
      processCount(i, counts[0], true, toReplicate);
    } else {
      throw new ParseException(BAD_CLOSURE_EXPRESSION, i);
    }
  }

  private int[] getCountNumbers(int i, String beforeComma, String afterComma) throws ParseException {
    try {
      return new int[] {Integer.parseInt(beforeComma), Integer.parseInt(afterComma)};
    } catch (NumberFormatException exception) {
      throw new ParseException(BAD_CLOSURE_EXPRESSION, i);
    }
  }

  private void writeClosureTokens(List<Token> toReplicate, int bc, int ac) {
    LOG.info("replicating {" + bc + "," + ac + "} : " + toReplicate);
    regExTokens.add(LanguageSymbols.OPEN_PAREN);
    for (int i = bc; i <= ac; i++) {
      for (int j = 0; j < i; j++) {
        regExTokens.addAll(toReplicate);
      }
      if (i < ac) {
        regExTokens.add(LanguageSymbols.OR);
      }
    }
    regExTokens.add(LanguageSymbols.CLOSE_PAREN);
  }

  private void processCount(int i, Integer count, boolean endless, List<Token> toReplicate) throws ParseException {
    try {
      LOG.info("counting {" + count + "} :" + toReplicate);
      regExTokens.add(LanguageSymbols.OPEN_PAREN);
        for (int j = 0; j < count; j++) {
          regExTokens.addAll(toReplicate);
        }
        if (endless) {
          regExTokens.addAll(toReplicate);
        regExTokens.add(LanguageSymbols.STAR);
      }
      regExTokens.add(LanguageSymbols.CLOSE_PAREN);
    } catch (NumberFormatException exception) {
      throw new ParseException(BAD_COUNT_EXPRESSION, i);
    }
  }

  /**
   * helper method to replicate statements for closure and count statements. 
   * 
   * @param index
   * @return
   */
  private List<Token> itemsToReplicate(int index) {
    int beforeInBracket = indexOfComponent(index);
    List<Token> toReplicate = new ArrayList<Token>(regExTokens.subList(beforeInBracket, index));
    removeTokensFromTo(beforeInBracket, regExTokens.size()-1);
    return toReplicate;
  }

  /**
   * goes back finding either a single token or a group of tokens to replicate. 
   * 
   * @param index
   * @return
   */
  private int indexOfComponent(int index) {
    int beforeInBracket = index - 1;
    Token token = regExTokens.get(beforeInBracket);
    if (token == LanguageSymbols.CLOSE_PAREN) {
      Stack<Token> parenStack = new Stack<Token>();
      parenStack.push(token);
      while(!parenStack.empty()) {
        final Token nextToken = regExTokens.get(--beforeInBracket);
        if (nextToken == LanguageSymbols.OPEN_PAREN) {
          parenStack.pop();    
        } else if (nextToken == LanguageSymbols.CLOSE_PAREN) {
          parenStack.push(nextToken);
        }
      }
    } 
    return beforeInBracket;
  }

  /**
   * rewrites the regExTokens introducing tokens that would match like character sets. 
   * 
   * @param i index into the actual text string
   * @throws ParseException
   */
  private void processClosingBracket(int i) throws ParseException {
    if (operators.isEmpty()) throw new ParseException(FAIL_MESSAGE, i);
    final Integer lastOp = operators.pop();
    final Token lastTokenOp = regExTokens.get(lastOp);
    final List<Token> tokens = evaluateTokenSet(lastOp);
    final int removeStartingFrom = regExTokens.size() - 1;
    if (lastTokenOp == LanguageSymbols.NOT) {
      regExTokens.add(new MatchingSet(tokens, true));  
    } else if (lastTokenOp == LanguageSymbols.OPEN_BRACKET) {
      regExTokens.add(new MatchingSet(tokens, false));  
    } else {
      throw new ParseException(FAIL_MESSAGE, i);
    }
    removeTokensFromTo(lastOp, removeStartingFrom);
  }

  /**
   * Helper method for closing the bracket 
   * 
   * @param lastOp <p>starting index from which to read the tokens until 
   *               the end of current tokens list, used for translating [] </p>
   * @return <p>a new list of tokens which is a shallow copy of the regExTokens 
   *         list, also wrapping any && expressions</p>
   * @throws ParseException
   */
  private List<Token> evaluateTokenSet(Integer lastOp) throws ParseException {
    List<Token> tokens = new ArrayList<Token>();
    final List<Token> subList = regExTokens.subList(lastOp + 1, regExTokens.size());
    LOG.info("building [] from:" + subList);
    for (int i = 0; i < subList.size(); i++) {
      Token token = subList.get(i);
      if (token == LanguageSymbols.AND) {
        if (i+1 >= subList.size()) {
          throw new ParseException(BAD_EXPRESSION, i);
        }
        token = new MatchingPair(tokens.remove(i-1), subList.get(i+1));
        i++;
      }
      tokens.add(token);
    }
    return tokens;
  }

  private void removeTokensFromTo(Integer firstIndex, int lastIndex) {
    for (int j = lastIndex; j >= firstIndex; j--) {
      regExTokens.remove(j);
    }
  }

  private CharacterToken getCurrentCharacter(int i) throws ParseException {
    final Token token = regExTokens.get(regExTokens.size() - 1);
    if (!(token instanceof CharacterToken)) {
      throw new ParseException(FAIL_MESSAGE, i);
    }
    return (CharacterToken) token;
  }

  private boolean hasNext(int i) {
    return expression.length() > i;
  }

  private void pushOperator(Token operator) {
    LOG.info("push:" + operator);
    operators.push(regExTokens.size());
    regExTokens.add(operator);
  }

  private int validateStateAndPop(int i, Token expectedToken) throws ParseException {
    LOG.info("pop:" + expectedToken);
    if (operators.isEmpty()) {
      throw new ParseException(FAIL_MESSAGE, i);
    }
    final Integer topOperatorIndex = operators.peek();
    if (topOperatorIndex >= regExTokens.size()) {
      throw new ParseException(FAIL_MESSAGE, i);
    }
    final Token token = regExTokens.get(topOperatorIndex);
    if (expectedToken != token) {
      throw new ParseException(FAIL_MESSAGE, i);
    }
    final int index = operators.pop();
    regExTokens.remove(index);
    return index;
  }

}
