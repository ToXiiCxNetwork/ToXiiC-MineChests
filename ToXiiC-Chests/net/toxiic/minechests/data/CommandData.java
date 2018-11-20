package net.toxiic.minechests.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class CommandData
  implements ConfigurationSerializable
{
  private List<String> commands = new ArrayList();
  private int chance = 1;
  
  public CommandData(List<String> commands, int chance)
  {
    this.commands = commands;
    this.chance = chance;
  }
  
  public boolean canRun()
  {
    return (hasCommands()) && ((int)(Math.random() * 100.0D) < this.chance);
  }
  
  public boolean canRun(boolean doChance)
  {
    return doChance ? canRun() : hasCommands();
  }
  
  public int getChance()
  {
    return this.chance;
  }
  
  public List<String> getCommands()
  {
    return this.commands;
  }
  
  public boolean hasCommands()
  {
    return (this.commands != null) && (!this.commands.isEmpty());
  }
  
  public Map<String, Object> serialize()
  {
    Map<String, Object> commandMap = new HashMap();
    if (this.commands != null) {
      commandMap.put("Run", this.commands);
    }
    commandMap.put("Chance", Integer.valueOf(this.chance));
    return commandMap;
  }
  
  public static CommandData deserialize(Map<String, Object> commandMap)
  {
    if ((commandMap != null) && (!commandMap.isEmpty()))
    {
      List<String> runCommands = commandMap.containsKey("Run") ? (List)commandMap.get("Run") : null;
      int chance = commandMap.containsKey("Chance") ? Integer.parseInt(commandMap.get("Chance").toString()) : 1;
      if (chance <= 0) {
        chance = 1;
      } else if (chance > 100) {
        chance = 100;
      }
      return new CommandData(runCommands, chance);
    }
    return null;
  }
}
