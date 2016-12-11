package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class creates normal distributions for moving in a semi-random direction.
 * Created by Hector on 12/7/16.
 */
public class Explore
{
  private class Vertex
  {
    Coordinate co;
    boolean visited = false;
    ArrayList<Vertex> adjacent = new ArrayList<>();
    HashMap<Vertex, Path> paths = new HashMap<>();

    public Vertex(int x, int y)
    {
      co = new Coordinate(x,y);
    }
  }

  Graph graph;
  // normal distribution, makes it more likely to move in the named direction
  private ArrayList<Direction> north; // w, nw, N, ne, e
  private ArrayList<Direction> south; // e, se, S, sw, w
  private ArrayList<Direction> east; // n, ne, E, se, s
  private ArrayList<Direction> west; // s, sw, W, nw, n
  private ArrayList<Direction> northwest; // sw, w, NW, n, ne
  private ArrayList<Direction> northeast; // nw, n, NE, e, se
  private ArrayList<Direction> southwest; // se, s, SW, w, nw
  private ArrayList<Direction> southeast; // ne, e, SE, s, sw

//  private ArrayList<>

  private Vertex[][] vertices;

  private ArrayList<Direction> possible = new ArrayList<>(8);

  private HashMap<Integer, Integer> numCollisions = new HashMap<>();
  private HashMap<Direction, ArrayList<Direction>> distributions = new HashMap<>();

  private HashMap<Integer, Direction> direction = new HashMap<>();

  public Explore(Graph graph)
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

    this.graph = graph;
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

    for(int i = 0; i < 30; i++) distribution.add(one);
    for(int i = 0; i < 8; i++) distribution.add(middleLeft);
    for(int i = 0; i < 8; i++) distribution.add(middleRight);
    for(int i = 0; i < 2; i++) distribution.add(right);
    for(int i = 0; i < 2; i++) distribution.add(left);

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

//    if (xdiff >= 0 && ydiff > 0)
//    {
//      if (slope <= .4)
//      {
//        direction.put(ant.id, Direction.EAST);
//        return;
//      }
//      if (slope <= 2.4 && slope > .4)
//      {
//        direction.put(ant.id, Direction.SOUTHEAST);
//        return;
//      }
//      if (slope > 2.4)
//      {
//        direction.put(ant.id, Direction.SOUTH);
//        return;
//      }
//    }
//
//    if (xdiff >= 0 && ydiff < 0)
//    {
//      if (slope >= -.4)
//      {
//        direction.put(ant.id, Direction.EAST);
//        return;
//      }
//      if (slope >= -2.4 && slope < -.4)
//      {
//        direction.put(ant.id, Direction.NORTHEAST);
//        return;
//      }
//      if (slope < -2.4)
//      {
//        direction.put(ant.id, Direction.NORTH);
//        return;
//      }
//    }
//
//    if (xdiff <= 0 && ydiff > 0)
//    {
//      if (slope >= -.4)
//      {
//        direction.put(ant.id, Direction.WEST);
//        return;
//      }
//      if (slope >= -2.4 && slope < -.4)
//      {
//        direction.put(ant.id, Direction.SOUTHWEST);
//        return;
//      }
//      if (slope < -2.4)
//      {
//        direction.put(ant.id, Direction.SOUTH);
//        return;
//      }
//    }
//
//    if (xdiff <= 0 && ydiff < 0)
//    {
//      if (slope <= .4)
//      {
//        direction.put(ant.id, Direction.WEST);
//        return;
//      }
//      if (slope <= 2.4 && slope > .4)
//      {
//        direction.put(ant.id, Direction.NORTHWEST);
//        return;
//      }
//      if (slope > 2.4)
//      {
//        direction.put(ant.id, Direction.NORTH);
//        return;
//      }
//    }

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
    Direction[] d = new Direction[3];
    d[0] = direction.get(ant.id);
    d[1] = Direction.getLeftDir(d[0]);
    d[2] = Direction.getRightDir(d[0]);

    direction.replace(ant.id, d[Constants.random.nextInt(3)]);
//    direction.replace(ant.id, getDirection(ant));
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
      Direction back = Direction.getLeftDir(Direction.getLeftDir(direction.get(ant.id)));
      if(possible.contains(back)) possible.remove(back);
      if(possible.contains(Direction.getLeftDir(back))) possible.remove(Direction.getLeftDir(back));
      if(possible.contains(Direction.getRightDir(back))) possible.remove(Direction.getRightDir(back));
//      Direction left = Direction.getLeftDir(direction.get(ant.id));
//      if(possible.contains(left)) possible.remove(left);
//      Direction furtherleft = Direction.getLeftDir(left);
//      if(possible.contains(furtherleft)) possible.remove(furtherleft);
      direction.replace(ant.id, possible.get(Constants.random.nextInt(possible.size())));
    }
  }

  public void genVertices()
  {
    int xTotal = 5000/AIconstants.BLOCK_SIZE;
    int yTotal = 2500/AIconstants.BLOCK_SIZE;

    vertices = new Vertex[xTotal][yTotal];

    for(int col = 0; col < xTotal; col++)
    {
      for(int row = 0; row < yTotal; row++)
      {
        if(Graph.getLandType((col+1)*AIconstants.BLOCK_SIZE, (row+1)*AIconstants.BLOCK_SIZE) == LandType.GRASS)
        {
          vertices[col][row] = new Vertex((col + 1) * AIconstants.BLOCK_SIZE, (row + 1) * AIconstants.BLOCK_SIZE);
        }
      }
    }

    for(int col = 0; col < xTotal; col++)
    {
      for (int row = 0; row < yTotal; row++)
      {
        if(vertices[col][row] != null)
        {
          genAdjacency(vertices[col][row], col, row);
        }
      }
    }

    Vertex v0;
    for(int col = 0; col < xTotal; col++)
    {
      for (int row = 0; row < yTotal; row++)
      {
        v0 = vertices[col][row];
        for(Vertex v1 : v0.adjacent)
        {
           v0.paths.put(v1, graph.findPath(v0.co.getX(), v0.co.getY(), v1.co.getX(), v1.co.getY()));
        }
      }
    }
  }

  private void genAdjacency(Vertex v, int col, int row)
  {
    for(int i = 0; i < 9; i++)
    {
      int m = i / 3 - 1;
      int n = i % 3 - 1;

      if(v.co.getX() + AIconstants.BLOCK_SIZE*m < 5000 && v.co.getX() + AIconstants.BLOCK_SIZE*m >= 0
              && v.co.getY() + AIconstants.BLOCK_SIZE*n < 2500 && v.co.getY() + AIconstants.BLOCK_SIZE*n >= 0)
      {
        if(vertices[col+m][row+n] != null)
        {
          v.adjacent.add(vertices[col+m][row+n]);
        }
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
    return distributions.get(direction.get(ant.id)).get(Constants.random.nextInt(50));
    //return direction.get(ant.id);
  }
}
