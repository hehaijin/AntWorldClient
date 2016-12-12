package antworld.client;

import antworld.common.Constants;
import antworld.common.Direction;

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

  private static Direction[] n = {Direction.NORTH, Direction.getLeftDir(Direction.NORTH), Direction.getRightDir(Direction.NORTH)};
  private static Direction[] s = {Direction.SOUTH, Direction.getLeftDir(Direction.SOUTH), Direction.getRightDir(Direction.SOUTH)};
  private static Direction[] e = {Direction.EAST, Direction.getLeftDir(Direction.EAST), Direction.getRightDir(Direction.EAST)};
  private static Direction[] w = {Direction.WEST, Direction.getLeftDir(Direction.WEST), Direction.getRightDir(Direction.WEST)};
  private static Direction[] nw = {Direction.NORTHWEST, Direction.getLeftDir(Direction.NORTHWEST), Direction.getRightDir(Direction.NORTHWEST)};
  private static Direction[] ne = {Direction.NORTHEAST, Direction.getLeftDir(Direction.NORTHEAST), Direction.getRightDir(Direction.NORTHEAST)};
  private static Direction[] se = {Direction.SOUTHEAST, Direction.getLeftDir(Direction.SOUTHEAST), Direction.getRightDir(Direction.SOUTHEAST)};
  private static Direction[] sw = {Direction.SOUTHWEST, Direction.getLeftDir(Direction.SOUTHWEST), Direction.getRightDir(Direction.SOUTHWEST)};

  private static Direction[][] dir = {n, ne, e, se, s, sw, w, nw};

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
  public static int manhattanDistance(int x1, int y1, int x2, int y2)
  {
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  public static int manhattanDistance(Coordinate c1, Coordinate c2)
  {
    return manhattanDistance(c1.getX(), c1.getY(), c2.getX(), c2.getY());
  }


  /**
   * Checks if two coordinates are the same
   */
  @Override
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

  public static Direction getXDirection(Coordinate cell, Coordinate ant)
  {
    return getXDirection(cell.getX() - ant.getX());
  }

  public static Direction getYDirection(Coordinate cell, Coordinate ant)
  {
    return getYDirection(cell.getY() - ant.getY());
  }

  public static Direction getXDirection(int xdiff)
  {
    if(xdiff < 0) return Direction.WEST;
    return Direction.EAST;
  }

  public static Direction getYDirection(int ydiff)
  {
    if(ydiff < 0) return Direction.NORTH;
    return Direction.SOUTH;
  }

  public static int linearDistance(Coordinate co1, Coordinate co2)
  {
    return Math.max(Math.abs(co1.getX()-co2.getX()), Math.abs(co1.getY()-co2.getY()));
  }

  public static Direction generalDir(Direction d)
  {
    return dir[d.ordinal()][Constants.random.nextInt(3)];
  }

}
