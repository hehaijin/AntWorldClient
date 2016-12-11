package antworld.client;

import antworld.common.CommData;
import antworld.common.LandType;
import antworld.common.NestData;
import antworld.common.NestNameEnum;

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
  OutputStreamWriter writer;

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
    System.out.println("MAP DONE");

  }

  public void write(CommData data)
  {
    System.out.println("WRITING");
    java.nio.file.Path out = Paths.get("waterLocations.txt");
    try {
      OutputStream write = Files.newOutputStream(out);
      this.writer = new OutputStreamWriter(write);

      for (int i = 0; i < waterLocations.size(); i++)
      {
        writer.write(String.valueOf(i));
        writer.write(" ");
        writer.write(String.valueOf(data.nestData[i].centerX));
        writer.write(" ");
        writer.write(String.valueOf(data.nestData[i].centerY));
        writer.write(" ");
        writer.write(String.valueOf(waterLocations.get(i).getX()));
        writer.write(" ");
        writer.write(String.valueOf(waterLocations.get(i).getY()));
        writer.write("\n");
      }
      writer.close();
    } catch (IOException e )
    {
      System.out.println(e);
      System.exit(-1);
    }
  }

  public ArrayList<Coordinate> reduce(CommData data, ArrayList<Coordinate> water)
  {
    int minDist;
    Coordinate[] temp = new Coordinate[data.nestData.length];
    ArrayList<Coordinate> nearestSources = new ArrayList<>(data.nestData.length);

    for(NestData nest : data.nestData)
    {
      minDist = 10000;
      for(Coordinate c : water)
      {
        if(Coordinate.manhattanDistance(c.getX(),c.getY(),nest.centerX, nest.centerY) < minDist)
        {
          minDist = Coordinate.linearDistance(c, new Coordinate(nest.centerX, nest.centerY));
          temp[nest.nestName.ordinal()] = c;
        }
      }
    }

    for(int i = 0; i < data.nestData.length; i++)
    {
      nearestSources.add(i, temp[i]);
    }
    System.out.println(nearestSources.size());
    return nearestSources;
  }

  private Coordinate waterLocation(NestNameEnum myNest)
  {
    String line;
    String[] split;
    Path sp;
    int nestNum;

    try
    {
      BufferedReader read = new BufferedReader(Files.newBufferedReader(Paths.get("waterLocations.txt")));
      while((line = read.readLine()) != null)
      {
        split = line.split(" ");
        nestNum = Integer.valueOf(split[0]);
        if(nestNum == myNest.ordinal());
        {
          int x = Integer.valueOf(split[3]);
          int y = Integer.valueOf(split[4]);
          return new Coordinate(x,y);
        }
      }
    }
    catch(IOException e)
    {
      System.out.println(e);
    }
    return null;
  }

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
//    water.waterLocations = water.reduce();
//    water.write();
  }
}
