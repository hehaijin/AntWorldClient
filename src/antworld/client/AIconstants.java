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

  public static final int ANT_OUT_RATE = 10;
  public static final int ANT_OUT_TICK = 40;

  public static final int MIN_ANT_NEST_SIZE = 150;

  public static final int ANTSPERPILE = 15;
  public static final int antsPerFoodPile= 15;

}