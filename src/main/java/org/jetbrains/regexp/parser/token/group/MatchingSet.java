package org.jetbrains.regexp.parser.token.group;

import org.jetbrains.regexp.parser.token.element.Token;

import java.util.List;

/**
 * A combinatorial structure which may contain MatchingRange and MatchingPair, 
 * or any other not OperatorToken characters. It matches the character as long as 
 * it's in the set. 
 * <p>
 *   Think [ab]. However it can actually be [a[b&&c][0-9]]
 *   
 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class MatchingSet implements Token {

  private final List<Token> matchTokens;
  private boolean positive = true;
  
  public MatchingSet(List<Token> tokens, boolean negativeSet) {
    this.positive = !negativeSet;
    this.matchTokens = tokens;
  }

  @Override
  public boolean matches(Character textCharacter) {
    boolean matched = false;
    for (Token matchToken : matchTokens) {
      if (matchToken.matches(textCharacter)) {
        matched = true;
        if (positive) {
          return true;
        }
      }
    }
    return !positive && !matched;
  }

  @Override
  public String toString() {
    return "MatchingSet{" +
        "matchTokens=" + matchTokens +
        ", positive=" + positive +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MatchingSet that = (MatchingSet) o;

    if (positive != that.positive) return false;
    if (matchTokens != null ? !matchTokens.equals(that.matchTokens) : that.matchTokens != null)
      return false;

    return true;
  }
  
}
