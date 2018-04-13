package org.jetbrains.graphs;

import com.google.common.collect.ListMultimap;

import java.util.LinkedList;
import java.util.List;

/**
 *   Encapsulates the resulting state after running a depth first search 
 */
class DFS {

  private final ListMultimap<Integer, Integer> digraph;
  private final int maximalVertexCount;
  private final boolean[] marked;

  DFS(ListMultimap<Integer, Integer> digraph, int maximalVertexCount) {
    this.digraph = digraph;
    this.maximalVertexCount = maximalVertexCount;
    this.marked = new boolean[maximalVertexCount];
  }
  
  void search(int v) {
    marked[v] = true;
    for (int w : digraph.get(v)) {
      if (!marked[w]) search(w);
    }
  }
  
  List<Integer> visited() {
    List<Integer> visited = new LinkedList<Integer>();
    for (int v = 0; v < maximalVertexCount; v++) {
      if (marked[v]) visited.add(v);
    }
    return visited;
  }

}
