package net.toxiic.minechests;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.toxiic.minechests.data.BlockData;
import net.toxiic.minechests.data.ChestData;
import net.toxiic.minechests.data.CommandData;
import net.toxiic.minechests.utils.FireworkManager;
import net.toxiic.minechests.utils.WorldGuard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class EventListener
  implements Listener
{
  private static final Random random = new Random();
  public Map<Location, ChestData> chestLocations = null;
  public List<String> chestViewers = new ArrayList();
  
  public EventListener()
  {
    this.chestLocations = new HashMap();
  }
  
  @EventHandler
  public void onPlayerMine(BlockBreakEvent event)
  {
    try
    {
      if (event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
        return;
      }
      if ((!getPlugin().ignoreList.contains(event.getPlayer().getUniqueId())) && (WorldGuard.hasWorldGuard()))
      {
        if (this.chestLocations.containsKey(event.getBlock().getLocation()))
        {
          event.setCancelled(true);
          if (event.getPlayer().getUniqueId().equals(((ChestData)this.chestLocations.get(event.getBlock().getLocation())).getPlayerUUID()))
          {
            if ((event.getBlock().getState() instanceof Chest))
            {
              Chest chest = (Chest)event.getBlock().getState();
              chest.getInventory().clear();
              chest.update();
            }
            event.getBlock().setType(Material.AIR);
            this.chestLocations.remove(event.getBlock().getLocation());
          }
          else
          {
            Lang.sendMessage(event.getPlayer(), Lang.NOT_CRATE_OWNER);
          }
          return;
        }
        if ((getPlugin().worlds.isEmpty()) || (getPlugin().worlds.contains(event.getBlock().getWorld().getName())))
        {
          if (getPlugin().useBreakList)
          {
            boolean isRewardable = false;
            for (BlockData breakableBlock : getPlugin().breakList) {
              if (breakableBlock.isBlock(event.getBlock())) {
                isRewardable = true;
              }
            }
            if (!isRewardable) {
              return;
            }
          }
          double chance = getPlugin().spawnChance;
          for (int i = 100; i > 0; i--) {
            if (event.getPlayer().hasPermission("novachest.multiplier." + i))
            {
              chance *= i;
              break;
            }
          }
          if ((int)(Math.random() * 100.0D) < chance)
          {
            WorldGuardPlugin worldGuardPlugin = (WorldGuardPlugin)WorldGuard.getWorldGuardPlugin();
            RegionManager regionManager = worldGuardPlugin.getRegionManager(event.getBlock().getWorld());
            if (regionManager != null)
            {
              ApplicableRegionSet blockRegions = regionManager.getApplicableRegions(event.getBlock().getLocation());
              if (blockRegions != null)
              {
                LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(event.getPlayer());
                if ((localPlayer == null) || (blockRegions.canBuild(localPlayer)))
                {
                  boolean inRegion = getPlugin().worldGuardRegions.contains("__global__");
                  if (!inRegion) {
                    for (ProtectedRegion blockRegion : blockRegions) {
                      if ((blockRegion != null) && 
                        (getPlugin().worldGuardRegions.contains(blockRegion.getId())))
                      {
                        inRegion = true;
                        break;
                      }
                    }
                  }
                  if (inRegion)
                  {
                    ChestData chestData = new ChestData(event.getPlayer().getUniqueId());
                    this.chestLocations.put(event.getBlock().getLocation(), chestData);
                    
                    event.setCancelled(true);
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                    {
                      event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInHand());
                    }
                    else
                    {
                      event.getBlock().setType(Material.AIR);
                    }
                    event.getBlock().setType(Material.CHEST);
                    if ((event.getBlock().getState() instanceof Chest))
                    {
                      Chest chest = (Chest)event.getBlock().getState();
                      if ((chest.getInventory() instanceof DoubleChestInventory))
                      {
                        event.getBlock().setType(Material.AIR);
                        this.chestLocations.remove(event.getBlock().getLocation());
                      }
                      else
                      {
                        if (getPlugin().warnChat) {
                          Lang.sendMessage(event.getPlayer(), Lang.CRATE_SPAWNED);
                        }
                        final Location chestLoc = event.getBlock().getLocation();
                        if (event.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR)
                        {
                          Location aboveLoc = event.getBlock().getRelative(BlockFace.UP).getLocation().clone().add(0.0D, 0.5D, 0.0D);
                          event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ANVIL_LAND, 5.0F, 1.0F);
                          getPlugin().fireworkManager.playFirework(aboveLoc, FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.AQUA, Color.LIME, Color.GREEN, Color.PURPLE, Color.ORANGE, Color.YELLOW }).withFade(new Color[] { Color.RED, Color.GREEN, Color.BLUE }).build());
                        }
                        int amountOfRewards = 0;
                        for (Map.Entry<ItemStack, Integer> rewardEntry : getPlugin().rewardChances.entrySet()) {
                          if (rewardEntry.getKey() != null)
                          {
                            if ((int)(Math.random() * 100.0D) < ((Integer)rewardEntry.getValue()).intValue() * 1)
                            {
                              chest.getInventory().addItem(new ItemStack[] { (ItemStack)rewardEntry.getKey() });
                              amountOfRewards++;
                            }
                            if (amountOfRewards >= getPlugin().maxRewards) {
                              break;
                            }
                          }
                        }
                        Player player = event.getPlayer();
                        this.chestLocations.put(chestLoc, chestData.setTaskID(player.getServer().getScheduler().runTaskLater(getPlugin(), new Runnable()
                        {
                          public void run()
                          {
                            EventListener.this.chestLocations.remove(chestLoc);
                            if ((chestLoc.getBlock().getState() instanceof Chest))
                            {
                              Chest chest = (Chest)chestLoc.getBlock().getState();
                              chest.getInventory().clear();
                              chest.update();
                            }
                            chestLoc.getBlock().setType(Material.AIR);
                          }
                        }, getPlugin().removeTime * 20L).getTaskId()));
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  @EventHandler
  public void onPlayerOpenChest(PlayerInteractEvent event)
  {
    try
    {
      if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && 
        (event.getClickedBlock() != null) && ((event.getClickedBlock().getState() instanceof Chest)) && 
        (this.chestLocations.containsKey(event.getClickedBlock().getLocation())))
      {
        event.setCancelled(true);
        ChestData chestData = (ChestData)this.chestLocations.get(event.getClickedBlock().getLocation());
        if (event.getPlayer().getUniqueId().equals(chestData.getPlayerUUID()))
        {
          if (!this.chestViewers.contains(event.getPlayer().getName()))
          {
            Chest chest = (Chest)event.getClickedBlock().getState();
            Inventory chestInv = event.getPlayer().getServer().createInventory(event.getPlayer(), 36, getPlugin().crateTitle);
            chestInv.setContents(chest.getInventory().getContents());
            this.chestViewers.add(event.getPlayer().getName());
            chestData.cancelTask();
            event.getPlayer().openInventory(chestInv);
            
            chest.getInventory().clear();
            event.getClickedBlock().setType(Material.AIR);
            this.chestLocations.remove(event.getClickedBlock().getLocation());
          }
        }
        else {
          Lang.sendMessage(event.getPlayer(), Lang.NOT_CRATE_OWNER);
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  @EventHandler
  public void onPlayerOpenInventory(InventoryOpenEvent event)
  {
    try
    {
      if (((event.getPlayer() instanceof Player)) && 
        (isCrate(event.getInventory())))
      {
        Player player = (Player)event.getPlayer();
        if ((event.getInventory().getHolder() != null) && ((event.getInventory().getHolder() instanceof Player)) && (((Player)event.getInventory().getHolder()).getUniqueId().equals(player.getUniqueId())))
        {
          if (isEmpty(event.getInventory().getContents())) {
            Lang.sendMessage(player, Lang.CRATE_NO_ITEMS);
          }
        }
        else
        {
          Lang.sendMessage(player, Lang.NOT_CRATE_OWNER);
          event.setCancelled(true);
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  @EventHandler
  public void onPlayerCloseInventory(InventoryCloseEvent event)
  {
    try
    {
      if (((event.getPlayer() instanceof Player)) && 
        (event.getInventory().getTitle().equals(getPlugin().crateTitle)))
      {
        Player player = (Player)event.getPlayer();
        if (((event.getInventory().getHolder() != null) && ((event.getInventory().getHolder() instanceof Player)) && (((Player)event.getInventory().getHolder()).getUniqueId().equals(player.getUniqueId()))) || ((event.getInventory().getViewers().size() == 1) && 
          (this.chestViewers.contains(event.getPlayer().getName()))))
        {
          Iterator localIterator2 = null;
          for (Iterator localIterator1 = getPlugin().commands.iterator(); localIterator1.hasNext(); localIterator2.hasNext())
          {
            CommandData commandData = (CommandData)localIterator1.next();
            if ((!commandData.canRun(false)) || ((int)(Math.random() * 100.0D) >= commandData.getChance())) {
              break;
            }
            String command = (String)localIterator2.next();
            localIterator2 = commandData.getCommands().iterator();
            if (command != null) {
              player.getServer().dispatchCommand(player.getServer().getConsoleSender(), command.replace("<player>", player.getName()));
            }
          }
          this.chestViewers.remove(event.getPlayer().getName());
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private MainChests getPlugin()
  {
    return MainChests.getInstance();
  }
  
  public static boolean isCrate(Inventory inventory)
  {
    if (inventory != null) {
      return isCrate(inventory.getTitle());
    }
    return false;
  }
  
  public static boolean isCrate(String invTitle)
  {
    if (invTitle != null) {
      invTitle = ChatColor.stripColor(invTitle);
    }
    return (invTitle != null) && (invTitle.equals(MainChests.getInstance().crateTitle));
  }
  
  public static boolean isEmpty(ItemStack[] items)
  {
    if ((items != null) && 
      (items.length > 0))
    {
      ItemStack[] arrayOfItemStack = items;int j = items.length;
      for (int i = 0; i < j; i++)
      {
        ItemStack item = arrayOfItemStack[i];
        if ((item != null) && (item.getType() != Material.AIR)) {
          return false;
        }
      }
    }
    return true;
  }
}
