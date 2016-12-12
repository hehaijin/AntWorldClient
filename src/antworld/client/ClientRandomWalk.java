package antworld.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import antworld.common.*;
import antworld.common.AntAction.AntActionType;

public class ClientRandomWalk
{
  private static final boolean DEBUG = false;
  private final TeamNameEnum myTeam;
  private static final long password = 962740848319L;//Each team has been assigned a random password.
  private ObjectInputStream inputStream = null;
  private ObjectOutputStream outputStream = null;
  private boolean isConnected = false;
  private NestNameEnum myNestName = null;
  private int centerX, centerY;
  private ExplorationManager explore;
  private boolean firstRun = true;
  private int outTotal = 0;
  private int tick = 960;

  private Socket clientSocket;


  private Graph world=new Graph();
  private static ArrayList<Coordinate> foodLocations = new ArrayList<>();

  // TODO read in file and write here
  private ArrayList<Coordinate> waterLocations = new ArrayList<>(); // stores some water locations

  // TODO hashmaps are unsynchronized by nature, it's possible they need to be synchronized
  private ConcurrentHashMap<Integer, Boolean> pathIsBeingExplored = new ConcurrentHashMap<>();
  private HashMap<Integer, Boolean> attacked = new HashMap<>();

  private ArrayList<Path> foodPath = new ArrayList<>(); // stores paths to food from colony
  private ArrayList<Path> waterPath = new ArrayList<>(); // stores paths to water from colony
  private ConcurrentHashMap<Integer,Path> allpaths=new ConcurrentHashMap<>(); // for storing shortest path.
  private HashMap<Integer, Direction> lastMove = new HashMap<>();
  private HashMap<Integer,AntAction> allactions=new HashMap<>(); //used to check if current action is successful
  private HashMap<Integer,Task> alltasks=new HashMap<>();
  private HashSet<Integer> antsForWater=new HashSet<>();
  private HashMap<FoodData,ArrayList<Integer>> foodSiteAnts=new HashMap<>();
  private HashSet<Integer> freeAnts=new HashSet<>();

// merge this
  private CommData previousData;

  //A random number generator is created in Constants. Use it.
  //Do not create a new generator every time you want a random number nor
  //  even in every class were you want a generator.
  //
  public static Random random = Constants.random;
  
  
//Thread pool for calculating shortest path.
  ExecutorService pool=Executors.newFixedThreadPool(8);
  
  class SPCalculator implements Runnable
  {
    ArrayList<AntData> ants;
    int startx;
    int starty;
    int endx;
    int endy;
    

    public SPCalculator(ArrayList<AntData> ants, int startx, int starty,int endx, int endy)
    {
      this.ants=ants;
      this.startx=startx;
      this.starty=starty;
      this.endx=endx;
      this.endy=endy;
    }
    
    
    @Override
    public void run()
    {
      // TODO Auto-generated method stub
      Path p=world.findPath(startx, starty,endx,endy);
      for(AntData ant: ants)
      {
       Path p1=allpaths.get(ant.id);;
       p1.addPathToHead(p1);  
      }

    }
  }

  class Explorer implements Runnable
  {
    ExplorationManager.Vertex goTo;
    AntData ant;

    public Explorer(AntData ant, ExplorationManager.Vertex v)
    {
      this.ant = ant;
      goTo = v;
    }

    @Override
    public void run()
    {
      System.out.println("ANT ID: " + ant.id + " GO TO : " + goTo.co.getX() + " " + goTo.co.getY());
      Path p = explore.genPath(new Coordinate(ant.gridX,ant.gridY), goTo);

      allpaths.put(ant.id, p);

      pathIsBeingExplored.replace(ant.id, Boolean.FALSE);
    }
  }

