package org.jetbrains.regexp;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.jetbrains.graphs.DirectedGraph;
import org.jetbrains.regexp.parser.token.element.Token;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Finds matching patterns in text by using parsed token symbols and epsilon transitions graph. 
 * 
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 * @see org.jetbrains.graphs.EpsilonTransitionsGraph
 * @see org.jetbrains.regexp.parser.token.element.Token
 */
public class Matcher {
  
  private static final Predicate<State> NOT_DONE = new Predicate<State>() {
    @Override
    public boolean apply(State inputState) {
      return !inputState.inAcceptState();
    }
  };

  private static final Predicate<State> DONE = new Predicate<State>() {
    @Override
    public boolean apply(State inputState) {
      return inputState.inAcceptState();
    }
  };
  
  private final String text;
  /**
   * regular expression token DAG
   */
  private final Token[] regexp;
  /**
   * a cyclic graph that describes all possible free transitions that the algorithm can take into token DAG
   */
  private final DirectedGraph epsilonTransitions;
  private final int acceptState;
  /**
   * after matches or match is executed, contains states which index into text that pattern matches
   */
  private List<State> finalStates = null;
  private int findPointer = -1;

  public Matcher(String text, Token[] tokens, DirectedGraph epsilonTransitions) {
    this.text = text;
    this.regexp = tokens;
    this.epsilonTransitions = epsilonTransitions;
    this.acceptState = tokens.length;
  }

  /**
   * Walks through the regex by visiting many vertices per same logical step. 
   * <p>
   *   The basic execution of this NFA is described by 
   *   Robert Sedgewick in algorithm 5.9 of Algorithms (4th Edition) p.802
   * </p>
   * 
   *
   * @return true if text is matched by the pattern entirely
   * @see org.jetbrains.graphs.EpsilonTransitionsGraph
   * @see <a href="http://algs4.cs.princeton.edu/54regexp/NFA.java">NFA.java</a> for details.
   */
  public boolean matches() {
    List<Integer> currentStates = epsilonTransitions.dfsFrom(0);
    for (int i = 0; i < text.length(); i++) {
      List<Integer> visitedStates = new LinkedList<Integer>();
      for (int v : currentStates) {
        if (v != acceptState && regexp[v].matches(text.charAt(i))) {
          visitedStates.add(v + 1);
        }
      }
      currentStates = epsilonTransitions.dfsFrom(visitedStates);
      if (currentStates.isEmpty()) return false;
    }
    final boolean matches = currentStates.contains(acceptState);
    if (matches) {
      finalStates = new LinkedList<State>();
      finalStates.add(new State(acceptState, acceptState, 0, text.length()));
      findPointer = 0;
    }
    return matches;
  }

  /**
   * Lets the client to tune the obtained results (truncated or not). Can be called before find
   * . 
   * If the match succeeds, more information can be obtained via the start, end, and group methods.
   * 
   * @param allMatches true if you want to see ALL strings that match the pattern,
   *                   greedy, or reluctant. false otherwise
   * @see org.jetbrains.regexp.Matcher#search() 
   * @see org.jetbrains.regexp.Matcher#find()
   * @see org.jetbrains.regexp.Matcher#cleanMatches() 
   *                   
   */
  public void match(boolean allMatches) {
    findPointer = -1;
    finalStates = search();
    Collections.sort(finalStates, State.BasicComparator);
    if (!allMatches) {
      cleanMatches();
    }
  }

  public boolean find() {
    if (finalStates == null) {
      match(false);
    }
    return finalStates.size() > ++findPointer;
  }

  public int end() {
    assertState();
    final State state = finalStates.get(findPointer);
    return state.getEnd();
  }

  public int start() {
    assertState();
    final State state = finalStates.get(findPointer);
    return state.getStart();
  }

  public String group() {
    if (start() > -1 && end() > -1) {
      return text.substring(start(), end());
    }
    return null;
  }

  /**
   *  Because the solution contains all possible matches, 
   *  mimic the greedy quantifiers. 
   *  
   *  <p> the states have been sorted on starting position then on reverse end position. 
   *  move from left to right and remove any future state that starts before current ends, 
   *  or any state that starts on the same position as the previous state. </p>
   *  
   */
  private void cleanMatches() {
    for (int i = 0; i < finalStates.size()-1; i++) {
      final State state = finalStates.get(i);
      final State stateAfter = finalStates.get(i + 1);
      if (state.getEnd() > stateAfter.getStart() ||
          state.getStart().equals(stateAfter.getStart())) {
        finalStates.remove(i + 1);
        i--;
      }
    }
  }

  /**
   *  Run a search through the regular expression and epsilon transition graphs.
   *  <p>
   *  The idea is the following: 
   *  every character can match any of the starting states in the regular expression graph. 
   *  each of those states can also transition through an epsilon link to another state. 
   *  so, go through each character and check if it matches any of starting states or any of the already existent states. 
   *  If it does match, then transition those states to the next phase and check if they accept, 
   *  otherwise discard.
   *  </p>
   *  
   *  @return the states that end up in accepted state after matching the pattern
   */
  private LinkedList<State> search() {
    Set<State> doneStates = new HashSet<State>();
    Collection<State> currentStates = getStartStates();
    for (int i = 0; i <= text.length(); i++) {
      Collection<State> visitedStates = new LinkedList<State>();
      for (State currentState : currentStates) {
        if (currentState.inAcceptState()) {
          if (currentState.setStart(i)) {
            currentState.setEnd(i);
          }
          doneStates.add(currentState);
          List<State> states = takeEpsilonTransitions(currentState);
          visitedStates.addAll(Collections2.filter(states, NOT_DONE));
        } else if (i < text.length() && 
            regexp[currentState.getNfaState()].matches(text.charAt(i))) {
          currentState.setStart(i);
          currentState.advance();
          List<State> states = takeEpsilonTransitions(currentState);
          visitedStates.addAll(Collections2.filter(states, NOT_DONE));
          for (State inputState : Collections2.filter(states, DONE)) {
            inputState.setEnd(i + 1);
            doneStates.add(inputState);
          }
        }
      }
      visitedStates.addAll(getStartStates());
      currentStates = visitedStates;
    }
    return new LinkedList<State>(doneStates);
  }

  /**
   * 
   * @return all states in the regular expression character DAG that could begin the first match. 
   */
  private List<State> getStartStates() {
    final List<Integer> startStates = epsilonTransitions.dfsFrom(0);
    return Lists.transform(startStates, new Function<Integer, State>() {
      @Override
      public State apply(Integer nfaState) {
        return new State(acceptState, nfaState);
      }
    });
  }

  /**
   * 
   * @param visitedState the previously partially matched state
   * @return all states that could potentially match from the argument state. 
   */
  private List<State> takeEpsilonTransitions(final State visitedState) {
    List<State> updatedStates = new LinkedList<State>();
    List<Integer> visitedList = epsilonTransitions.dfsFrom(visitedState.getNfaState());
    final List<State> visited = Lists.transform(
        visitedList,
        new Function<Integer, State>() {
          @Override
          public State apply(Integer newState) {
            return new State(acceptState, newState, visitedState.getStart());
          }
        });
    updatedStates.addAll(visited);
    return updatedStates;
  }

  /**
   *  just to make sure that you call find or matches before calling group end or start
   */
  private void assertState() {
    if (finalStates == null) {
      throw new IllegalStateException();
    }
    if (findPointer >= finalStates.size()) {
      findPointer = finalStates.size()-1;
    }
  }
  
}
