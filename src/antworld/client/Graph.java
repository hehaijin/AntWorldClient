package antworld.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import antworld.common.LandType;

/**
 * 
 * the graph representing the whole map. includes private Node class as graph
 * node. use findPath() function to find the shortest path between 2 nodes by A
 * star. returns a Path object which is a wrapper on an Liskinedlist. use
 * getNext() to get next step in the path.
 */

public class Graph
{
  private class Node
  {
    int x, y;
    int height;
    LandType landtype;

    int[] connections = new int[9]; // for the edges.

    // TODO do we need this info?
    int cost_so_far; // cost from the source so far.
    int cost_estimate; // estimation for remaining cost to reach the
                       // destination.
    Node pre; // a pointer to previous Node in the path.
    short color; // color indicating the node is visited or not. 1 for not
               // visited. -1 for processed. 0 for in the frontier.

    public Node(LandType landtype, int height, int x, int y)
    {
      this.landtype = landtype;
      this.height = height;
      this.x = x;
      this.y = y;
    }
  }


  private class Nodeinfo
  {
    int x, y;
    int cost_so_far; // cost from the source so far.
    int cost_estimate; // estimation for remaining cost to reach the
                       // destination.
    Nodeinfo pre; // a pointer to previous Node in the path.
    int color; // color indicating the node is visited or not. 1 for not
               // visited. -1 for processed. 0 for in the frontier.

    Nodeinfo(int x, int y)
    {
      this.x=x;
      this.y=y;
    }

  }





  private static int worldWidth = 5000;
  private static int worldHeight = 2500;

  private static Node[][] world = new Node[worldWidth][worldHeight];



  public Graph()
  {
    BufferedImage image = null;
    try
    {
      image = ImageIO.read(new File("resources/AntWorld.png"));
    } catch (IOException ie)
    {
      System.out.println("image load failure");
    }
    System.out.println("loading the picture done");

    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {

        int rgb = (image.getRGB(x, y) & 0x00FFFFFF);
        LandType landType;
        int height = 0;
        if (rgb == 0x0)
        {
          landType = LandType.NEST;
          // NestNameEnum nestName = NestNameEnum.values()[Nest.getNextID()];
          // nestList.add(new Nest(nestName, x, y));
        } else if (rgb == 0xF0E68C)
        {
          landType = LandType.NEST;
        } else if (rgb == 0x1E90FF)
        {
          landType = LandType.WATER;
        } else
        {
          landType = LandType.GRASS;
          height = LandType.getMapHeight(rgb);
        }

        world[x][y] = new Node(landType, height, x, y);
      }
    }

    System.out.println("now calculating edges");
    calculateEdges();
  }

  public Node getNode(int x, int y)
  {
    if (x < 0 || x >= 5000 || y < 0 || y > 2500)
    {
      System.out.println("wrong input for node coordinates");
      return null;
    } else
      return world[x][y];
  }

  public void calculateEdges()
  {
    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {

        if (world[x][y].landtype == LandType.GRASS)
        {
          for (int i = 0; i < 9; i++)
          {
            int m = i / 3 - 1;
            int n = i % 3 - 1;
            if (world[x + m][y + n].landtype == LandType.GRASS && i != 4)
            {
              world[x][y].connections[i] = 1;
            }
            if(world[x+m][y+n].height-world[x][y].height>0)
            {
              world[x][y].connections[i]*= 5;// to account for uphill movement.
            }
          }
        }

      }
    }
  }

  public Path findPath(Node start, Node end)
  {

    long t1 = System.currentTimeMillis();
    int startx=start.x;
    int starty=start.y;
    int endx=end.x;
    int endy=end.y;

    Nodeinfo[][] runinfo=new Nodeinfo[worldWidth][worldHeight];

    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {
        runinfo[x][y]=new Nodeinfo(x,y);
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
    PriorityQueue<Nodeinfo> frontier = new PriorityQueue<>(100, new Comparator<Nodeinfo>()
    {

      @Override
      public int compare(Nodeinfo o1, Nodeinfo o2)
      {
        // TODO Auto-generated method stub
        return o1.cost_so_far + o1.cost_estimate - o2.cost_so_far - o2.cost_estimate;
      }
    });

    frontier.offer(runinfo[startx][starty]);

    System.out.println("start calculating path");
    loop: while (frontier.size() > 0)
    {
      Nodeinfo u = frontier.poll();

      for (int i = 0; i < 9; i++)
      {

        if (world[u.x][u.y].connections[i] > 0)
        {
          int m = i / 3 - 1;
          int n = i % 3 - 1;

          Nodeinfo v = runinfo[u.x + m][u.y + n];
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
    Nodeinfo p = runinfo[end.x][end.y];
    Nodeinfo pre = p.pre;
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

  public void initiateGraph()
  {

    // long t1=System.currentTimeMillis();
    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {
        world[x][y].cost_so_far = Integer.MAX_VALUE;
        world[x][y].cost_so_far = Integer.MAX_VALUE;
        world[x][y].color = 1;
        world[x][y].pre = null;
      }
    }
   // System.out.println("it takes "+ (System.currentTimeMillis()-t1)+"ms");

  }




 public Path findPath(int startx, int starty, int endx, int endy)
 {

   return findPath(getNode(startx,starty),getNode(endx,endy));

 }



  public int costestimate(Nodeinfo start, Nodeinfo end)
  {
    return Math.max(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
  }


  public static LandType getLandType(int x, int y) {return world[x][y].landtype;}



  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

    Graph g=new Graph();

//    Path p=g.findPath(300,300, 715,800);
//    while(p.hasNext())
//    {
//      System.out.println(p.getNext());
//    }

    ExplorationManager e = new ExplorationManager(g);
    e.genVertices();
  }

}