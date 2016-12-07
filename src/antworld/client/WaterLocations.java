package antworld.client;

import antworld.common.LandType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * This is to pre-calculate all water locations.
 * TODO should I reduce the number of water locations to just the ones closest to each nest?
 * Created by Hector on 12/6/16.
 */
public class WaterLocations
{
  private class Node
  {
    int x, y;
    LandType landtype;

    public Node(LandType landtype, int x, int y)
    {
      this.landtype = landtype;
      this.x = x;
      this.y = y;

    }
  }

  int worldWidth = 5000;
  int worldHeight = 2500;
  BufferedImage image;
  Node[][] world = new Node[worldWidth][worldHeight];
  ArrayList<Coordinate> waterLocations;
  BufferedWriter writer;

  public WaterLocations()
  {
    // Image IO is Haijin's
    image = null;
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
        }

        world[x][y] = new Node(landType, x, y);
      }
    }
  }

  private void write()
  {
    java.nio.file.Path out = Paths.get("test.txt");
    try {
      OutputStream write = Files.newOutputStream(out);
      this.writer = new BufferedWriter(new OutputStreamWriter(write));
    } catch (IOException e )
    {
      System.out.println(e);
      System.exit(-1);
    }

    for(Coordinate c : waterLocations)
    {
      try{
        writer.write(String.valueOf(c.getX()));
        writer.write(" ");
        writer.write(String.valueOf(c.getY()));
        writer.write("\n");
      } catch (IOException e)
      {
        System.out.println(e);
        System.exit(-1);
      }
    }
  }

//  private ArrayList<Coordinate> reduce(ArrayList<Coordinate> water)
//  {
//    for(int i = 0; i < water)
//  }

  private boolean isGrass(Node[][] world, int x, int y)
  {
    if(x >= 5000 || x < 0 || y >= 2500 || y < 0) return false;
    if(world[x][y].landtype == LandType.GRASS) return true;
    return false;
  }

  public ArrayList<Coordinate> findWater(Node[][] world)
  {
    waterLocations = new ArrayList<>();

    //
    for (int x = 0; x < worldWidth; x++)
    {
      for (int y = 0; y < worldHeight; y++)
      {
        if(world[x][y].landtype == LandType.WATER)
        {
          // checks if this water tile is on the coast (reachable by ants)
          // this means the water has an adjacent grass tile
          if(isGrass(world, x+1, y+0)) waterLocations.add(new Coordinate(x+1,y));
          if(isGrass(world, x+0, y+1)) waterLocations.add(new Coordinate(x,y+1));
          if(isGrass(world, x+0, y-1)) waterLocations.add(new Coordinate(x,y-1));
          if(isGrass(world, x-1, y+0)) waterLocations.add(new Coordinate(x-1,y));
        }
      }
    }

//    ArrayList<Coordinate> reducedWaterLocations = reduce(waterLocations);
    return waterLocations;
  }

  public static void main(String[] args)
  {
    WaterLocations water = new WaterLocations();
    water.findWater(water.world);
    water.write();
  }
}
