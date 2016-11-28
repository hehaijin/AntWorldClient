package antworld.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import antworld.common.LandType;



public class Graph
{
  private class Node
  {
    int x, y;
    int height;
    LandType landtype;
    int east,west,north,south,southwest,southeast,northeast,northwest;
   
    public Node(LandType landtype, int height, int x, int y)
    {
      this.landtype=landtype;
      this.height=height;
      this.x=x;
      this.y=y;
     
    }
    
    
  }
  
  int worldWidth=5000;
  int worldHeight=2500;
  Node[][] world=new Node[worldWidth][worldHeight];

  public Graph()
  {
    BufferedImage image=null;
    try{
     image=ImageIO.read(new File("resources/AntWorld.png"));
    }
    catch(IOException ie)
    {
      System.out.println("image load failure");
    }
    
    
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
       //   NestNameEnum nestName = NestNameEnum.values()[Nest.getNextID()];
        //  nestList.add(new Nest(nestName, x, y));
        }
        else if (rgb == 0xF0E68C)
        {
          landType = LandType.NEST;
        }
        else if (rgb == 0x1E90FF)
        {
          landType = LandType.WATER;
        }
        else
        { landType = LandType.GRASS;
          height=LandType.getMapHeight(rgb);
        }
      
        world[x][y] = new Node(landType, height, x, y);
      }
    }
    calculateEdges();
  }
  
  
  
  public void calculateEdges()
  {
    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {
  
        if(world[x][y].landtype==LandType.GRASS)
        {
          if(world[x+1][y].landtype==LandType.GRASS)
          {
            world[x][y].east=1;
          }
          if(world[x+1][y+1].landtype==LandType.GRASS)
          {
            world[x][y].southeast=1;
          }
          if(world[x+1][y-1].landtype==LandType.GRASS)
          {
            world[x][y].northeast=1;
          }
          if(world[x-1][y].landtype==LandType.GRASS)
          {
            world[x][y].west=1;
          }
          if(world[x-1][y-1].landtype==LandType.GRASS)
          {
            world[x][y].northwest=1;
          }
          
          if(world[x-1][y+1].landtype==LandType.GRASS)
          {
            world[x][y].southwest=1;
          }
          if(world[x][y+1].landtype==LandType.GRASS)
          {
            world[x][y].south=1;
          }
          if(world[x][y-1].landtype==LandType.GRASS)
          {
            world[x][y].north=1;
          }
        }
        
      }
    }
  }
  
  
  public void findPath(Node start,Node end)
  {
    int sx=start.x;
    int sy=start.y;
    int ex=end.x;
    int ey=end.y;
    
    
    
    
    
    
    
  }
  
  
  
  
  

  public static void main(String[] args)
  {
    // TODO Auto-generated method stub
    Graph g=new Graph();
    for (int x = 0; x < 5000; x++)
    {
      for (int y = 0; y < 2500; y++)
      {
        //if(g.world[x][y].landtype==LandType.NEST)
        //  System.out.println("there is a nest at "+ x+" "+ y);
      }
    }
   System.out.println(g.world[1000][1000].east+" "+g.world[1000][1000].west);
  }

}
