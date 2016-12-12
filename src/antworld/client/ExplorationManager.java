package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * This class creates normal distributions for moving in a semi-random direction.
 * Created by Hector on 12/7/16.
 */
public class ExplorationManager
{
  private class Vertex
  {
    Coordinate co;
    boolean visited = false;
    short color;
    ArrayList<Vertex> adjacent = new ArrayList<>();
    HashMap<Vertex, Path> paths = new HashMap<>();
    int cost_so_far = Integer.MAX_VALUE;
    int cost_estimate = Integer.MAX_VALUE;
    int x;
    int y;
    Vertex pre;

    public Vertex(int x, int y)
    {
      co = new Coordinate(x,y);
      this.x = x/AIconstants.BLOCK_SIZE;
      this.y = y/AIconstants.BLOCK_SIZE;
    }

    public Path getPath(Vertex v)
    {
      return this.paths.get(v);
    }

    public int costestimate(Vertex v, Vertex u)
    {
      return Coordinate.manhattanDistance(v.x,v.y,u.x,u.y);
    }

//    public int getCost(Vertex v)
//    {
//      return this.cost.get(v);
//    }
  }

  Graph graph;
  int xTotal = 5000/AIconstants.BLOCK_SIZE;
  int yTotal = 2500/AIconstants.BLOCK_SIZE;

//  private ArrayList<>

  private ArrayList<Vertex> visited = new ArrayList<>();
  private Vertex[][] vertices;

  public ExplorationManager(Graph graph)
  {
    this.graph = graph;
    genVertices();
  }

  private void genVertices()
  {
    vertices = new Vertex[xTotal][yTotal];

    for(int col = 0; col < xTotal; col++)
    {
      for(int row = 0; row < yTotal; row++)
      {
        if(Graph.getLandType((col)*AIconstants.BLOCK_SIZE, (row)*AIconstants.BLOCK_SIZE) == LandType.GRASS)
        {
          vertices[col][row] = new Vertex((col) * AIconstants.BLOCK_SIZE, (row) * AIconstants.BLOCK_SIZE);
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
        if(v0 != null)
        {
          for (Vertex v1 : v0.adjacent)
          {
            v0.paths.put(v1, Path.straightLine(v0.co.getX(), v0.co.getY(), v1.co.getX(), v1.co.getY()));
          }
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
        if(col+m < vertices.length && row+n < vertices[col+m].length)
        {
          if (vertices[col + m][row + n] != null)
          {
            v.adjacent.add(vertices[col + m][row + n]);
          }
        }
      }
    }
  }

  private Vertex findClosestVertex(Coordinate c)
  {
    int col = c.getX();
    int row = c.getY();

    try
    {
      if (vertices[col][row] != null) return vertices[col][row];
      if (vertices[col + 1][row + 1] != null) return vertices[col][row];
      if (vertices[col + 1][row] != null) return vertices[col][row];
      if (vertices[col][row + 1] != null) return vertices[col][row];
    } catch(IndexOutOfBoundsException e)
    {
      System.out.println("Index out of bounds, but it was handled");
    }
    // this situation should never happen, otherwise, how did the ant even get here?
    return null;
  }

  private void reset()
  {
    for(int col = 0; col < xTotal; col++)
    {
      for (int row = 0; row < yTotal; row++)
      {
        if(vertices[col][row] != null)
        {
          vertices[col][row].cost_estimate = Short.MAX_VALUE;
          vertices[col][row].cost_so_far = Short.MAX_VALUE;
        }
      }
    }
  }

  private ArrayList<Vertex> vertexList(Vertex start, Vertex end)
  {
    long t1 = System.currentTimeMillis();

    int startx = start.co.getX()/AIconstants.BLOCK_SIZE;
    int starty = start.co.getY()/AIconstants.BLOCK_SIZE;
    int endx = end.co.getX()/AIconstants.BLOCK_SIZE;
    int endy = end.co.getY()/AIconstants.BLOCK_SIZE;

    reset();

    // TODO: might need to check if enough connections

    vertices[startx][starty].cost_so_far = 0;

    PriorityQueue<Vertex> frontier = new PriorityQueue<>(100, new Comparator<Vertex>()
    {

      @Override
      public int compare(Vertex o1, Vertex o2)
      {
        // TODO Auto-generated method stub
        return o1.cost_so_far + o1.cost_estimate - o2.cost_so_far - o2.cost_estimate;
      }
    });

    frontier.offer(vertices[startx][starty]);

    System.out.println("start calculating path");
    loop: while (frontier.size() > 0)
    {
      Vertex u = frontier.poll();

      for (Vertex v : u.adjacent)
      {
        if (v.color == 1)
        {
          v.color = 0;
          // TODO this will either be cost or just + 1
          if (v.cost_so_far > u.cost_so_far + 1)
          {
            v.cost_so_far = u.cost_so_far + 1;
            v.cost_estimate = v.cost_so_far + v.costestimate(v, vertices[end.x][end.y]);
          }
          v.pre = u;
          frontier.offer(v);
        }
        if (v.x == end.x && v.y == end.y)
          break loop;

      }
    }

    // adding nodes to the path
    ArrayList<Vertex> list = new ArrayList<>();
    Vertex p = vertices[end.x][end.y];
    Vertex pre = p.pre;
    // path.add(Coordinate.getDirection(p.x - pre.x, p.y - pre.y));
    while (p.pre != null)
    {
      list.add(p);
      p = pre;
      pre = p.pre;

    }
    System.out.println("finding the path takes " + (System.currentTimeMillis() - t1) + "ms");
    return list;
  }

  public Path genPath(Coordinate s, Vertex end)
  {
    Vertex start = vertices[s.getX()/AIconstants.BLOCK_SIZE][s.getY()/AIconstants.BLOCK_SIZE];

    ArrayList<Vertex> p = vertexList(start, end);

    Vertex current = findClosestVertex(s);
    Vertex next = start;

    Path path = Path.straightLine(current.co.getX(), current.co.getY(), next.co.getX(),next.co.getY());

    current = start;
    next = p.remove(0);

//    path.addPathToHead(Path.straightLine(current.co.getX(), current.co.getY(), next.co.getX(),next.co.getY()));
    path.addPathToHead(current.getPath(next));

    while(p.size() > 0)
    {
      current = next;
      next = p.remove(0);
//      path.addPathToHead(Path.straightLine(current.co.getX(), current.co.getY(), next.co.getX(),next.co.getY()));
      path.addPathToHead(current.getPath(next));
    }
    return path;
  }

  public Vertex randomUnexploredVertex()
  {
    return visited.remove(visited.size());
  }
}
