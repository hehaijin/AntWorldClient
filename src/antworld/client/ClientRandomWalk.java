package antworld.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import antworld.common.*;
import antworld.common.AntAction.AntActionType;
import antworld.client.Coordinate;

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

  private Socket clientSocket;

  /* I was thinking putting paramaters that the ants use to make decisions here
   * For example, one paramater I was thinking is enemy health. Depending of the health of the enemy
   * the ant can decide to attack or flee */

  private static final int GENERAL_ANT_ATTACK_HEALTH_DIFF = 3;
  private static final int ATTACK_ANT_ATTACK_HEALTH_DIFF = 3;
  private static final int DEFENSE_ANT_ATTACK_HEALTH_DIFF = 3;

  private static ArrayList<Coordinate> foodLocations = new ArrayList<>();



  //A random number generator is created in Constants. Use it.
  //Do not create a new generator every time you want a random number nor
  //  even in every class were you want a generator.
  //
  private static Random random = Constants.random;


  public ClientRandomWalk(String host, int portNumber, TeamNameEnum team)
  {
    myTeam = team;
    System.out.println("Starting " + team +" on " + host + ":" + portNumber + " at "
      + System.currentTimeMillis());

    isConnected = openConnection(host, portNumber);
    if (!isConnected) System.exit(0);
    CommData data = obtainNest();
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
    for (AntData ant : commData.myAntList)
    {
      AntAction action = chooseAction(commData, ant);
      ant.myAction = action;
    }
  }

  private void findResources(CommData data)
  {
    Coordinate foodCoordinate;
    for(FoodData food : data.foodSet)
    {
      foodCoordinate = new Coordinate(food.gridX, food.gridY);
      if(!foodLocations.contains(foodCoordinate))
      {
        foodLocations.add(foodCoordinate);
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
      action.type = AntActionType.EXIT_NEST;
      action.x = centerX - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
      action.y = centerY - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
      return true;
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
    AntType antType = ant.antType;
    AntType enemyType;
    for(AntData enemy : data.enemyAntSet)
    {
      enemyType = enemy.antType;
      // enemy ant has to be next to our ant
      if(Math.abs(ant.gridX - enemy.gridX) <= 1 && Math.abs(ant.gridY - enemy.gridY) <= 1)
      {
        // if this is the current ant
        if(antType == AntType.WORKER || antType == AntType.MEDIC
                || antType == AntType.SPEED || antType == AntType.VISION)
        {
          // if the enemy is one of these then attack
          if(enemyType == AntType.WORKER || enemyType == AntType.MEDIC
                  || enemyType == AntType.SPEED || enemyType == AntType.VISION)
          {
            action.type = AntActionType.ATTACK;
            action.direction = Coordinate.getDirection(enemy.gridX - ant.gridX, enemy.gridY - ant.gridY);
            return true;
          }
          else return false;
        }
        // if the ant is attack or defense attack enemy regardless of type
        if(antType == AntType.ATTACK || antType == AntType.DEFENCE)
        {
          action.type = AntActionType.ATTACK;
          action.direction = Coordinate.getDirection(ant.gridX - enemy.gridX, ant.gridY - enemy.gridY);
          return true;
        }
        break; // don't go through the rest
      }
    }
    return false;
  }

  private boolean pickUpFoodAdjacent(CommData data, AntData ant, AntAction action)
  {
    FoodData foodData;
    for (FoodData food : data.foodSet)
    {
      foodData = food;
      int spaceleft = 0;
      // ant has no food or the ant has this type of food and space for it
      if(ant.carryType == null || (food.foodType == ant.carryType && ant.carryUnits < ant.antType.getCarryCapacity()))
      {
        if (Math.abs(ant.gridX - foodData.gridX) <= 1 && Math.abs(ant.gridY - foodData.gridY) <= 1)
        {
          action.type = AntActionType.PICKUP;
          action.direction = Coordinate.getDirection(foodData.gridX - ant.gridX, foodData.gridY - ant.gridY);

          // define amount to pickup
          spaceleft = ant.antType.getCarryCapacity() - ant.carryUnits;
          if(spaceleft > 0)
          {
            if(food.count >= spaceleft) action.quantity = spaceleft;
            else action.quantity = food.count;
          }
          return true;
        }
      }
    }
    return false;
  }

  private boolean goHomeIfCarryingOrHurt(CommData data, AntData ant, AntAction action)
  {
    return false;
  }

  private boolean pickUpWater(CommData data, AntData ant, AntAction action)
  {
    return false;
  }

  private boolean goToEnemyAnt(CommData data, AntData ant, AntAction action)
  {
    return false;
  }

  private boolean goToFood(CommData data, AntData ant, AntAction action)
  {
    return false;
  }

  private boolean goToGoodAnt(CommData data, AntData ant, AntAction action)
  {
    return false;
  }

  private boolean goExplore(CommData data, AntData ant, AntAction action)
  {
    Direction dir = Direction.getRandomDir();
    action.type = AntActionType.MOVE;
    action.direction = dir;
    return true;
  }


  private AntAction chooseAction(CommData data, AntData ant)
  {
    AntAction action = new AntAction(AntActionType.STASIS);

    if (ant.ticksUntilNextAction > 0) return ant.myAction;

    if (exitNest(data, ant, action)) return action;

    if (attackAdjacent(data, ant, action)) return action;

    if (pickUpFoodAdjacent(data, ant, action)) return action;

    if (goHomeIfCarryingOrHurt(data, ant, action)) return action;

    if (pickUpWater(data, ant, action)) return action;

    if (goToEnemyAnt(data, ant, action)) return action;

    if (goToFood(data, ant, action)) return action;

    if (goToGoodAnt(data, ant, action)) return action;

    if (goExplore(data, ant, action)) return action;

    return action;
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

    TeamNameEnum team = TeamNameEnum.RANDOM_WALKERS;
    if (args.length > 1)
    { team = TeamNameEnum.getTeamByString(args[0]);
    }

    new ClientRandomWalk(serverHost, Constants.PORT, team);
  }

}
