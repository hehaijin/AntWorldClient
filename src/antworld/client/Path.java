package antworld.client;

import antworld.common.Direction;
import java.util.LinkedList;

public class Path
{
  private LinkedList<Direction> path = new LinkedList<>();

  Direction getNext()
  {
    return path.pop();
  }

  /**
   * add to the path in reverse sequence.
   * 
   * @param node
   */
  void add(Direction node)
  {
    path.push(node);
  }

  int size()
  {
    return path.size();
  }

  // TODO do you want this?
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub
  }
}
