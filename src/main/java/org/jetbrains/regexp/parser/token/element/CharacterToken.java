package org.jetbrains.regexp.parser.token.element;

/**
 * Represents a single character in the parsed regular expression 
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class CharacterToken implements Token {

  private final Character value;
  
  public CharacterToken(Character value) {
    this.value = value;
  }

  public Character getValue() {
    return value;
  }

  @Override
  public boolean matches(Character textCharacter) {
    return value.equals(textCharacter);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CharacterToken) {
      CharacterToken toCompare = (CharacterToken) o;
      return value.equals(toCompare.value);
    } 
    return false;
  }

  @Override
  public String toString() {
    return "value=" + value;
  }

}
