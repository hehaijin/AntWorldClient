package antworld.client;

import java.util.LinkedList;



public class Path
{

  private LinkedList<Coordinate> path = new LinkedList<>();

  Coordinate getNext()
  {
    return path.pop();
  }

  /**
   * add to the path in reverse sequence.
   * 
   * @param node
   */
  void add(Coordinate node)
  {
    path.push(node);
  }

  int size()
  {
    return path.size();
  }
}
