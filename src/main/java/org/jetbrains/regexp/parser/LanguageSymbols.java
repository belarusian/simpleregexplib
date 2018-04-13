package org.jetbrains.regexp.parser;

import org.jetbrains.regexp.parser.token.element.CharacterToken;
import org.jetbrains.regexp.parser.token.element.OperatorToken;
import org.jetbrains.regexp.parser.token.element.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulating all the tokens that are used in when evaluating 
 * regular expressions. 
 * <p>
 *   You may notice that not all tokens are defined. For example there is OPEN_BRACKET 
 *   and OPEN_BRACE but no closing. Yet there are both closing and opening PAREN. 
 *   This is because PAREN are tokens that are understood by the evaluating algorithm, 
 *   while BRACKETS and BRACE are used by the parser internally to translate into another 
 *   structure. 
 *   
 * @see org.jetbrains.regexp.Matcher
 * @see org.jetbrains.regexp.parser.RegexParser
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class LanguageSymbols {
  
  public static final Token STAR = new OperatorToken() {
    @Override
    public String toString() {
      return "*";
    }
  }; 
  
  public static final Token PLUS = new OperatorToken() {
    @Override
    public String toString() {
      return "+";
    }
  };
  
  public static final Token OPTIONAL = new OperatorToken() {
    @Override
    public String toString() {
      return "?";
    }
  }; 
  
  public static final Token CLOSE_PAREN = new OperatorToken() {
    @Override
    public String toString() {
      return ")";
    }
  }; 
  
  public static final Token OPEN_PAREN = new OperatorToken() {
    @Override
    public String toString() {
      return "(";
    }
  }; 
  
  public static final Token OPEN_BRACKET = new OperatorToken() {
    @Override
    public String toString() {
      return "[";
    }
  }; 
  
  public static final Token AND = new OperatorToken() {
    @Override
    public String toString() {
      return "&&";
    }
  };
  
  public static final Token NOT = new OperatorToken(){
    @Override
    public String toString() {
      return "^";
    }
  };
  
  public static final Token OR = new OperatorToken(){
    @Override
    public String toString() {
      return "|";
    }
  };
  
  public static final Token ANY = new OperatorToken() {
    @Override
    public String toString() {
      return ".";
    }

    @Override
    public boolean matches(Character textCharacter) {
      return true;
    }
  };
  
  public static final Token OPEN_BRACE = new OperatorToken() {
    @Override
    public String toString() {
      return "{";
    }
  };

  private final Map<Character, Token> tokens = new HashMap<Character, Token>();

  public Token getCharacterToken(Character character) {
    Token token = tokens.get(character);
    if (token != null) return token;
    token = new CharacterToken(character);
    tokens.put(character, token);
    return token;
  }
  
}
