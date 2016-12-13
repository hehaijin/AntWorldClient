package antworld.client;

import antworld.common.*;

/**
 * manages ant birth randomly
 * Created by Hector on 12/12/16.
 */
public class BirthManager
{
  NestNameEnum nest;
  TeamNameEnum team;

  public BirthManager(CommData data)
  {
    nest = data.myNest;
    team = data.myTeam;
  }

  private AntType getAnt(int ordinal)
  {
    if(ordinal == 0) return AntType.ATTACK;
    if(ordinal == 1) return AntType.DEFENCE;
    if(ordinal == 2) return AntType.WORKER;
    if(ordinal == 3) return AntType.MEDIC;
    if(ordinal == 4) return AntType.VISION;
    if(ordinal == 5) return AntType.SPEED;
    return null;
  }


  public void birthAnts(CommData data)
  {
    if(data.myAntList.size() < AIconstants.MIN_ANT_NEST_SIZE)
    {
      AntData newAnt = new AntData(Constants.UNKNOWN_ANT_ID, getAnt(Constants.random.nextInt(6)), nest, team);
      data.myAntList.add(newAnt);
    }
  }
}
