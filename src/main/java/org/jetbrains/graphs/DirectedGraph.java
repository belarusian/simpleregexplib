package org.jetbrains.graphs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.List;

/**
 * Representation of a graph of simple nodes (Integers). 
 * Integers in this case are pointers to regular expression states.
 * 
 * @author sasha.malahov@here.com (Sasha Malahov)
 */
public class DirectedGraph {

  private final ListMultimap<Integer, Integer> digraph = ArrayListMultimap.create();
  private final int maximalVertexCount;

  /**
   * 
   * @param maximalVertexCount number of vertices in the graph, useful for DFS 
   *                           because not all vertices have outgoing edges
   */
  DirectedGraph(int maximalVertexCount) {
    this.maximalVertexCount = maximalVertexCount;
  }

  public List<Integer> dfsFrom(List<Integer> vs) {
    final DFS dfs = new DFS(digraph, maximalVertexCount);
    for (int v : vs) {
      dfs.search(v);
    }
    return dfs.visited();
  }

  public List<Integer> dfsFrom(Integer v) {
    final DFS dfs = new DFS(digraph, maximalVertexCount);
    dfs.search(v);
    return dfs.visited();
  }

  void addEdge(Integer from, Integer to) {
    digraph.put(from, to);
    assert digraph.keySet().size() <= maximalVertexCount;
  }

}
