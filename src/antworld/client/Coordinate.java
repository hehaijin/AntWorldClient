package antworld.client;

import antworld.common.Direction;

import java.util.Objects;

/**
 * This class stores coordinates, does math with them.
 * NOTE: to calculate direction the functions need the difference in x and y coordinates.
 * This difference MUST BE the coordinate of the desired cell - the coordinate of the ant. It can also be thought of as
 * end - start
 * Created by Hector on 12/4/16.
 */
public class Coordinate
{
  private int x;
  private int y;

  /**
   * Takes in two coordinates
   * @param x coordinate
   * @param y coordinate
   */
  Coordinate(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  public int getX()
  {
    return this.x;
  }

  public int getY()
  {
    return this.y;
  }

  /**
   * @param x1 coordinate of first item
   * @param y1 coordinate of first item
   * @param x2 coordinate of second item
   * @param y2 coordinate of second item
   * @return manhattan distance
   */
  public static int getDistance(int x1, int y1, int x2, int y2)
  {
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  @Override
  /**
   * Checks if two coordinates are the same
   */
  public boolean equals(Object coordinate)
  {
    Coordinate c = (Coordinate) coordinate;
    if(this.getX() == c.getX() && this.getY() == c.getY()) return true;
    return false;
  }

  /**
   * Checks if two coordinates are equal
   * @param x
   * @param y
   * @return
   */
  public boolean equals(int x, int y)
  {
    if(this.getX() == x && this.getY() == y) return true;
    return false;
  }

  // Gives the direction of the adjacent food/enemy using coordinate subtraction
  public static Direction getDirection(int xdiff, int ydiff)
  {
    for(Direction dir : Direction.values())
    {
      if(dir.deltaX() == xdiff && dir.deltaY() == ydiff)
      {
        return dir;
      }
    }
    return null;
  }

  public static Direction getDirection(Coordinate cell, Coordinate ant)
  {
    return getDirection(cell.getX() - ant.getX(), cell.getY() - ant.getY());
  }

  public static Direction getDirectionX(Coordinate cell, Coordinate ant)
  {
    return getDirectionX(cell.getX() - ant.getX());
  }

  public static Direction getDirectionY(Coordinate cell, Coordinate ant)
  {
    return getDirectionX(cell.getY() - ant.getY());
  }

  public static Direction getDirectionX(int xdiff)
  {
    if(xdiff < 0) return Direction.EAST;
    return Direction.WEST;
  }

  public static Direction getDirectionY(int ydiff)
  {
    if(ydiff < 0) return Direction.SOUTH;
    return Direction.NORTH;
  }

  public static int getDistance(Coordinate co1, Coordinate co2)
  {
    return Math.max(Math.abs(co1.getX()-co2.getX()), Math.abs(co1.getY()-co2.getY()));
  }

}
