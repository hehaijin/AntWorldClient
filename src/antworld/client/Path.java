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

  public Path straightLine(int startX, int startY, int endX, int endY)
  {
    Path line = new Path();
    int xdiff = endX - startX;
    int ydiff = endY - startY;
    int xdiff_abs = Math.abs(xdiff);
    int ydiff_abs = Math.abs(ydiff);

    int dirX = xdiff/xdiff_abs;
    int dirY = ydiff/ydiff_abs;
    Direction diag = Coordinate.getDirection(dirX, dirY);
    Direction straightX = Coordinate.getDirectionX(xdiff);
    Direction straightY = Coordinate.getDirectionY(ydiff);

    int max = Math.max(xdiff_abs, xdiff_abs);

    // first goes in a diagonal
    for(int i = 0; i < Math.min(xdiff, ydiff); i++)
    {
      line.add(diag);
    }

    // then goes in straight line, either on the x axis or y axis

    if(max == xdiff_abs)
    {
      for (int i = 0; i < xdiff - ydiff; i++)
      {
        line.add(straightX);
      }
    }

    if(max == ydiff_abs)
    {
      for (int i = 0; i < ydiff - xdiff; i++)
      {
        line.add(straightY);
      }
    }

    return line;
  }

  public Path straightLine(Coordinate start, Coordinate end)
  {
    return straightLine(start.getX(), start.getY(), end.getX(), end.getY());
  }

  // TODO do you want this?
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub
  }
}
