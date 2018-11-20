package net.toxiic.minechests;

import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import net.toxiic.minechests.data.CommandData;

public class CommandListener
  implements CommandExecutor
{
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if (cmd.getName().equals("chests"))
    {
      if (args.length == 1)
      {
        String strCommand = args[0];
        if (strCommand.equalsIgnoreCase("on"))
        {
          if ((sender instanceof Player))
          {
            Player player = (Player)sender;
            if (player.hasPermission(getPlugin().permissionToggle))
            {
              if (getPlugin().ignoreList.contains(player.getUniqueId()))
              {
                getPlugin().ignoreList.remove(player.getUniqueId());
                Lang.sendMessage(sender, Lang.COMMAND_TOGGLE_ON);
              }
              else
              {
                Lang.sendMessage(sender, Lang.COMMAND_TOGGLE_ALREADY_ON);
              }
            }
            else {
              Lang.sendMessage(sender, Lang.COMMAND_TOGGLE_NO_PERMISSION);
            }
          }
          else
          {
            sender.sendMessage(ChatColor.RED + "You must be a player to use that command!");
          }
        }
        else if (strCommand.equalsIgnoreCase("off"))
        {
          if ((sender instanceof Player))
          {
            Player player = (Player)sender;
            if (player.hasPermission(getPlugin().permissionToggle))
            {
              if (!getPlugin().ignoreList.contains(player.getUniqueId()))
              {
                getPlugin().ignoreList.add(player.getUniqueId());
                Lang.sendMessage(sender, Lang.COMMAND_TOGGLE_OFF);
              }
              else
              {
                Lang.sendMessage(sender, Lang.COMMAND_TOGGLE_ALREADY_OFF);
              }
            }
            else {
              Lang.sendMessage(sender, Lang.COMMAND_TOGGLE_NO_PERMISSION);
            }
          }
          else
          {
            sender.sendMessage(ChatColor.RED + "You must be a player to use that command!");
          }
        }
        else if (strCommand.equalsIgnoreCase("reload"))
        {
          if (sender.hasPermission(getPlugin().permissionReload))
          {
            HandlerList.unregisterAll(getPlugin().getEventListener());
            for (String playerName : getPlugin().getEventListener().chestViewers)
            {
              Player player = Bukkit.getPlayerExact(playerName);
              if (player != null)
              {
                for (CommandData commandData : getPlugin().commands) {
                  if (commandData.canRun()) {
                    for (String command : commandData.getCommands()) {
                      if (command != null) {
                        player.getServer().dispatchCommand(player.getServer().getConsoleSender(), command.replace("<player>", player.getName()));
                      }
                    }
                  }
                }
                player.closeInventory();
              }
            }
            for (Location chestLoc : getPlugin().getEventListener().chestLocations.keySet()) {
              if ((chestLoc != null) && 
                (chestLoc.getBlock().getType() == Material.CHEST)) {
                chestLoc.getBlock().setType(Material.AIR);
              }
            }
            getPlugin().getEventListener().chestLocations.clear();
            
            getPlugin().getServer().getPluginManager().registerEvents(getPlugin().getEventListener(), getPlugin());
            
            getPlugin().reloadConfig();
            getPlugin().reloadRewardsConfig();
            getPlugin().loadConfiguration();
            
            sender.sendMessage(ChatColor.GOLD + "Reloaded " + getPlugin().getDescription().getFullName() + ".");
          }
          else
          {
            Lang.sendMessage(sender, Lang.COMMAND_HELP);
          }
        }
        else {
          Lang.sendMessage(sender, Lang.COMMAND_HELP);
        }
      }
      else
      {
        Lang.sendMessage(sender, Lang.COMMAND_HELP);
      }
      return true;
    }
    return false;
  }
  
  private MainChests getPlugin()
  {
    return MainChests.getInstance();
  }
}
