package org.jetbrains.regexp.parser.token.group;

import org.jetbrains.regexp.parser.token.element.CharacterToken;
import org.jetbrains.regexp.parser.token.element.Token;

/**
 * A language construct that matches if any token in the range matches the argument character. 
 * <p>
 *   Think [a-z]
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class MatchingRange implements Token {

  private final CharacterToken from;
  private final CharacterToken to;

  public MatchingRange(CharacterToken fromToken, CharacterToken toToken) {
    this.from = fromToken;
    this.to = toToken;
  }

  @Override
  public boolean matches(Character textCharacter) {
    return from.getValue() <= textCharacter && textCharacter <= to.getValue();
  }

  @Override
  public String toString() {
    return "MatchingRange{" +
        "from=" + from +
        ", to=" + to +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MatchingRange range = (MatchingRange) o;

    if (from != null ? !from.equals(range.from) : range.from != null)
      return false;
    if (to != null ? !to.equals(range.to) : range.to != null) return false;

    return true;
  }
  
}
