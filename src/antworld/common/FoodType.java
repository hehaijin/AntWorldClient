package antworld.common;

public enum FoodType 
{
  WATER,
  NECTAR,
  SEEDS,
  MEAT;
  
  public static final int SIZE = values().length;
  public static int getColor() {return 0x7C00BA;}


  /**
   * @return random non-water food.
   */
  public static FoodType getRandomFood()
  {
    int idx = Constants.random.nextInt(SIZE-1)+1;
    return values()[idx];
  }
};  
