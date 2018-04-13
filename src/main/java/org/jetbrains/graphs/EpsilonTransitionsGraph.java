package org.jetbrains.graphs;

import org.jetbrains.regexp.parser.LanguageSymbols;
import org.jetbrains.regexp.parser.RegexParser;
import org.jetbrains.regexp.parser.token.element.Token;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * An improved version of Sedgewick's algorithm to build the NFA.
 * <p>
 * Details in his book: Algorithms(4th Edition) on p.802
 * 
 * 
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 * @see <a href="http://algs4.cs.princeton.edu/54regexp/NFA.java">NFA.java</a> for details.
 */
public class EpsilonTransitionsGraph extends DirectedGraph {

  public EpsilonTransitionsGraph(Token[] regexp) throws ParseException {
    super(regexp.length + 1);
    Stack<Integer> operators = new Stack<Integer>();
    for (int i = 0; i < regexp.length; i++) {
      int lp = i;
      final Token token = regexp[i];
      if (token == LanguageSymbols.OPEN_PAREN || token == LanguageSymbols.OR) {
        operators.push(i);
      } else if (token == LanguageSymbols.CLOSE_PAREN) {
        int or = operators.pop();
        if (regexp[or] == LanguageSymbols.OR) {
          List<Integer> orOperators = new LinkedList<Integer>();
          lp = operators.pop();
          while (regexp[lp] == LanguageSymbols.OR) {
            orOperators.add(lp);
            lp = operators.pop();
          }
          addEdge(lp, or + 1);
          addEdge(or, i);
          for (Integer orOp : orOperators) {
            addEdge(lp, orOp + 1);
            addEdge(orOp, i);
          }
        } else if (regexp[or] == LanguageSymbols.OPEN_PAREN){
          lp = or;
        } else {
          throw new ParseException(RegexParser.FAIL_MESSAGE, or);
        }
      }
      if (i < regexp.length - 1) {
        if (regexp[i + 1] == LanguageSymbols.STAR) {
          addEdge(lp, i + 1);
          addEdge(i + 1, lp);  
        }
        if (regexp[i + 1] == LanguageSymbols.PLUS) {
          addEdge(i + 1, lp);
        }
        if (regexp[i + 1] == LanguageSymbols.OPTIONAL) {
          addEdge(lp, i + 1);
        }
      }
      if (token == LanguageSymbols.OPEN_PAREN || 
          token == LanguageSymbols.STAR || 
          token == LanguageSymbols.CLOSE_PAREN || 
          token == LanguageSymbols.OPTIONAL || 
          token == LanguageSymbols.PLUS) {
        addEdge(i, i + 1);
      }
    }
  }
  
}
