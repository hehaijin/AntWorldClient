package antworld.client;

import antworld.common.*;

import java.util.*;

/**
 * This class creates normal distributions for moving in a semi-random direction.
 * Created by Hector on 12/7/16.
 */
public class ExplorationManager
{
  public class Vertex
  {
    Coordinate co;
    boolean visited = false;
    short color = 1;
    ArrayList<Vertex> adjacent = new ArrayList<>();
//    HashMap<Vertex, Path> paths = new HashMap<>();
    int cost_so_far = Integer.MAX_VALUE;
    int cost_estimate = Integer.MAX_VALUE;
    int x;
    int y;
    Vertex pre = null;

    public Vertex(int x, int y)
    {
      co = new Coordinate(x,y);
      this.x = x/AIconstants.BLOCK_SIZE;
      this.y = y/AIconstants.BLOCK_SIZE;
    }

//    public Path getPath(Vertex v)
//    {
//      return this.paths.get(v);
//    }

    public int costestimate(Vertex v, Vertex u)
    {
      return Coordinate.manhattanDistance(v.x,v.y,u.x,u.y);
    }

    public Vertex copy()
    {
      Vertex copy = new Vertex(this.co.getX(), this.co.getY());
      copy.x = x/AIconstants.BLOCK_SIZE;
      copy.y = y/AIconstants.BLOCK_SIZE;
      copy.visited = this.visited;

      return copy;
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

  private ArrayList<Vertex> unvisited = new ArrayList<>();
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
          unvisited.add(vertices[col][row]);
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

//    Vertex v0;
//    for(int col = 0; col < xTotal; col++)
//    {
//      for (int row = 0; row < yTotal; row++)
//      {
//        v0 = vertices[col][row];
//        if(v0 != null)
//        {
//          for (Vertex v1 : v0.adjacent)
//          {
//            v0.paths.put(v1, Path.straightLine(v0.co.getX(), v0.co.getY(), v1.co.getX(), v1.co.getY()));
//          }
//        }
//      }
//    }
  }

  public void markVisited(int x, int y)
  {
    int row;
    int col;
    if((col = x % AIconstants.BLOCK_SIZE) == 0 && (row = y % AIconstants.BLOCK_SIZE) == 0)
    {
      vertices[col][row].visited = true;
      unvisited.remove(vertices[col][row]);
    }
  }

