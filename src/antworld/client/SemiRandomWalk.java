package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class creates normal distributions for moving in a semi-random direction
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

    determineDirection(data, nestName);
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
   * @param nestName
   */
  public void determineDirection(CommData data, NestNameEnum nestName)
  {
    for(AntData ant : data.myAntList)
    {
      int xdiff = ant.gridX - data.nestData[nestName.ordinal()].centerX;
      int ydiff = ant.gridY - data.nestData[nestName.ordinal()].centerY;

      if (xdiff == 0) xdiff = 1;

      double slope = xdiff / ydiff;

      if (xdiff > 0 && ydiff > 0)
      {
        if (slope <= .4) direction.put(ant.id, Direction.EAST);
        if (slope <= 2.4 && slope > .4) direction.put(ant.id, Direction.SOUTHEAST);
        if (slope > 2.4) direction.put(ant.id, Direction.SOUTH);
      }

      if (xdiff > 0 && ydiff < 0)
      {
        if (slope >= -.4) direction.put(ant.id, Direction.EAST);
        if (slope >= -2.4 && slope < -.4) direction.put(ant.id, Direction.NORTHEAST);
        if (slope < -2.4) direction.put(ant.id, Direction.NORTH);
      }

      if (xdiff < 0 && ydiff > 0)
      {
        if (slope >= -.4) direction.put(ant.id, Direction.WEST);
        if (slope >= -2.4 && slope < -.4) direction.put(ant.id, Direction.SOUTHWEST);
        if (slope < -2.4) direction.put(ant.id, Direction.SOUTH);
      }

      if (xdiff < 0 && ydiff < 0)
      {
        if (slope <= .4) direction.put(ant.id, Direction.WEST);
        if (slope <= 2.4 && slope > .4) direction.put(ant.id, Direction.NORTHWEST);
        if (slope > 2.4) direction.put(ant.id, Direction.NORTH);
      }
    }
  }

  /**
   * Gets random direction for the ant to go too
   * @param ant
   * @return Direction the ant will go to
   */
  public Direction getDirection(AntData ant)
  {
    return distributions.get(direction.get(ant.id)).get(ClientRandomWalk.random.nextInt(50));
  }
}