  public ClientRandomWalk(String host, int portNumber, TeamNameEnum team)
  {
    myTeam = team;
    System.out.println("Starting " + team +" on " + host + ":" + portNumber + " at "
      + System.currentTimeMillis());
    explore = new ExplorationManager(world);

    isConnected = openConnection(host, portNumber);
    if (!isConnected) System.exit(0);

    // TODO perform this on separate thread?

    CommData data = obtainNest();

    // for detecting nearest water locations, should not be runned most of the time
//    WaterLocations water = new WaterLocations();
//    water.findWater(water.world);
//    water.waterLocations = water.reduce(data, water.waterLocations);
//    water.write(data);

    mainGameLoop(data);
    closeAll();
  }

  private boolean openConnection(String host, int portNumber)
  {
    try
    {
      clientSocket = new Socket(host, portNumber);
    }
    catch (UnknownHostException e)
    {
      System.err.println("antworld.client.ClientRandomWalk Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      System.err.println("antworld.client.ClientRandomWalk Error: Could not open connection to " + host + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }

    try
    {
      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      inputStream = new ObjectInputStream(clientSocket.getInputStream());

    }
    catch (IOException e)
    {
      System.err.println("antworld.client.ClientRandomWalk Error: Could not open i/o streams");
      e.printStackTrace();
      return false;
    }

    return true;

  }

  public void closeAll()
  {
    System.out.println("antworld.client.ClientRandomWalk.closeAll()");
    {
      try
      {
        if (outputStream != null) outputStream.close();
        if (inputStream != null) inputStream.close();
        clientSocket.close();
      }
      catch (IOException e)
      {
        System.err.println("antworld.client.ClientRandomWalk Error: Could not close");
        e.printStackTrace();
      }
    }
  }

  /**
   * This method is called ONCE after the socket has been opened.
   * The server assigns a nest to this client with an initial ant population.
   * @return a reusable CommData structure populated by the server.
   */
  public CommData obtainNest()
  {
      CommData data = new CommData(myTeam);
      data.password = password;

      if( sendCommData(data) )
      {
        try
        {
          if (DEBUG) System.out.println("antworld.client.ClientRandomWalk: listening to socket....");
          data = (CommData) inputStream.readObject();
          if (DEBUG) System.out.println("antworld.client.ClientRandomWalk: received <<<<<<<<<"+inputStream.available()+"<...\n" + data);
          
          if (data.errorMsg != null)
          {
            System.err.println("antworld.client.ClientRandomWalk***ERROR***: " + data.errorMsg);
            System.exit(0);
          }
        }
        catch (IOException e)
        {
          System.err.println("antworld.client.ClientRandomWalk***ERROR***: client read failed");
          e.printStackTrace();
          System.exit(0);
        }
        catch (ClassNotFoundException e)
        {
          System.err.println("antworld.client.ClientRandomWalk***ERROR***: client sent incorrect common format");
        }
      }
    if (data.myTeam != myTeam)
    {
      System.err.println("antworld.client.ClientRandomWalk***ERROR***: Server returned wrong team name: "+data.myTeam);
      System.exit(0);
    }
    if (data.myNest == null)
    {
      System.err.println("antworld.client.ClientRandomWalk***ERROR***: Server returned NULL nest");
      System.exit(0);
    }

    myNestName = data.myNest;
    centerX = data.nestData[myNestName.ordinal()].centerX;
    centerY = data.nestData[myNestName.ordinal()].centerY;
    System.out.println("antworld.client.ClientRandomWalk: ==== Nest Assigned ===>: " + myNestName);
    return data;
  }
    
  public void mainGameLoop(CommData data)
  {
    while (true)
    { 
      try
      {

        if (DEBUG) System.out.println("antworld.client.ClientRandomWalk: chooseActions: " + myNestName);

        findResources(data);
        chooseActionsOfAllAnts(data);

        CommData sendData = data.packageForSendToServer();
        
        if(DEBUG) System.out.println("antworld.client.ClientRandomWalk: Sending>>>>>>>: " + sendData);
        outputStream.writeObject(sendData);
        outputStream.flush();
        outputStream.reset();
       

        if (DEBUG) System.out.println("antworld.client.ClientRandomWalk: listening to socket....");
        CommData receivedData = (CommData) inputStream.readObject();
        if (DEBUG) System.out.println("antworld.client.ClientRandomWalk: received <<<<<<<<<"+inputStream.available()+"<...\n" + receivedData);
        data = receivedData;
  
        
        
        if ((myNestName == null) || (data.myTeam != myTeam))
        {
          System.err.println("antworld.client.ClientRandomWalk: !!!!ERROR!!!! " + myNestName);
        }


        previousData=data;//update previousData

      }
      catch (IOException e)
      {
        System.err.println("antworld.client.ClientRandomWalk***ERROR***: client read failed");
        e.printStackTrace();
        System.exit(0);

      }
      catch (ClassNotFoundException e)
      {
        System.err.println("ServerToClientConnection***ERROR***: client sent incorrect common format");
        e.printStackTrace();
        System.exit(0);
      }

    }


  }
  
  
  private boolean sendCommData(CommData data)
  {

    CommData sendData = data.packageForSendToServer();
    try
    {
      if (DEBUG) System.out.println("antworld.client.ClientRandomWalk.sendCommData(" + sendData +")");
      outputStream.writeObject(sendData);
      outputStream.flush();
      outputStream.reset();
    }
    catch (IOException e)
    {
      System.err.println("antworld.client.ClientRandomWalk***ERROR***: client read failed");
      e.printStackTrace();
      System.exit(0);
    }

    return true;
    
  }

  private void chooseActionsOfAllAnts(CommData commData)
  {
    outTotal = 0;

//    checkAndDispatchWaterAnts(commData);

    for (AntData ant : commData.myAntList)
    {
      AntAction action = chooseAction(commData, ant);
      ant.myAction = action;
    }
    tick++;
  }
  
  
  private void checkAndDispatchWaterAnts(CommData commData)
  {
    
    
    if(antsForWater.size()<AIconstants.antsForWater)
    {
      ArrayList<AntData> ants=getClosestFreeAnts(commData, new Coordinate(centerX, centerY), AIconstants.antsForWater-antsForWater.size());
   
      Coordinate waterlocation=getWaterLocationForNest(commData);

      for(AntData ant: ants)
      {
        alltasks.put(ant.id, Task.GOTOWATER);
        antsForWater.add(ant.id);
      }
      dispatchTo(ants, waterlocation);
    }
    
    
    
  }
  
  
  private Coordinate getWaterLocationForNest(CommData comdata) 
  {
    int nestid=comdata.myNest.ordinal();
    Coordinate co=null;
    Scanner sc=null;
    try{
    sc=new Scanner(new File("resources/waterLocations.txt"));
    }
    catch(FileNotFoundException e)
    {
      System.out.println("water location file not found");
    }
    while(sc.hasNextLine())
    {
      String s=sc.nextLine();
      String[] s1=s.split(" ");
      if(Integer.parseInt(s1[0])==nestid)
      {
        co=new Coordinate(Integer.parseInt(s1[3]), Integer.parseInt(s1[4]));
      }
      
      
    }
    System.out.println("the water location is "+ co.getX()+ " "+ co.getY());
    sc.close();    
    return co;
    
  }

  
  
  
  
  
  
  public void checkAndDispatchFoodAnts(CommData commData)
  {
    //dispatch food ants
    for(FoodData fd: foodSiteAnts.keySet())
    {
      if(foodSiteAnts.get(fd).size()< AIconstants.antsPerFoodPile)
      {
        Coordinate co=new Coordinate(fd.gridX, fd.gridY);
        ArrayList<AntData> ants=getClosestFreeAnts(commData, co, AIconstants.antsPerFoodPile-foodSiteAnts.get(fd).size());
        dispatchTo(ants, co);
        for(AntData ant: ants)
        {
          alltasks.put(ant.id, Task.GOTOFOOD);
        }     
      }
      
    }
    
    
    
  }
  
  
  
  

  /**
   * the function get an ant from the collection randomly, then the group around it within the radius
   * calculate a path for the group.
   * do it recursively.
   * @param ants
   * @param co1
   */
  
  private void dispatchTo(ArrayList<AntData> ants, Coordinate co1)
  {

    if(ants.size()==0) return;
    AntData ant0=ants.get(0);
    ArrayList<AntData> group=new ArrayList<>();
    Coordinate c0=new Coordinate(ant0.gridX, ant0.gridY);
    Iterator it=ants.iterator();
    while(it.hasNext())
    {
      AntData ant=(AntData) it.next();

      if(Coordinate.linearDistance(new Coordinate(ant.gridX, ant.gridY), c0)<= AIconstants.groupRadius)
      {
        group.add(ant);
        it.remove();
      }
    }

    for(AntData ant: group)
    {
      Path p=Path.straightLine(ant.gridX, ant.gridY, ant0.gridX, ant0.gridY);
      allpaths.put(ant.id, p);
    }
    
    pool.submit(new SPCalculator(group, ant0.gridX,ant0.gridY,co1.getX(),co1.getY()));
    
    
    //TODO here: add direct path to c0 for each ant in the group.
    // calculate the path from c0 to co1, when done, append it to the front of each path in group.
   
    dispatchTo(ants, co1);

  }
  
  /**
   * calculates n the most closet free ants to the specific coordinate based on direct distance
   * returns an ArrayList.
   * @param data
   * @param co
   * @param n
   * @return   n the most closet free ants
   */

  private ArrayList<AntData> getClosestFreeAnts(CommData data,Coordinate co, int n)
  {
    ArrayList<AntData> ants=new ArrayList<>();
    ArrayList<Integer> dist=new ArrayList<>();
    for(Integer id: freeAnts )
    {
      AntData ant=getAntbyId(data, id);
      dist.add(Coordinate.linearDistance(new Coordinate(ant.gridX, ant.gridY), co));
    }

    Collections.sort(dist);
   // System.out.println(dist);
    int range;
    if(dist.size()==0)
      range=0;
    else if(dist.size()< n)
      range=dist.get(dist.size()-1);
    else range=dist.get(n-1);

    int i=0;
    for(Integer id: freeAnts )
    {
      AntData ant=getAntbyId(data, id);
      if(Coordinate.linearDistance(new Coordinate(ant.gridX, ant.gridY), co)<= range && i<n)
      {
        ants.add(ant);
        i++;
        if(i==n) break;
      }
    }
     System.out.println("recruited "+ ants.size()+ " ants.");
    return ants;
  }




  /**
   * return an antData by id.
   * @param data
   * @param n
   * @return
   */
  private AntData getAntbyId(CommData data,int n)
  {
    for(AntData ant: data.myAntList)
    {
      if(ant.id==n)
        return ant;
    }
    return null;
  }

  private void detectAttacks(CommData data)
  {
    for(AntData enemy : data.enemyAntSet)
    {
      if(enemy.myAction.type == AntActionType.ATTACK)
      {
        int x = enemy.myAction.x;
        int y = enemy.myAction.y;
        boolean tempAttack = false;

        for(AntData ant : data.myAntList)
        {
          if(ant.gridX == x && ant.gridY == y)
          {
            attacked.replace(ant.id, true);
            tempAttack = true;
          }

          if(!tempAttack) attacked.replace(ant.id, false);
        }
      }
    }
  }

  private void findResources(CommData data)
  {
    Coordinate foodCoordinate;
    for(FoodData food : data.foodSet)
    {
      if(food.foodType != FoodType.WATER)
      {
        foodCoordinate = new Coordinate(food.gridX, food.gridY);
        if (!foodLocations.contains(foodCoordinate))
        {
          System.out.println("Food found");
          foodLocations.add(foodCoordinate);
          System.out.println("Generating Path");
          // generate path to food from colony
        }
      }
    }
  }


  //=============================================================================
  // This method sets the given action to EXIT_NEST if and only if the given
  //   ant is underground.
  // Returns true if an action was set. Otherwise returns false
  //=============================================================================

  /**
   *
   * Manages the actions of an ant inside the nest
   * @param data communication data
   * @param ant ant data
   * @param action action that the ant will perform (may or may not be set here)
   * @return
   */
  private boolean exitNest(CommData data, AntData ant, AntAction action)
  {
    if (ant.underground)
    {

      if(ant.health != ant.antType.getMaxHealth() && data.foodStockPile[FoodType.WATER.ordinal()] > 0)
      {
        action.type = AntActionType.HEAL;
        if(ant.antType.getMaxHealth()-ant.health < data.foodStockPile[FoodType.WATER.ordinal()])
        {
          action.quantity = ant.health;
        }
        // not enough water to heal completely
        else
        {
          action.quantity = data.foodStockPile[FoodType.WATER.ordinal()];
        }
        return true;
      }
      if(ant.carryUnits != 0)
      {
        action.type = AntActionType.DROP;
        action.quantity = ant.carryUnits;
        return true;
      }
      // if it has finished all the things above, it is will come out

      if(outTotal != AIconstants.ANT_OUT_RATE && (tick % AIconstants.ANT_OUT_TICK == 0))
      {
        outTotal++;
        alltasks.put(ant.id, Task.GOTOWATER);
        freeAnts.add(ant.id);
        action.type = AntActionType.EXIT_NEST;
        action.x = centerX - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
        action.y = centerY - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
        return true;
      }
      else
      {
        action.type = AntActionType.STASIS;
        return true;
      }
    }
    return false;
  }


  /**
   * Determines if the ant will attack. Worth noting that it does not take into account the number of enemies.
   * If there are too many enemies the ant should know before hand. This only considers if there is an enemy adjacent.
   * @param data data containing nearby enemies.
   * @param ant the current ant that needs to make a decision
   * @param action action that the ant should take, might not be determined here
   * @return true if ant decision was made here
   */
  private boolean attackAdjacent(CommData data, AntData ant, AntAction action)
  {
    // use these values often and makes code more readable
    AntType antType = ant.antType;
    AntType enemyType;
    boolean weakAnt;
    boolean enemyAdjacent; // true if the ant is adjacent, don't want to do the math twice
    boolean enemyWeakAnt;
    boolean hasAdvantage; // true if more health

    for(AntData enemy : data.enemyAntSet)
    {
      enemyType = enemy.antType;
      enemyAdjacent = false;
      enemyWeakAnt = true;
      weakAnt = true;
      hasAdvantage = false;

      if(ant.health > enemy.health) hasAdvantage = true;
      if (Math.abs(ant.gridX - enemy.gridX) <= 1 && Math.abs(ant.gridY - enemy.gridY) <= 1) enemyAdjacent = true;
      if(antType == AntType.ATTACK || antType == AntType.DEFENCE) weakAnt = false;
      if(enemyType == AntType.ATTACK || enemyType == AntType.DEFENCE) enemyWeakAnt = false;

      if (AIconstants.aggresive)
      {
        // enemy ant has to be next to our ant
        if (enemyAdjacent)
        {
          // if both ants are of the same level
          if (weakAnt)
          {
            if (enemyWeakAnt && hasAdvantage)
            {
              action.type = AntActionType.ATTACK;
              action.direction = Coordinate.getDirection(enemy.gridX - ant.gridX, enemy.gridY - ant.gridY);
              return true;
            } else return false;
          }
          // if the ant is attack or defense, attack enemy regardless of type
          if (!weakAnt && hasAdvantage)
          {
            action.type = AntActionType.ATTACK;
            action.direction = Coordinate.getDirection(ant.gridX - enemy.gridX, ant.gridY - enemy.gridY);
            return true;
          }
          break; // don't go through the rest
        }
      }
      else // not aggressive but will defend
      {
        // ant attacked one of our ants
        if(enemy.myAction.type == AntActionType.ATTACK &&
                enemy.myAction.x == ant.gridX && enemy.myAction.y == ant.gridY)
        {
          attacked.replace(ant.id, true);
          // defend itself if it has an advantage
          if (!weakAnt && hasAdvantage)
          {
            action.type = AntActionType.ATTACK;
            action.direction = Coordinate.getDirection(ant.gridX - enemy.gridX, ant.gridY - enemy.gridY);
            return true;
          }
        }
        attacked.replace(ant.id, false);
      }
    }
    return false;
  }

  // TODO might need a special flag to know if this ant is looking for food
  private boolean pickUpFoodAdjacent(CommData data, AntData ant, AntAction action)
  {
    FoodData foodData;
    for (FoodData food : data.foodSet)
    {
      foodData = food;
      int spaceleft = 0;
      // dont want to pick up water and a medic should only have water to be useful
      if(ant.antType != AntType.MEDIC)
      {
        // ant has no food or the ant has this type of food and space for it
        if (ant.carryType == null || (food.foodType == ant.carryType && ant.carryUnits < ant.antType.getCarryCapacity()))
        {
          if (Math.abs(ant.gridX - foodData.gridX) <= 1 && Math.abs(ant.gridY - foodData.gridY) <= 1)
          {
            action.type = AntActionType.PICKUP;
            action.direction = Coordinate.getDirection(foodData.gridX - ant.gridX, foodData.gridY - ant.gridY);

            // define amount to pickup
            spaceleft = ant.antType.getCarryCapacity() - ant.carryUnits;
            if (spaceleft > 0)
            {
              if (food.count >= spaceleft) action.quantity = spaceleft;
              else action.quantity = food.count;
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  /*
   this method should define make ant go home if attacked it comes after the attack adjacent method,
   so assume that it is not advantageous to attack if it reached this point, perhaps have an array
   keeping track of attacks
    */
  private boolean goHomeIfCarryingOrHurt(CommData data, AntData ant, AntAction action)
  {
    // TODO might need shortest path here, if carrying there should be a path already, if hurt might need to generate one
    if(attacked.get(ant.id))
    {
      // go home
      return true;
    }
    if(ant.health < ant.antType.getMaxHealth()/2)
    {
      // go home
      return true;
    }
    if(ant.carryType != null)
    {
      // go home
      return true;
    }
    return false;
  }

  /**
   * // TODO this does not work
   * @param data
   * @param ant
   * @param action
   * @return
   */
  private boolean pickUpWater(CommData data, AntData ant, AntAction action)
  {
    FoodData foodData;
    for (FoodData food : data.foodSet)
    {
      foodData = food;
      int spaceleft = 0;
      if(food.foodType == FoodType.WATER)
      {
        // ant has no water or the ant is carrying water and space for it
        if (ant.carryType == null || (FoodType.WATER == ant.carryType && ant.carryUnits < ant.antType.getCarryCapacity()))
        {
          if (Math.abs(ant.gridX - foodData.gridX) <= 1 && Math.abs(ant.gridY - foodData.gridY) <= 1)
          {
            action.type = AntActionType.PICKUP;
            action.direction = Coordinate.getDirection(foodData.gridX - ant.gridX, foodData.gridY - ant.gridY);

            // define amount to pickup
            spaceleft = ant.antType.getCarryCapacity() - ant.carryUnits;
            if (spaceleft > 0)
            {
              if (food.count >= spaceleft) action.quantity = spaceleft;
              else action.quantity = food.count;
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean goToEnemyAnt(CommData data, AntData ant, AntAction action)
  {
    // if not aggressive
    if(alltasks.get(ant.id)==Task.GOTOENIMYANT)
    {
      Direction d=allpaths.get(ant.id).getNext();
      action.direction=d;
      action.type=AntActionType.MOVE;
      return true;
    }

    return false;
  }

  private boolean goToFood(CommData data, AntData ant, AntAction action)
  {
    if(alltasks.get(ant.id)==Task.GOTOFOOD)
    {
      Direction d=allpaths.get(ant.id).getNext();
      action.direction=d;
      action.type=AntActionType.MOVE;
      return true;
    }

    return false;
  }

  private boolean goToGoodAnt(CommData data, AntData ant, AntAction action)
  {
    if(alltasks.get(ant.id)==Task.GOTOGOODANT)
    {
      Direction d=allpaths.get(ant.id).getNext();
      action.direction=d;
      action.type=AntActionType.MOVE;
      return true;
    }

    return false;
  }

  /**
   * Primitive exploration algorithm. Uses a semi random walk which uses a normal distribution of directions.
   * TODO logic for when obstacle is encountered.
   * @param data
   * @param ant
   * @param action
   * @return
   */
  private boolean goExplore(CommData data, AntData ant, AntAction action)
  {
    if(freeAnts.contains(ant.id) || ((alltasks.get(ant.id) == Task.EXPLORE)
            && !pathIsBeingExplored.get(ant.id) && (allpaths.get(ant.id) == null || allpaths.get(ant.id).size() == 0)))
    {
      freeAnts.remove(ant.id);

      alltasks.put(ant.id, Task.EXPLORE);

      pathIsBeingExplored.put(ant.id, Boolean.TRUE);

      ExplorationManager.Vertex v = explore.getUnexploredVertex();

      pool.submit(new Explorer(ant, v));
    }

    if(allpaths.get(ant.id) != null && allpaths.get(ant.id).size() != 0)
    {
      // collision handling
      if(ant.myAction.type == AntActionType.STASIS)
      {
        if(lastMove.get(ant.id) != null)
        {
          action.type = AntActionType.MOVE;
          action.direction = Coordinate.generalDir(lastMove.get(ant.id));
          System.out.println("b ");
          return true;
        }
      }

      Direction dir = allpaths.get(ant.id).getNext();
      Direction truDir =Coordinate.generalDir(dir);
      System.out.println("a " + truDir.ordinal() + " " );

      lastMove.put(ant.id, dir);

      action.type = AntActionType.MOVE;
      action.direction = truDir;
    }
    else
    {
      action.type = AntActionType.STASIS;
    }
    return true;
  }


  private AntAction chooseAction(CommData data, AntData ant)
  {
    AntAction action = new AntAction(AntActionType.STASIS);

    if (ant.ticksUntilNextAction > 0) return ant.myAction;

    if (exitNest(data, ant, action)) return action;

    if (attackAdjacent(data, ant, action)) return action;

    if (pickUpFoodAdjacent(data, ant, action)) return action;

//    if (goHomeIfCarryingOrHurt(data, ant, action)) return action;

    if (pickUpWater(data, ant, action)) return action;

    // only for enemy ants close to ant
    //
//    if (goToEnemyAnt(data, ant, action)) return action;
//
//    if (goToFood(data, ant, action)) return action;
//
//    if (goToGoodAnt(data, ant, action)) return action;

    if (goExplore(data, ant, action)) return action;

    return action;
  }



  private ArrayList<Coordinate> newFoundFoodSite(CommData data)
  {
    ArrayList<Coordinate> newfoodsite=new ArrayList<>();
    HashSet<FoodData> fdnew=data.foodSet;
    HashSet<FoodData> fdold=previousData.foodSet;
    for(FoodData fd: fdnew)
    {
      if(!fdold.contains(fd))
      {
        newfoodsite.add(new Coordinate(fd.gridX, fd.gridY));
      }
    }
    return newfoodsite;
  }






  /**
   * The last argument is taken as the host name.
   * The default host is localhost.
   * Also supports an optional option for the teamname.
   * The default teamname is TeamNameEnum.RANDOM_WALKERS.
   * @param args Array of command-line arguments.
   */
  public static void main(String[] args)
  {
    String serverHost = "localhost";
    if (args.length > 0) serverHost = args[args.length -1];

    TeamNameEnum team = TeamNameEnum.Hector_Haijin;
    if (args.length > 1)
    { team = TeamNameEnum.getTeamByString(args[0]);
    }

    new ClientRandomWalk(serverHost, Constants.PORT, team);
  }

}
