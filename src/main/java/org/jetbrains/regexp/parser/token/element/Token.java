package org.jetbrains.regexp.parser.token.element;

/**
 * The most basic token that is built by the parser. 
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 * @see org.jetbrains.regexp.parser.RegexParser
 * @see org.jetbrains.regexp.parser.LanguageSymbols
 */
public abstract interface Token {
  
  public abstract boolean matches(Character textCharacter);
  
}
