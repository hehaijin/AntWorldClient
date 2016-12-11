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
    ArrayList<Vertex> adjacent = new ArrayList<>();
    HashMap<Vertex, Path> paths = new HashMap<>();

    public Vertex(int x, int y)
    {
      co = new Coordinate(x,y);
    }
  }

  Graph graph;

//  private ArrayList<>

  private Vertex[][] vertices;

  public ExplorationManager(Graph graph)
  {
    this.graph = graph;
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
            v0.paths.put(v1, graph.findPath(v0.co.getX(), v0.co.getY(), v1.co.getX(), v1.co.getY()));
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

  public Vertex findClosestVertex(AntData ant)
  {
    int col = ant.gridX;
    int row = ant.gridY;

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

  public Path genPath(Vertex start, Vertex end)
  {
    long t1 = System.currentTimeMillis();
    int startx = start.co.getX();
    int starty = start.co.getY();
    int endx = end.co.getX();
    int endy = end.co.getY();

    Graph.Nodeinfo[][] runinfo=new Graph.Nodeinfo[worldWidth][worldHeight];

    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {
        runinfo[x][y]=new Graph.Nodeinfo(x,y);
        runinfo[x][y].cost_estimate = Integer.MAX_VALUE;
        runinfo[x][y].cost_so_far = Integer.MAX_VALUE;
        runinfo[x][y].color = 1;
        runinfo[x][y].pre = null;
      }
    }

    int s = 0;
    int t = 0;

    for (int i = 0; i < 9; i++)
    {
      if (start.connections[i] > 0)
        s++;
      if (end.connections[i] > 0)
        t++;

    }
    if (s == 0)
      System.out.println("no path from start");
    if (t == 0)
      System.out.println("no path to end");

    //  initiateGraph();
    runinfo[startx][starty].color = 0;
    runinfo[startx][starty].cost_so_far = 0;
    PriorityQueue<Graph.Nodeinfo> frontier = new PriorityQueue<>(100, new Comparator<Graph.Nodeinfo>()
    {

      @Override
      public int compare(Graph.Nodeinfo o1, Graph.Nodeinfo o2)
      {
        // TODO Auto-generated method stub
        return o1.cost_so_far + o1.cost_estimate - o2.cost_so_far - o2.cost_estimate;
      }
    });

    frontier.offer(runinfo[startx][starty]);

    System.out.println("start calculating path");
    loop: while (frontier.size() > 0)
    {
      Graph.Nodeinfo u = frontier.poll();

      for (int i = 0; i < 9; i++)
      {

        if (world[u.x][u.y].connections[i] > 0)
        {
          int m = i / 3 - 1;
          int n = i % 3 - 1;

          Graph.Nodeinfo v = runinfo[u.x + m][u.y + n];
          if (v.color == 1)
          {
            v.color = 0;
            if (v.cost_so_far > u.cost_so_far + world[u.x][u.y].connections[i])
            {
              v.cost_so_far = u.cost_so_far + world[u.x][u.y].connections[i];
              v.cost_estimate = v.cost_so_far + costestimate(v, runinfo[end.x][end.y]);
            }
            v.pre = u;
            frontier.offer(v);
            // System.out.println("adding Node "+v.x+ " "+ v.y+ " to the
            // frountier");
          }
          if (v.x == end.x && v.y==end.y)
            break loop;
        }

      }

    }

    // adding nodes to the path
    Path path = new Path();
    Graph.Nodeinfo p = runinfo[end.x][end.y];
    Graph.Nodeinfo pre = p.pre;
    // path.add(Coordinate.getDirection(p.x - pre.x, p.y - pre.y));
    while (p.pre != null)
    {
      path.add(Coordinate.getDirection(p.x - pre.x, p.y - pre.y));
      p = pre;
      pre = p.pre;

    }
    System.out.println("finding the path takes " + (System.currentTimeMillis() - t1) + "ms");
    return path;
  }
}
