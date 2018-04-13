package org.jetbrains.regexp;

import java.util.Comparator;

/**
 * Object that is used to encapsulate a potential match during a search through solutions space. 
 * 
 * @see org.jetbrains.regexp.Matcher#search() 
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
final class State {
  private Integer start = Integer.MIN_VALUE;
  private Integer end = 0;
  private Integer nfaState = 0;
  private final int acceptState;

  static final Comparator<State> BasicComparator = new Comparator<State>() {
    @Override
    public int compare(State state, State state2) {
      final int startCmp = state.start.compareTo(state2.start);
      if (startCmp == 0) {
        // reverse on the end index, we want to see the longer sequences first
        return state2.end.compareTo(state.end);
      }
      return startCmp;
    }
  };

  State(int acceptState, int nfaState, int start) {
    this.acceptState = acceptState;
    this.nfaState = nfaState;
    this.start = start;
  }

  State(int acceptState, Integer nfaState) {
    this.acceptState = acceptState;
    this.nfaState = nfaState;
  }

  public State(int acceptState, int nfaState, int start, int end) {
    this.acceptState = acceptState;
    this.nfaState = nfaState;
    this.start = 0;
    this.end = end;
  }

  Integer getStart() {
    return start;
  }

  Integer getNfaState() {
    return nfaState;
  }

  boolean setStart(int start) {
    if (this.start == Integer.MIN_VALUE) {
      this.start = start;
      return true;
    }
    return false;
  }

  Integer getEnd() {
    return end;
  }

  void setEnd(int end) {
    this.end = end;
  }

  void advance() {
    if (!inAcceptState()) nfaState++;
  }
  
  boolean inAcceptState() {
    return nfaState == acceptState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    State state = (State) o;

    if (!end.equals(state.end)) return false;
    return start.equals(state.start);

  }

  @Override
  public int hashCode() {
    int result = start.hashCode();
    result = 31 * result + end.hashCode();
    return result;
  }

}
