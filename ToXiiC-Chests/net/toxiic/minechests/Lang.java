package net.toxiic.minechests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public enum Lang
{
  CRATE_SPAWNED("Crate spawned", "&6You were lucky and received a crate box!"),  CRATE_NO_ITEMS("No items", "&7Unfortunately there were no rewards in the crate."),  NOT_CRATE_OWNER("Not crate owner", "&cThat crate is not yours!"),  COMMAND_HELP("Command.Help", "&6ToXiiC Chests by ToXiiCxMonster"),  COMMAND_TOGGLE_NO_PERMISSION("Command.Toggle.No permission", "&4You do not access to that command."),  COMMAND_TOGGLE_ON("Command.Toggle.On", "&6You can now find reward chests!"),  COMMAND_TOGGLE_ALREADY_ON("Command.Toggle.On already", "&cYou can already find reward chests!"),  COMMAND_TOGGLE_OFF("Command.Toggle.Off", "&6You can no longer find reward chests."),  COMMAND_TOGGLE_ALREADY_OFF("Command.Toggle.Off already", "&cYou already can't find reward chests!");
  
  private static YamlConfiguration config = null;
  private static File configFile = null;
  private String key = "";
  private String defaultValue = "";
  
  private Lang(String key, String defValue)
  {
    this.key = key;
    this.defaultValue = defValue;
  }
  
  public String getMessage()
  {
    return replaceChatColours(getRawMessage());
  }
  
  public String getMessage(Object... format)
  {
    return replaceChatColours(String.format(getRawMessage(), format));
  }
  
  public String getRawMessage()
  {
    return config != null ? config.getString(this.key, this.defaultValue) : this.defaultValue;
  }
  
  public String getReplacedMessage(Object... objects)
  {
    String langMessage = getRawMessage();
    if (objects != null)
    {
      Object firstObject = null;
      for (int i = 0; i < objects.length; i++) {
        if (i % 2 == 0) {
          firstObject = objects[i];
        } else if ((firstObject != null) && (objects[i] != null)) {
          langMessage = langMessage.replace(firstObject.toString(), objects[i].toString());
        }
      }
    }
    return replaceChatColours(langMessage);
  }
  
  public static void sendMessage(CommandSender sender, Lang lang)
  {
    String strMessage = lang.getMessage();
    if (!strMessage.isEmpty())
    {
      List<String> messages = new ArrayList();
      if (strMessage.contains("\n"))
      {
        String[] messageSplit = strMessage.split("\n");
        String[] arrayOfString1;
        int j = (arrayOfString1 = messageSplit).length;
        for (int i = 0; i < j; i++)
        {
          String message = arrayOfString1[i];
          messages.add(message);
        }
      }
      else
      {
        messages.add(strMessage);
      }
      String message;
      for (Iterator i$ = messages.iterator(); i$.hasNext(); sender.sendMessage(message)) {
        message = (String)i$.next();
      }
    }
  }
  
  public static void sendMessage(CommandSender sender, Lang lang, Object... objects)
  {
    String strMessage = lang.getMessage(objects);
    if (!strMessage.isEmpty())
    {
      List<String> messages = new ArrayList();
      if (strMessage.contains("\n"))
      {
        String[] messageSplit = strMessage.split("\n");
        String[] arrayOfString1;
        int j = (arrayOfString1 = messageSplit).length;
        for (int i = 0; i < j; i++)
        {
          String message = arrayOfString1[i];
          messages.add(message);
        }
      }
      else
      {
        messages.add(strMessage);
      }
      String message;
      for (Iterator i$ = messages.iterator(); i$.hasNext(); sender.sendMessage(message)) {
        message = (String)i$.next();
      }
    }
  }
  
  public static void sendRawMessage(CommandSender sender, Lang lang)
  {
    String strMessage = lang.getRawMessage();
    if (!strMessage.isEmpty())
    {
      List<String> messages = new ArrayList();
      if (strMessage.contains("\n"))
      {
        String[] messageSplit = strMessage.split("\n");
        String[] arrayOfString1;
        int j = (arrayOfString1 = messageSplit).length;
        for (int i = 0; i < j; i++)
        {
          String message = arrayOfString1[i];
          messages.add(message);
        }
      }
      else
      {
        messages.add(strMessage);
      }
      String message;
      for (Iterator i$ = messages.iterator(); i$.hasNext(); sender.sendMessage(message)) {
        message = (String)i$.next();
      }
    }
  }
  
  public static void sendReplacedMessage(CommandSender sender, Lang lang, Object... objects)
  {
    String strMessage = lang.getReplacedMessage(objects);
    if (!strMessage.isEmpty())
    {
      List<String> messages = new ArrayList();
      if (strMessage.contains("\n"))
      {
        String[] messageSplit = strMessage.split("\n");
        String[] arrayOfString1;
        int j = (arrayOfString1 = messageSplit).length;
        for (int i = 0; i < j; i++)
        {
          String message = arrayOfString1[i];
          messages.add(message);
        }
      }
      else
      {
        messages.add(strMessage);
      }
      String message;
      for (Iterator i$ = messages.iterator(); i$.hasNext(); sender.sendMessage(message)) {
        message = (String)i$.next();
      }
    }
  }
  
  public static void init(JavaPlugin plugin)
  {
    configFile = new File(plugin.getDataFolder(), "messages.yml");
    config = YamlConfiguration.loadConfiguration(configFile);
    Lang[] arrayOfLang;
    int j = (arrayOfLang = values()).length;
    for (int i = 0; i < j; i++)
    {
      Lang value = arrayOfLang[i];
      if (!config.isSet(value.key)) {
        config.set(value.key, value.defaultValue);
      }
    }
    try
    {
      config.save(configFile);
    }
    catch (Exception localException) {}
  }
  
  public static String getString(String path)
  {
    return config.getString(path);
  }
  
  public static String getString(String path, String defaultValue)
  {
    return config.getString(path, defaultValue);
  }
  
  public static String saveString(String path, String value)
  {
    if (!config.isSet(path))
    {
      config.set(path, value);
      try
      {
        config.save(configFile);
      }
      catch (Exception localException) {}
    }
    return config.isSet(path) ? config.getString(value) : value;
  }
  
  public static String replaceChatColours(String aString)
  {
    return aString != null ? ChatColor.translateAlternateColorCodes('&', aString) : "";
  }
  
  public static List<String> replaceChatColours(List<String> lines)
  {
    if (lines != null) {
      for (int i = 0; i < lines.size(); i++) {
        lines.set(i, replaceChatColours((String)lines.get(i)));
      }
    }
    return lines;
  }
}
