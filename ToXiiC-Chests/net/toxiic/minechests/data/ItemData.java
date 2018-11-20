package net.toxiic.minechests.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.toxiic.minechests.MainChests;

public class ItemData
  implements ConfigurationSerializable
{
  private ItemStack itemStack = null;
  private int rewardChance = 1;
  
  public ItemData(ItemStack itemStack, int chance)
  {
    this.itemStack = itemStack;
    this.rewardChance = chance;
  }
  
  public int getChance()
  {
    return this.rewardChance;
  }
  
  public ItemStack getItemStack()
  {
    return this.itemStack;
  }
  
  public boolean hasItemStack()
  {
    return this.itemStack != null;
  }
  
  public Map<String, Object> serialize()
  {
    Map<String, Object> itemMap = new HashMap();
    if (this.itemStack != null)
    {
      if (this.rewardChance <= 0) {
        this.rewardChance = 1;
      } else if (this.rewardChance > 100) {
        this.rewardChance = 100;
      }
      Material itemType = this.itemStack.getType();
      int itemAmount = this.itemStack.getAmount();
      short itemDurability = this.itemStack.getDurability();
      Map<String, Integer> itemEnchantments = null;
      for (Map.Entry<Enchantment, Integer> itemEnchantment : this.itemStack.getEnchantments().entrySet()) {
        if (itemEnchantments == null) {
          itemEnchantments = new HashMap();
        }
      }
      ItemMeta itemMeta = this.itemStack.getItemMeta();
      String itemName = null;
      List<String> itemLores = null;
      int itemDye = -1;
      if (itemMeta != null)
      {
        itemName = replaceBukkitColours(itemMeta.getDisplayName());
        itemLores = replaceBukkitColours(itemMeta.getLore());
        if ((itemMeta instanceof LeatherArmorMeta)) {
          itemDye = ((LeatherArmorMeta)itemMeta).getColor().asRGB();
        }
      }
      itemMap.put("Chance", Integer.valueOf(this.rewardChance));
      itemMap.put("Type", itemType.toString());
      if (itemAmount != 1) {
        itemMap.put("Amount", Integer.valueOf(itemAmount));
      }
      if (itemDurability != 0) {
        itemMap.put("Data", Short.valueOf(itemDurability));
      }
      if (itemEnchantments != null) {
        itemMap.put("Enchantments", itemEnchantments);
      }
      if (itemName != null) {
        itemMap.put("Name", itemName);
      }
      if (itemLores != null) {
        itemMap.put("Lore", itemLores);
      }
      if (itemDye != -1) {
        itemMap.put("Dye", Integer.valueOf(itemDye));
      }
    }
    return itemMap;
  }
  
  public static ItemData deserialize(Map<String, Object> itemMap)
  {
    if ((itemMap != null) && (!itemMap.isEmpty()))
    {
      int chance = itemMap.containsKey("Chance") ? Integer.parseInt(itemMap.get("Chance").toString()) : 1;
      if (chance <= 0) {
        chance = 1;
      } else if (chance > 100) {
        chance = 100;
      }
      ItemStack itemStack = null;
      String strType = itemMap.containsKey("Type") ? itemMap.get("Type").toString() : "AIR";
      Material type = MainChests.isInteger(strType) ? Material.getMaterial(Integer.parseInt(strType)) : Material.getMaterial(strType.toUpperCase());
      if ((type != null) && (type != Material.AIR)) {
        if (itemMap.containsKey("Data"))
        {
          String strData = itemMap.get("Data") != null ? itemMap.get("Data").toString() : "0";
          byte itemData = Byte.parseByte(strData);
          if (itemData < 0) {
            itemData = 0;
          }
          itemStack = new ItemStack(type, 1, (short)itemData);
        }
        else
        {
          itemStack = new ItemStack(type);
        }
      }
      if (itemStack != null)
      {
        if (itemMap.containsKey("Amount"))
        {
          Object objAmount = itemMap.get("Amount");
          if ((objAmount != null) && (MainChests.isInteger(objAmount.toString()))) {
            itemStack.setAmount(Integer.parseInt(objAmount.toString()));
          }
        }
        Map<String, Object> itemEnchantments = getMap(itemMap.get("Enchantments"));
        if ((itemEnchantments != null) && (!itemEnchantments.isEmpty())) {
          for (Map.Entry<String, Object> enchantmentEntry : itemEnchantments.entrySet())
          {
            String strValue = enchantmentEntry.getValue() != null ? enchantmentEntry.getValue().toString() : null;
            if ((strValue != null) && (MainChests.isInteger(strValue)))
            {
              int enchantmentLevel = Integer.parseInt(strValue);
              String key = (String)enchantmentEntry.getKey();
              if (key != null)
              {
                Enchantment targetEnchantment = MainChests.isInteger(key) ? Enchantment.getById(Integer.parseInt(key)) : Enchantment.getByName(getEnchantmentName(key));
                if (targetEnchantment != null) {
                  itemStack.addUnsafeEnchantment(targetEnchantment, enchantmentLevel);
                }
              }
            }
          }
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null)
        {
          String itemName = getString(itemMap, "Name");
          if (itemName != null) {
            itemMeta.setDisplayName(replaceColours(itemName));
          }
          List<String> itemLores = (List)getObject(itemMap, "Lore", List.class);
          if (itemLores != null) {
            itemMeta.setLore(replaceColours(itemLores));
          }
          if ((itemMeta instanceof LeatherArmorMeta))
          {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)itemMeta;
            if (itemMap.containsKey("Dye"))
            {
              Color dyeColor = getDyeColour(itemMap.get("Dye") != null ? itemMap.get("Dye").toString() : null);
              if (dyeColor != null) {
                leatherArmorMeta.setColor(dyeColor);
              }
            }
            itemStack.setItemMeta(leatherArmorMeta);
          }
          else
          {
            itemStack.setItemMeta(itemMeta);
          }
        }
      }
      return new ItemData(itemStack, chance);
    }
    return null;
  }
  
  public static ItemStack addEnchantment(ItemStack itemStack, Enchantment enchantment, int level)
  {
    if ((itemStack != null) && (enchantment != null))
    {
      if (level < enchantment.getStartLevel()) {
        level = enchantment.getStartLevel();
      }
      itemStack.addUnsafeEnchantment(enchantment, level);
    }
    return itemStack;
  }
  
  private static String getString(Map<String, Object> itemMap, String key)
  {
    return (key != null) && (itemMap != null) ? null : itemMap.containsKey(key) ? null : itemMap.get(key) != null ? itemMap.get(key).toString() : null;
  }
  
  private static <T> T getObject(Map<String, Object> itemMap, String key, Class<T> unused)
  {
    return (T)getObject(itemMap, key, null, unused);
  }
  
  private static <T> T getObject(Map<String, Object> paramMap, String paramString, T paramT, Class<T> paramClass)
  {
    throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from Object to T\n");
  }
  
  public static Map<String, Object> getMap(Object objMap)
  {
    return objMap != null ? new HashMap() : (objMap instanceof ConfigurationSection) ? ((ConfigurationSection)objMap).getValues(false) : (objMap instanceof Map) ? (Map)objMap : new HashMap();
  }
  
  private static Color getDyeColour(String friendlyName)
  {
    if (friendlyName != null)
    {
      if (MainChests.isInteger(friendlyName)) {
        return Color.fromRGB(Integer.parseInt(friendlyName));
      }
      if (friendlyName.equalsIgnoreCase("Aqua")) {
        return Color.AQUA;
      }
      if (friendlyName.equalsIgnoreCase("Black")) {
        return Color.BLACK;
      }
      if (friendlyName.equalsIgnoreCase("Blue")) {
        return Color.BLUE;
      }
      if (friendlyName.equalsIgnoreCase("Fuchsia")) {
        return Color.FUCHSIA;
      }
      if ((friendlyName.equalsIgnoreCase("Gray")) || (friendlyName.equalsIgnoreCase("Grey"))) {
        return Color.GRAY;
      }
      if (friendlyName.equalsIgnoreCase("Green")) {
        return Color.GREEN;
      }
      if (friendlyName.equalsIgnoreCase("Lime")) {
        return Color.LIME;
      }
      if (friendlyName.equalsIgnoreCase("Maroon")) {
        return Color.MAROON;
      }
      if (friendlyName.equalsIgnoreCase("Navy")) {
        return Color.NAVY;
      }
      if (friendlyName.equalsIgnoreCase("Olive")) {
        return Color.OLIVE;
      }
      if (friendlyName.equalsIgnoreCase("Orange")) {
        return Color.ORANGE;
      }
      if (friendlyName.equalsIgnoreCase("Purple")) {
        return Color.PURPLE;
      }
      if (friendlyName.equalsIgnoreCase("Red")) {
        return Color.RED;
      }
      if (friendlyName.equalsIgnoreCase("Silver")) {
        return Color.SILVER;
      }
      if (friendlyName.equalsIgnoreCase("Teal")) {
        return Color.TEAL;
      }
      if (friendlyName.equalsIgnoreCase("White")) {
        return Color.WHITE;
      }
      if (friendlyName.equalsIgnoreCase("Yellow")) {
        return Color.YELLOW;
      }
    }
    return null;
  }
  
  private static String getEnchantmentName(String friendlyName)
  {
    if (friendlyName != null)
    {
      if ((friendlyName.equalsIgnoreCase("Sharpness")) || (friendlyName.equalsIgnoreCase("Sharp"))) {
        return Enchantment.DAMAGE_ALL.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Bane of Arthropods")) || (friendlyName.equalsIgnoreCase("Arthropods")) || (friendlyName.equalsIgnoreCase("Bane")) || (friendlyName.equalsIgnoreCase("Arthro"))) {
        return Enchantment.DAMAGE_ARTHROPODS.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Smite")) || (friendlyName.equalsIgnoreCase("Undead"))) {
        return Enchantment.DAMAGE_UNDEAD.getName();
      }
      if (friendlyName.equalsIgnoreCase("Power")) {
        return Enchantment.ARROW_DAMAGE.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Flame")) || (friendlyName.equalsIgnoreCase("Flames"))) {
        return Enchantment.ARROW_FIRE.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Infinite")) || (friendlyName.equalsIgnoreCase("Infinity"))) {
        return Enchantment.ARROW_INFINITE.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Punch")) || (friendlyName.equalsIgnoreCase("Push"))) {
        return Enchantment.ARROW_KNOCKBACK.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Efficiency")) || (friendlyName.equalsIgnoreCase("Eff"))) {
        return Enchantment.DIG_SPEED.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Unbreaking")) || (friendlyName.equalsIgnoreCase("Durability")) || (friendlyName.equalsIgnoreCase("Dura"))) {
        return Enchantment.DURABILITY.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Fire Aspect")) || (friendlyName.equalsIgnoreCase("Fire"))) {
        return Enchantment.FIRE_ASPECT.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Knockback")) || (friendlyName.equalsIgnoreCase("Knock"))) {
        return Enchantment.KNOCKBACK.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Fortune")) || (friendlyName.equalsIgnoreCase("Fort"))) {
        return Enchantment.LOOT_BONUS_BLOCKS.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Looting")) || (friendlyName.equalsIgnoreCase("Loot"))) {
        return Enchantment.LOOT_BONUS_MOBS.getName();
      }
      if (friendlyName.equalsIgnoreCase("Luck")) {
        return Enchantment.LUCK.getName();
      }
      if (friendlyName.equalsIgnoreCase("Lure")) {
        return Enchantment.LURE.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Oxygen")) || (friendlyName.equalsIgnoreCase("Breathing")) || (friendlyName.equalsIgnoreCase("Respiration"))) {
        return Enchantment.OXYGEN.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Protection")) || (friendlyName.equalsIgnoreCase("Prot"))) {
        return Enchantment.PROTECTION_ENVIRONMENTAL.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Blast Protection")) || (friendlyName.equalsIgnoreCase("BlastProt"))) {
        return Enchantment.PROTECTION_EXPLOSIONS.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Fall Protection")) || (friendlyName.equalsIgnoreCase("FallProt")) || (friendlyName.equalsIgnoreCase("Feather")) || (friendlyName.equalsIgnoreCase("Feather Falling"))) {
        return Enchantment.PROTECTION_FALL.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Fire Protection")) || (friendlyName.equalsIgnoreCase("FireProt"))) {
        return Enchantment.PROTECTION_FIRE.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Projectile Protection")) || (friendlyName.equalsIgnoreCase("ProjProt"))) {
        return Enchantment.PROTECTION_PROJECTILE.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Silk Touch")) || (friendlyName.equalsIgnoreCase("SilkTouch")) || (friendlyName.equalsIgnoreCase("Silk"))) {
        return Enchantment.SILK_TOUCH.getName();
      }
      if (friendlyName.equalsIgnoreCase("Thorns")) {
        return Enchantment.THORNS.getName();
      }
      if ((friendlyName.equalsIgnoreCase("Water Worker")) || (friendlyName.equalsIgnoreCase("Aqua Affinity"))) {
        return Enchantment.WATER_WORKER.getName();
      }
    }
    return friendlyName != null ? friendlyName.toUpperCase().replace(" ", "_") : "";
  }
  
  public static String getItemName(ItemStack itemStack)
  {
    if (itemStack != null)
    {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
        return itemMeta.hasDisplayName() ? ChatColor.stripColor(itemMeta.getDisplayName()) : "";
      }
    }
    return "";
  }
  
  public static ItemStack renameItem(ItemStack itemStack, String name)
  {
    if ((itemStack != null) && (name != null))
    {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null)
      {
        itemMeta.setDisplayName(replaceColours(name));
        itemStack.setItemMeta(itemMeta);
      }
    }
    return itemStack;
  }
  
  private static String replaceBukkitColours(String item)
  {
    if (item != null)
    {
      ChatColor[] arrayOfChatColor;
      int j = (arrayOfChatColor = ChatColor.values()).length;
      for (int i = 0; i < j; i++)
      {
        ChatColor chatColor = arrayOfChatColor[i];
        item = item.replace(chatColor.toString(), "&" + chatColor.getChar());
      }
    }
    return item;
  }
  
  private static List<String> replaceBukkitColours(List<String> list)
  {
    if (list != null)
    {
      List<String> newList = new ArrayList();
      for (String listItem : list) {
        newList.add(replaceBukkitColours(listItem));
      }
      return newList;
    }
    return list;
  }
  
  private static String replaceColours(String text)
  {
    return text != null ? ChatColor.translateAlternateColorCodes('&', text) : null;
  }
  
  private static List<String> replaceColours(List<String> textList)
  {
    if (textList != null)
    {
      List<String> newText = new ArrayList();
      for (String text : textList)
      {
        String replacedColours = replaceColours(text);
        if (replacedColours != null) {
          newText.add(replacedColours);
        }
      }
      return newText;
    }
    return null;
  }
  
  public static ItemStack setLore(ItemStack itemStack, String... lores)
  {
    if ((itemStack != null) && (lores != null))
    {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null)
      {
        itemMeta.setLore(replaceColours(Arrays.asList(lores)));
        itemStack.setItemMeta(itemMeta);
      }
    }
    return itemStack;
  }
}
