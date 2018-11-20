package net.toxiic.minechests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.toxiic.minechests.data.BlockData;
import net.toxiic.minechests.data.CommandData;
import net.toxiic.minechests.data.ItemData;
import net.toxiic.minechests.utils.FireworkManager;

public class MainChests
  extends JavaPlugin
{
  private static MainChests pluginInstance = null;
  private EventListener eventListener = null;
  public FireworkManager fireworkManager;
  public String crateTitle = "";
  public List<String> worlds = new ArrayList();
  public List<CommandData> commands = new ArrayList();
  public List<String> worldGuardRegions = new ArrayList();
  public List<BlockData> breakList = new ArrayList();
  public Map<ItemStack, Integer> rewardChances = new HashMap();
  public List<UUID> ignoreList = new ArrayList();
  public boolean useBreakList = false;
  public double spawnChance = 10.0D;
  public int maxRewards = 9;
  public int removeTime = 10;
  public boolean warnChat = true;
  public boolean warnHolograms = true;
  public List<String> hologramMessages = new ArrayList();
  public Permission permissionReload = null;
  public Permission permissionToggle = null;
  
  public void onEnable()
  {
    pluginInstance = this;
    this.fireworkManager = new FireworkManager();
    ConfigurationSerialization.registerClass(ItemData.class);
    ConfigurationSerialization.registerClass(CommandData.class);
    Lang.init(this);
    loadConfiguration();
    
    getCommand("chests").setExecutor(new CommandListener());
    
    this.eventListener = new EventListener();
    getServer().getPluginManager().registerEvents(this.eventListener, this);
    
    this.permissionReload = new Permission("toxiicchest.reload");
    this.permissionToggle = new Permission("toxiicchest.toggle");
    try
    {
      getServer().getPluginManager().addPermission(this.permissionReload);
      getServer().getPluginManager().addPermission(this.permissionToggle);
      for (int i = 100; i > 0; i--) {
        getServer().getPluginManager().addPermission(new Permission("toxiicchest.multiplier." + i));
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    getServer().getScheduler().runTaskTimer(this, new Runnable()
    {
      public void run() {}
    }, 10L, 10L);
  }
  
  public void onDisable()
  {
    getServer().getScheduler().cancelTasks(this);
    ConfigurationSerialization.unregisterClass(ItemData.class);
    ConfigurationSerialization.unregisterClass(CommandData.class);
    if (this.eventListener != null)
    {
      HandlerList.unregisterAll(this.eventListener);
      for (String playerName : this.eventListener.chestViewers)
      {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null)
        {
          for (CommandData commandData : this.commands) {
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
      for (Location chestLoc : this.eventListener.chestLocations.keySet()) {
        if ((chestLoc != null) && 
          (chestLoc.getBlock().getType() == Material.CHEST)) {
          chestLoc.getBlock().setType(Material.AIR);
        }
      }
      this.eventListener.chestLocations.clear();
      this.eventListener.chestLocations = null;
    }
    this.ignoreList.clear();
    this.ignoreList = null;
    
    getServer().getPluginManager().removePermission(this.permissionReload);
    getServer().getPluginManager().removePermission(this.permissionToggle);
    for (int i = 100; i > 0; i--) {
      getServer().getPluginManager().removePermission("toxiicchest.multiplier." + i);
    }
    this.eventListener = null;
    this.fireworkManager = null;
    this.permissionReload = null;
    this.permissionToggle = null;
    
    pluginInstance = null;
  }
  
  public void loadConfiguration()
  {
    getConfig().options().header("ToXiiCxChests configuration");
    getConfig().addDefault("Worlds", Arrays.asList(new String[] { "world" }));
    getConfig().addDefault("Crate title", "&6Crate");
    getConfig().addDefault("Spawn chance", Double.valueOf(10.0D));
    getConfig().addDefault("Max rewards", Integer.valueOf(9));
    getConfig().addDefault("Remove crate time", Integer.valueOf(10));
    Map<Integer, Map<String, Object>> defaultCommands = new HashMap();
    defaultCommands.put(Integer.valueOf(1), new CommandData(Arrays.asList(new String[] { "msg <player> &6Hope you liked your rewards! (if any)" }), 100).serialize());
    defaultCommands.put(Integer.valueOf(2), new CommandData(Arrays.asList(new String[] { "msg <player> &aYou were lucky and received 5 diamonds!", "give <player> 264 5" }), 10).serialize());
    if ((!getConfig().contains("Commands")) || (!getConfig().isConfigurationSection("Commands"))) {
      getConfig().set("Commands", defaultCommands);
    }
    getConfig().addDefault("Warnings.Chat", Boolean.valueOf(true));
    getConfig().addDefault("Warnings.Holograms", Boolean.valueOf(true));
    getConfig().addDefault("Hologram messages", Arrays.asList(new String[] { "&6Your reward chest!" }));
    getConfig().addDefault("Use break list", Boolean.valueOf(false));
    getConfig().addDefault("Break list", new ArrayList(Arrays.asList(new String[] { new BlockData(Material.LOG).toString(), new BlockData(Material.STONE).toString(), new BlockData(Material.COBBLESTONE).toString(), new BlockData(Material.IRON_ORE).toString(), new BlockData(Material.GOLD_ORE).toString() })));
    getConfig().addDefault("WorldGuard regions", new ArrayList(Arrays.asList(new String[] { "amine", "bmine", "cmine" })));
    getConfig().options().copyDefaults(true);
    getConfig().options().copyHeader(true);
    saveConfig();
    
    this.worlds = getConfig().getStringList("Worlds");
    this.crateTitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Crate title", "&6Crate"));
    this.spawnChance = getConfig().getDouble("Spawn chance", 10.0D);
    this.maxRewards = getConfig().getInt("Max rewards", 9);
    this.removeTime = getConfig().getInt("Remove crate time", 10);
    this.warnChat = getConfig().getBoolean("Warnings.Chat", true);
    this.warnHolograms = getConfig().getBoolean("Warnings.Holograms", true);
    this.useBreakList = getConfig().getBoolean("Use break list", false);
    
    this.commands = new ArrayList();
    Map<String, Object> objCommands = ItemData.getMap(getConfig().get("Commands", defaultCommands));
    CommandData commandData;
    for (Map.Entry<String, Object> commandEntry : objCommands.entrySet())
    {
      Map<String, Object> commandMap = ItemData.getMap(commandEntry.getValue());
      commandData = CommandData.deserialize(commandMap);
      if ((commandData != null) && (commandData.hasCommands())) {
        this.commands.add(commandData);
      }
    }
    this.hologramMessages = Lang.replaceChatColours(getConfig().getStringList("Hologram messages"));
    
    this.breakList = new ArrayList();
    List<String> strBreakList = getConfig().getStringList("Break list");
    for (String strBreak : strBreakList)
    {
      BlockData breakData = BlockData.fromString(strBreak);
      if (breakData != null) {
        this.breakList.add(breakData);
      }
    }
    this.worldGuardRegions = getConfig().getStringList("WorldGuard regions");
    
    loadRewards();
  }
  
  private void loadRewards()
  {
    getRewardsConfig().options().header("Rewards configuration");
    if (getRewardsConfig().getValues(false).isEmpty())
    {
      getRewardsConfig().addDefault(String.valueOf(1), new ItemData(ItemData.addEnchantment(new ItemStack(Material.IRON_HELMET, 1), Enchantment.OXYGEN, 1), 35).serialize());
      getRewardsConfig().addDefault(String.valueOf(2), new ItemData(ItemData.renameItem(ItemData.setLore(new ItemStack(Material.IRON_CHESTPLATE, 1), new String[] { "&6Strong chestplate." }), "&6Stolen Chestplate"), 10).serialize());
      getRewardsConfig().addDefault(String.valueOf(3), new ItemData(new ItemStack(Material.IRON_LEGGINGS, 1), 25).serialize());
      getRewardsConfig().addDefault(String.valueOf(4), new ItemData(new ItemStack(Material.IRON_BOOTS, 1), 50).serialize());
      getRewardsConfig().addDefault(String.valueOf(5), new ItemData(ItemData.renameItem(new ItemStack(Material.DIAMOND_PICKAXE, 1), "&6Stolen Pickaxe"), 100).serialize());
    }
    getRewardsConfig().options().copyDefaults(true);
    getRewardsConfig().options().copyHeader(true);
    saveRewardsConfig();
    
    this.rewardChances = new HashMap();
    for (Map.Entry<String, Object> configEntry : getRewardsConfig().getValues(false).entrySet())
    {
      ItemData itemData = ItemData.deserialize(ItemData.getMap(configEntry.getValue()));
      try
      {
        if ((itemData != null) && (itemData.hasItemStack())) {
          this.rewardChances.put(itemData.getItemStack(), Integer.valueOf(itemData.getChance()));
        } else {
          getLogger().warning("Could not register the reward '" + (String)configEntry.getKey() + "' because the item was null.");
        }
      }
      catch (Exception ex)
      {
        getLogger().log(Level.SEVERE, "Could not register the reward '" + (String)configEntry.getKey() + "'.", ex);
      }
    }
  }
  
  public EventListener getEventListener()
  {
    return this.eventListener;
  }
  
  public static MainChests getInstance()
  {
    return pluginInstance;
  }
  
  private File rewardsFile = null;
  private FileConfiguration rewardsConfig = null;
  
  public FileConfiguration getRewardsConfig()
  {
    if ((this.rewardsFile == null) || (this.rewardsConfig == null)) {
      reloadRewardsConfig();
    }
    return this.rewardsConfig;
  }
  
  public void reloadRewardsConfig()
  {
    if (this.rewardsFile == null) {
      this.rewardsFile = new File(getDataFolder(), "rewards.yml");
    }
    this.rewardsConfig = YamlConfiguration.loadConfiguration(this.rewardsFile);
  }
  
  public void saveRewardsConfig()
  {
    if ((this.rewardsFile == null) || (this.rewardsConfig == null)) {
      return;
    }
    try
    {
      this.rewardsConfig.save(this.rewardsFile);
    }
    catch (Exception localException) {}
  }
  
  public static boolean isByte(String strByte)
  {
    try
    {
      Byte.parseByte(strByte);
      return true;
    }
    catch (Exception localException) {}
    return false;
  }
  
  public static boolean isInteger(String strInt)
  {
    try
    {
      Integer.parseInt(strInt);
      return true;
    }
    catch (Exception localException) {}
    return false;
  }
  
  public static boolean isUUID(String strUUID)
  {
    try
    {
      return UUID.fromString(strUUID) != null;
    }
    catch (Exception localException) {}
    return true;
  }
}
