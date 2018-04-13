package org.jetbrains.regexp.parser.token.element;

/**
 * A basic construct that never matches in the text. used for ()|+? and others.
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class OperatorToken implements Token {
  
  @Override
  public boolean matches(Character textCharacter) {
    return false;
  }
}
