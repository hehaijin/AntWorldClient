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

  private ArrayList<Direction> possible = new ArrayList<>(8);

  private HashMap<Integer, Integer> numCollisions = new HashMap<>();
  private HashMap<Direction, ArrayList<Direction>> distributions = new HashMap<>();

  private HashMap<Integer, Direction> direction = new HashMap<>();

  public SemiRandomWalk(CommData data, NestNameEnum nestName)
  {

    north = distribute(Direction.NORTH);
    south = distribute(Direction.SOUTH);
    east = distribute(Direction.EAST);
    west = distribute(Direction.WEST);
    northwest = distribute(Direction.NORTHWEST);
    northeast = distribute(Direction.NORTHEAST);
    southwest = distribute(Direction.SOUTHWEST);
    southeast = distribute(Direction.SOUTHEAST);

    distributions.put(Direction.NORTH, north);
    distributions.put(Direction.EAST, east);
    distributions.put(Direction.WEST, west);
    distributions.put(Direction.SOUTH, south);
    distributions.put(Direction.NORTHEAST, northeast);
    distributions.put(Direction.NORTHWEST, northwest);
    distributions.put(Direction.SOUTHEAST, southeast);
    distributions.put(Direction.SOUTHWEST, southwest);

    // for collisions
    possible.add(Direction.NORTH);
    possible.add(Direction.SOUTH);
    possible.add(Direction.EAST);
    possible.add(Direction.WEST);
    possible.add(Direction.NORTHEAST);
    possible.add(Direction.NORTHWEST);
    possible.add(Direction.SOUTHEAST);
    possible.add(Direction.SOUTHWEST);

    for(AntData ant : data.myAntList)
    {
      numCollisions.put(ant.id, 0);
    }
  }

  /**
   * Creates distributions
   * @param one most likely direction
   * @return array distribution
   */
  private ArrayList<Direction> distribute(Direction one)
  {
    ArrayList<Direction> distribution = new ArrayList<>(50);
    Direction left = Direction.getLeftDir(one);
    Direction right = Direction.getRightDir(one);
    Direction middleLeft = Direction.getRightDir(left);
    Direction middleRight = Direction.getLeftDir(right);

    for(int i = 0; i < 24; i++) distribution.add(one);
    for(int i = 0; i < 10; i++) distribution.add(middleLeft);
    for(int i = 0; i < 10; i++) distribution.add(middleRight);
    for(int i = 0; i < 3; i++) distribution.add(right);
    for(int i = 0; i < 3; i++) distribution.add(left);

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

//      if (xdiff >= 0 && ydiff > 0)
//      {
//        if (slope <= .4) {direction.put(ant.id, Direction.EAST); return;}
//        if (slope <= 2.4 && slope > .4) {direction.put(ant.id, Direction.SOUTHEAST); return;}
//        if (slope > 2.4) {direction.put(ant.id, Direction.SOUTH); return;}
//      }
//
//      if (xdiff >= 0 && ydiff < 0)
//      {
//        if (slope >= -.4) {direction.put(ant.id, Direction.EAST); return;}
//        if (slope >= -2.4 && slope < -.4) {direction.put(ant.id, Direction.NORTHEAST); return;}
//        if (slope < -2.4) {direction.put(ant.id, Direction.NORTH); return;}
//      }
//
//      if (xdiff <= 0 && ydiff > 0)
//      {
//        if (slope >= -.4) {direction.put(ant.id, Direction.WEST); return;}
//        if (slope >= -2.4 && slope < -.4) {direction.put(ant.id, Direction.SOUTHWEST); return;}
//        if (slope < -2.4) {direction.put(ant.id, Direction.SOUTH); return;}
//      }
//
//      if (xdiff <= 0 && ydiff < 0)
//      {
//        if (slope <= .4) {direction.put(ant.id, Direction.WEST); return;}
//        if (slope <= 2.4 && slope > .4) {direction.put(ant.id, Direction.NORTHWEST); return;}
//        if (slope > 2.4) {direction.put(ant.id, Direction.NORTH); return;}
//      }

    if (xdiff >= 0 && ydiff > 0)
    {
      if (slope <= 1) {direction.put(ant.id, Direction.EAST); return;}
      if (slope > 1) {direction.put(ant.id, Direction.SOUTH); return;}
    }

    if (xdiff >= 0 && ydiff < 0)
    {
      if (slope >= -1) {direction.put(ant.id, Direction.EAST); return;}
      if (slope < -1) {direction.put(ant.id, Direction.NORTH); return;}
    }

    if (xdiff <= 0 && ydiff > 0)
    {
      if (slope >= -1) {direction.put(ant.id, Direction.WEST); return;}
      if (slope < -1) {direction.put(ant.id, Direction.SOUTH); return;}
    }

    if (xdiff <= 0 && ydiff < 0)
    {
      if (slope <= 1) {direction.put(ant.id, Direction.WEST); return;}
      if (slope > 1) {direction.put(ant.id, Direction.NORTH); return;}
    }
      System.out.println("Didn't choose. SLOPE: " + slope + " x: " + xdiff + " y: " + ydiff);
  }

  public void normalDirectionChange(AntData ant)
  {
    Direction[] d = new Direction[5];
    d[0] = direction.get(ant.id);
    d[1] = Direction.getLeftDir(d[0]);
    d[2] = Direction.getRightDir(d[0]);
    d[3] = Direction.getLeftDir(d[2]);
    d[4] = Direction.getRightDir(d[1]);

    direction.replace(ant.id, d[Constants.random.nextInt(5)]);
  }

  public void waterDirectionChange(AntData ant)
  {
    ArrayList<Direction> possible = new ArrayList<>(8);
    boolean collision = false;
    possible.addAll(this.possible);
    for (int i = 0; i < 9; i++)
    {
      int m = i / 3 - 1;
      int n = i % 3 - 1;

      if (Graph.getLandType(ant.gridX + m, ant.gridY + n) == LandType.WATER)
      {
        possible.remove(Coordinate.getDirection(m, n));
        collision = true;
      }
    }

    if(collision == true)
    {
      // remove going back
      possible.remove(Direction.getLeftDir(Direction.getLeftDir(direction.get(ant.id))));
      direction.replace(ant.id, possible.get(Constants.random.nextInt(possible.size())));
    }
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