  public Vertex getUnexploredVertex()
  {
    return unvisited.get(Constants.random.nextInt(unvisited.size()));
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
    int x = c.getX();
    int y = c.getY();

    int col = x/AIconstants.BLOCK_SIZE;
    int row = y/AIconstants.BLOCK_SIZE;

//    System.out.println(col + " " + row);

    if(col >= 0 && row >= 0 && col < 5000/AIconstants.BLOCK_SIZE && row < 2500/AIconstants.BLOCK_SIZE)
    {
      if (vertices[col][row] != null) return vertices[col][row];
      else {System.out.println(col + " " + row + " is null");}
    }
    if(col+1 >= 0 && row+1 >= 0 && col+1 < 5000/AIconstants.BLOCK_SIZE && row+1 < 2500/AIconstants.BLOCK_SIZE)
    {
      if (vertices[col + 1][row + 1] != null) return vertices[col+1][row+1];
      else {System.out.println(col + " " + row + " is null");}

      }
    if(col+1 >= 0 && row >= 0 && col+1 < 5000/AIconstants.BLOCK_SIZE && row < 2500/AIconstants.BLOCK_SIZE)
    {
      if (vertices[col + 1][row] != null) return vertices[col+1][row];
      else {System.out.println(col + " " + row + " is null");}

      }
    if(col >= 0 && row+1 >= 0 && col < 5000/AIconstants.BLOCK_SIZE && row+1 < 2500/AIconstants.BLOCK_SIZE)
    {
      if (vertices[col][row + 1] != null) return vertices[col][row+1];
      else {System.out.println(col + " " + row + " is null");}

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
          vertices[col][row].color = 1;
          vertices[col][row].cost_estimate = Short.MAX_VALUE;
          vertices[col][row].cost_so_far = Short.MAX_VALUE;
        }
      }
    }
  }

  private Vertex[][] copy()
  {
    Vertex[][] temp = new Vertex[xTotal][yTotal];
    for(int col = 0; col < xTotal; col++)
    {
      for (int row = 0; row < yTotal; row++)
      {
        if (vertices[col][row] != null)
        {
          temp[col][row] = vertices[col][row].copy();
        }
      }
    }

    for(int col = 0; col < xTotal; col++)
    {
      for (int row = 0; row < yTotal; row++)
      {
        if(vertices[col][row] != null)
        {
          for(Vertex v : vertices[col][row].adjacent)
          {
            temp[col][row].adjacent.add(temp[v.x][v.y]);
          }
        }
      }
    }
    return temp;
  }

  private LinkedList<Vertex> vertexList(Vertex start, Vertex end)
  {
    int startx = start.co.getX()/AIconstants.BLOCK_SIZE;
    int starty = start.co.getY()/AIconstants.BLOCK_SIZE;
    int endx = end.co.getX()/AIconstants.BLOCK_SIZE;
    int endy = end.co.getY()/AIconstants.BLOCK_SIZE;

    Vertex[][] temp = copy();

//    reset();

    // TODO: might need to check if enough connections

    temp[startx][starty].color = 0;
    temp[startx][starty].cost_so_far = 0;

    PriorityQueue<Vertex> frontier = new PriorityQueue<>(100, new Comparator<Vertex>()
    {
      @Override
      public int compare(Vertex o1, Vertex o2)
      {
        return o1.cost_so_far + o1.cost_estimate - o2.cost_so_far - o2.cost_estimate;
      }
    });

    frontier.offer(temp[startx][starty]);

//    System.out.println("start calculating path");
    loop: while (frontier.size() > 0)
    {
      Vertex u = frontier.poll();

      for (Vertex v : u.adjacent)
      {
        if (v.color == 1)
        {
          v.color = 0;
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
    LinkedList<Vertex> list = new LinkedList<>();
    Vertex p = temp[end.x][end.y];
    Vertex pre = p.pre;
    // path.add(Coordinate.getDirection(p.x - pre.x, p.y - pre.y));
    while (p.pre != null)
    {
      list.addFirst(p);
      p = pre;
      pre = p.pre;
    }
    System.out.println("Done calculating path");
    return list;
  }

  public Path genPath(Coordinate s, Vertex end)
  {
    long t1 = System.currentTimeMillis();

    Vertex start = findClosestVertex(s);
    if(start.visited == false)
    {
      unvisited.remove(start);
      start.visited = true;
    }

    LinkedList<Vertex> p = vertexList(start, end);


    Vertex current = start;
    Vertex next = p.removeFirst();
    if(next.visited == false)
    {
      unvisited.remove(next);
      next.visited = true;
    }

    Path path = Path.straightLine(s.getX(), s.getY(), start.co.getX(), start.co.getY());

    path.addPathToHead(Path.straightLine(current.co.getX(), current.co.getY(), next.co.getX(),next.co.getY()));

    System.out.println("size of path " + p.size());
    while(p.size() > 0)
    {
      current = next;
      next = p.removeFirst();
      if(next.visited == false)
      {
        unvisited.remove(next);
        next.visited = true;
      }
      path.addPathToHead(Path.straightLine(current.co.getX(), current.co.getY(), next.co.getX(),next.co.getY()));
    }
    System.out.println("finding the path takes " + (System.currentTimeMillis() - t1) + "ms");
    return path;
  }

  public static void main(String[] args)
  {
    Graph g = new Graph();
    ExplorationManager ex = new ExplorationManager(g);

//    Path p = ex.genPath(new Coordinate(900,2203), ex.vertices[126][9]);
    Path p = ex.genPath(new Coordinate(3407,2259), ex.vertices[67][22]);

    int size = p.size();
    System.out.println("Size : " + size);
    Direction d;
    for(int i = 0; i < size; i++)
    {
      d = p.getNext();
      System.out.print(d.ordinal());
    }
  }
}
