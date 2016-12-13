package antworld.client;

import antworld.common.AntAction;
import antworld.common.AntData;
import antworld.common.CommData;
import antworld.common.Direction;

/**
 * 
 * We can put some constants here for tweaking the performance of AI. 
 *
 */
public class AIconstants
{
  public static final boolean aggresive=true; // 0 means peaceful.

  public static final int antsForWater=5;
  
  public static final int groupRadius=40;

  public static final int ThreadPoolSize=10;
  
  public static final int BLOCK_SIZE = 30;

  public static final int ANT_OUT_RATE = 6;
  public static final int ANT_OUT_TICK = 50;

  public static final int MIN_ANT_NEST_SIZE = 150;

  public static final int ANTSPERPILE = 15;
  public static final int antsPerFoodPile= 15;

}


//  private boolean goExplore(CommData data, AntData ant, AntAction action)
//  {
//    if(((alltasks.get(ant.id) == Task.EXPLORE) && !pathIsBeingExplored.get(ant.id) && (allpaths.get(ant.id) == null || allpaths.get(ant.id).size() == 0)))
//    {
//      freeAnts.remove(ant.id);
//
//      alltasks.put(ant.id, Task.EXPLORE);
//
//      pathIsBeingExplored.put(ant.id, Boolean.TRUE);
//
//      ExplorationManager.Vertex v = explore.getUnexploredVertex(centerX, centerY);
//
//      pool.submit(new ClientRandomWalk.Explorer(ant, v));
//    }
//
//    if(allpaths.get(ant.id) != null && allpaths.get(ant.id).size() != 0)
//    {
//      Direction dir = allpaths.get(ant.id).getNext();
//      Direction truDir =Coordinate.generalDir(dir);
//
//      lastMove.put(ant.id, dir);
//
//      action.type = AntAction.AntActionType.MOVE;
//      action.direction = truDir;
//    }
//    else
//    {
//      action.type = AntAction.AntActionType.STASIS;
//    }
//    return true;
//  }