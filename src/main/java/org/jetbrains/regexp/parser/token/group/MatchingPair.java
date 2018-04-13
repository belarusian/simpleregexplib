package org.jetbrains.regexp.parser.token.group;

import org.jetbrains.regexp.parser.token.element.Token;

/**
 * A language construct that encapsulates two tokens and it only accepts the input 
 * if both tokens match. 
 * <p>
 *   Think [a&b]
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class MatchingPair implements Token {
  
  private final Token first;
  private final Token second;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MatchingPair that = (MatchingPair) o;

    if (first != null ? !first.equals(that.first) : that.first != null)
      return false;
    if (second != null ? !second.equals(that.second) : that.second != null)
      return false;

    return true;
  }


  public MatchingPair(Token first, Token second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean matches(Character textCharacter) {
    return first.matches(textCharacter) && second.matches(textCharacter);
  }

  @Override
  public String toString() {
    return "MatchingPair{" +
        "first=" + first +
        ", second=" + second +
        '}';
  }
}
