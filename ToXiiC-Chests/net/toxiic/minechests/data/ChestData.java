package net.toxiic.minechests.data;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class ChestData
{
  private UUID playerUUID = null;
  private int taskID = -1;
  
  public ChestData(UUID playerUUID)
  {
    this.playerUUID = playerUUID;
  }
  
  public void cancelTask()
  {
    if ((this.taskID != -1) && (Bukkit.getScheduler().isQueued(this.taskID))) {
      Bukkit.getScheduler().cancelTask(this.taskID);
    }
  }
  
  public Player getPlayer()
  {
    return this.playerUUID != null ? Bukkit.getPlayer(this.playerUUID) : null;
  }
  
  public String getPlayerName()
  {
    Player player = getPlayer();
    return player != null ? player.getName() : null;
  }
  
  public UUID getPlayerUUID()
  {
    return this.playerUUID;
  }
  
  public ChestData setTaskID(int taskID)
  {
    this.taskID = taskID;
    return this;
  }
}
