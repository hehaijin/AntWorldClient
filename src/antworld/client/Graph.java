package antworld.client;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import javax.imageio.ImageIO;

import antworld.common.AntType;
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

    int cost_so_far; // cost from the source so far.
    int cost_estimate; // estimation for remaining cost to reach the
                       // destination.
    Node pre; // a pointer to previous Node in the path.
    int color; // color indicating the node is visited or not. 1 for not
               // visited. -1 for processed. 0 for in the frontier.

    public Node(LandType landtype, int height, int x, int y)
    {
      this.landtype = landtype;
      this.height = height;
      this.x = x;
      this.y = y;

    }
  }

  private static int worldWidth = 5000;
  private static int worldHeight = 2500;
  // public for collision detection
  private static Node[][] world = new Node[worldWidth][worldHeight];
  ArrayList<Coordinate> c = new ArrayList<>();

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

  public static LandType getLandType(int x, int y) {return world[x][y].landtype;}


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

    initiateGraph();
    start.color = 0;
    start.cost_so_far = 0;
    PriorityQueue<Node> frontier = new PriorityQueue<>(100, new Comparator<Node>()
    {

      @Override
      public int compare(Node o1, Node o2)
      {
        // TODO Auto-generated method stub
        return o1.cost_so_far + o1.cost_estimate - o2.cost_so_far - o2.cost_estimate;
      }
    });

    frontier.offer(start);

    System.out.println("start calculating path");
    loop: while (frontier.size() > 0)
    {
      Node u = frontier.poll();

      for (int i = 0; i < 9; i++)
      {

        if (u.connections[i] > 0)
        {
          int m = i / 3 - 1;
          int n = i % 3 - 1;

          Node v = world[u.x + m][u.y + n];
          if (v.color == 1)
          {
            v.color = 0;
            if (v.cost_so_far > u.cost_so_far + u.connections[i])
            {
              v.cost_so_far = u.cost_so_far + u.connections[i];
              v.cost_estimate = v.cost_so_far + costestimate(v, end);
            }
            v.pre = u;
            frontier.offer(v);
            // System.out.println("adding Node "+v.x+ " "+ v.y+ " to the
            // frountier");
          }
          if (v == end)
            break loop;
        }

      }

    }

    // adding nodes to the path
    Path path = new Path();
    Node p = end;
    Node pre = end.pre;
    path.add(Coordinate.getDirection(p.x - pre.x, p.y - pre.y));


    c.add(c.size(), new Coordinate(p.x, p.y));
    c.add(c.size(), new Coordinate(pre.x, pre.y));

    while (p.pre.pre != null)
    {
      p = pre;
      pre = p.pre;
      c.add(c.size() - 1, new Coordinate(p.x, p.y));
      c.add(c.size() - 1, new Coordinate(pre.x, pre.y));
      path.add(Coordinate.getDirection(p.x - pre.x, p.y - pre.y));
    }
    System.out.println("finding the path takes " + (System.currentTimeMillis() - t1) + "ms");

    drawPath();

    return path;

  }

  private void drawPath()
  {
    BufferedImage image = null;
    try
    {
      image = ImageIO.read(new File("resources/test.png"));
    } catch (IOException ie)
    {
      System.out.println("image load failure");
    }
    System.out.println("loading the picture done");

    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {
        if(c.contains(new Coordinate(x,y)))
        {
          image.setRGB(x,y,0xFF0000);
        }
      }
    }
    try
    {
      File outputfile = new File("saved.png");
      ImageIO.write(image, "png", outputfile);
    }
    catch(IOException e)
    {
      System.out.println(e);
    }

    System.out.println("now calculating edges");
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

  public int costestimate(Node start, Node end)
  {
    return Math.max(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
  }

  public static void main(String[] args)
  {
    // TODO Auto-generated method stub
    Graph g = new Graph();
    System.out.println(g.findPath(g.getNode(300, 300), g.getNode(4550, 2190)).size());
//    System.out.println(g.findPath(g.getNode(300, 300), g.getNode(500, 500)).size());
  }

}
