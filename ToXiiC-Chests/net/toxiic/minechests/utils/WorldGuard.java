package net.toxiic.minechests.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class WorldGuard
{
  public static Object getWorldGuardPlugin()
  {
    return Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
  }
  
  public static boolean hasWorldGuard()
  {
    return Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard");
  }
}
