package antworld.client;
/**
 * 
 * We can put some constants here for tweaking the performance of AI. 
 *
 */
public class AIconstants
{
  public static final int antsPerFoodPile=10;
  public static final boolean aggresive=false; // 0 means peaceful.

  // I may or may not end up using these
  public static final int GENERAL_ANT_ATTACK_HEALTH_DIFF = 3;
  public static final int ATTACK_ANT_ATTACK_HEALTH_DIFF = 3;
  public static final int DEFENSE_ANT_ATTACK_HEALTH_DIFF = 3;

  public static final int CHANGE_DIR_TICK = 2000;
  public static final int MAX_COLLISIONS = 10;
  
  public static final int antsForWater=5;
  
  public static final int groupRadius=40;
  public static final int groupSize=3;
  
  public static final int ThreadPoolSize=10;
  
  public static final int BLOCK_SIZE = 30;
  public static final int antsforexploration = 50;

  public static final int ANT_OUT_RATE = 1;
  public static final int ANT_OUT_TICK = 40;
  public static final int EXIT_NEST_TIME = 100;
  
}
