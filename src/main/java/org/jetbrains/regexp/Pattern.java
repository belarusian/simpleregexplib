package org.jetbrains.regexp;

import org.jetbrains.graphs.DirectedGraph;
import org.jetbrains.graphs.EpsilonTransitionsGraph;
import org.jetbrains.regexp.parser.RegexParser;
import org.jetbrains.regexp.parser.token.element.Token;

import java.text.ParseException;

/**
 * An entry class that builds the regex digraph and epsilon transitions graphs. 
 * <p>
 *   Once the pattern is built, use matcher method to match different text inputs. 
 *   
 * @author sasha.malahov@here.com (Sasha Malahov)
 * @see Pattern#matcher(String) 
 */
public class Pattern {

  private final DirectedGraph epsilonTransitions; 
  private final Token[] tokens;

  private Pattern(Token[] tokens, DirectedGraph epsilonTransitions) {
    this.tokens = tokens;
    this.epsilonTransitions = epsilonTransitions; 
  }

  public static Pattern compile(String regexpString) throws ParseException {
    final Token[] tokens = new RegexParser(regexpString).getTokens();
    final DirectedGraph transitionsGraph = new EpsilonTransitionsGraph(tokens);
    return new Pattern(tokens, transitionsGraph);
  }

  public Matcher matcher(String txt) {
    return new Matcher(txt, tokens, epsilonTransitions);
  }

  public boolean matches(String txt) {
    final Matcher matcher = matcher(txt);
    return matcher.matches();
  }
}
