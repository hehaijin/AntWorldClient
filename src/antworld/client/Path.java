package antworld.client;

import antworld.common.Direction;
import java.util.LinkedList;

public class Path
{
  private LinkedList<Direction> path = new LinkedList<>();

  Direction getNext()
  {
    return path.getLast();
  }

  /**
   * add to the path in reverse sequence.
   * 
   * @param node
   */
  void add(Direction node)
  {
    path.addLast(node);
  }

  int size()
  {
    return path.size();
  }
  
  void addPath(Path p)
  {
    while(!p.path.isEmpty())
    {
      Direction d=p.path.getFirst(); 
      path.addLast(d);
    }
 
  }
  
  void addPathToHead(Path p)
  {
    while(!p.path.isEmpty())
    {
      Direction d=p.path.getLast();
      path.addFirst(d);
    }
    
    
  }

  // TODO do you want this?
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub
  }
}
