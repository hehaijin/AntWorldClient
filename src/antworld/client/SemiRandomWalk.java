package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class creates normal distributions for moving in a semi-random direction.
 * Created by Hector on 12/7/16.
 */
public class SemiRandomWalk
{
  // normal distribution, makes it more likely to move in the named direction
  private ArrayList<Direction> north; // w, nw, N, ne, e
  private ArrayList<Direction> south; // e, se, S, sw, w
  private ArrayList<Direction> east; // n, ne, E, se, s
  private ArrayList<Direction> west; // s, sw, W, nw, n
  private ArrayList<Direction> northwest; // sw, w, NW, n, ne
  private ArrayList<Direction> northeast; // nw, n, NE, e, se
  private ArrayList<Direction> southwest; // se, s, SW, w, nw
  private ArrayList<Direction> southeast; // ne, e, SE, s, sw

  private HashMap<Direction, ArrayList<Direction>> distributions = new HashMap<>();

  private HashMap<Integer, Direction> direction = new HashMap<>();

  public SemiRandomWalk(CommData data, NestNameEnum nestName)
  {
    north = distribute(Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.WEST, Direction.EAST);
    south = distribute(Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.EAST, Direction.WEST);
    east = distribute(Direction.EAST, Direction.SOUTHEAST, Direction.NORTHEAST, Direction.NORTH, Direction.SOUTH);
    west = distribute(Direction.WEST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTH, Direction.NORTH);
    northwest = distribute(Direction.NORTHWEST, Direction.WEST, Direction.NORTH, Direction.SOUTHWEST, Direction.NORTHEAST);
    northeast = distribute(Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHWEST, Direction.SOUTHEAST);
    southwest = distribute(Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.SOUTHEAST, Direction.NORTHWEST);
    southeast = distribute(Direction.SOUTHEAST, Direction.EAST, Direction.SOUTH, Direction.NORTHEAST, Direction.SOUTHWEST);

    distributions.put(Direction.NORTH, north);
    distributions.put(Direction.EAST, east);
    distributions.put(Direction.WEST, west);
    distributions.put(Direction.SOUTH, south);
    distributions.put(Direction.NORTHEAST, northeast);
    distributions.put(Direction.NORTHWEST, northwest);
    distributions.put(Direction.SOUTHEAST, southeast);
    distributions.put(Direction.SOUTHWEST, southwest);
  }

  /**
   * Creates distributions
   * @param one most likely direction
   * @param two second most likely direction
   * @param three third most likely direction
   * @param four fourth most likely direction
   * @param five fifth most likely direction
   * @return
   */
  private ArrayList<Direction> distribute(Direction one, Direction two, Direction three, Direction four, Direction five)
  {
    ArrayList<Direction> distribution = new ArrayList<>();

    for(int i = 0; i < 30; i++) distribution.add(one);
    for(int i = 0; i < 8; i++) distribution.add(two);
    for(int i = 0; i < 8; i++) distribution.add(three);
    for(int i = 0; i < 2; i++) distribution.add(four);
    for(int i = 0; i < 2; i++) distribution.add(five);

    return distribution;
  }

  /**
   * Determine general direction the ants will go based on position relative to nest
   * @param data
   */
  public void determineDirection(CommData data, AntData ant, int x, int y, int nestx, int nesty)
  {
      int xdiff = x - nestx;
      int ydiff = y - nesty;

      if (ydiff == 0) ydiff = 1;
      double slope = xdiff / ydiff;

      if (xdiff >= 0 && ydiff > 0)
      {
        if (slope <= .4) {direction.put(ant.id, Direction.EAST); return;}
        if (slope <= 2.4 && slope > .4) {direction.put(ant.id, Direction.SOUTHEAST); return;}
        if (slope > 2.4) {direction.put(ant.id, Direction.SOUTH); return;}
      }

      if (xdiff >= 0 && ydiff < 0)
      {
        if (slope >= -.4) {direction.put(ant.id, Direction.EAST); return;}
        if (slope >= -2.4 && slope < -.4) {direction.put(ant.id, Direction.NORTHEAST); return;}
        if (slope < -2.4) {direction.put(ant.id, Direction.NORTH); return;}
      }

      if (xdiff <= 0 && ydiff > 0)
      {
        if (slope >= -.4) {direction.put(ant.id, Direction.WEST); return;}
        if (slope >= -2.4 && slope < -.4) {direction.put(ant.id, Direction.SOUTHWEST); return;}
        if (slope < -2.4) {direction.put(ant.id, Direction.SOUTH); return;}
      }

      if (xdiff <= 0 && ydiff < 0)
      {
        if (slope <= .4) {direction.put(ant.id, Direction.WEST); return;}
        if (slope <= 2.4 && slope > .4) {direction.put(ant.id, Direction.NORTHWEST); return;}
        if (slope > 2.4) {direction.put(ant.id, Direction.NORTH); return;}
      }
      System.out.println("Didn't choose. SLOPE: " + slope + " x: " + xdiff + " y: " + ydiff);
  }

  public void normalDirectionChange(AntData ant)
  {
    Direction newDir = distributions.get(direction.get(ant.id)).get(Constants.random.nextInt(50));
    direction.replace(ant.id, newDir);
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

  /**
   * Gets random direction for the ant to go too
   * @param ant
   * @return Direction the ant will go to
   */
  public Direction getDirection(AntData ant)
  {
    return distributions.get(direction.get(ant.id)).get(Constants.random.nextInt(50));
  }
}
