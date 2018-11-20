package net.toxiic.minechests.data;

import org.bukkit.Material;
import org.bukkit.block.Block;

import net.toxiic.minechests.MainChests;

public class BlockData
{
  private Material blockType = Material.AIR;
  private byte blockData = -1;
  
  public BlockData() {}
  
  public BlockData(Material type)
  {
    if (type != null) {
      this.blockType = type;
    }
  }
  
  public BlockData(Material type, byte blockData)
  {
    if (type != null)
    {
      this.blockType = type;
      this.blockData = blockData;
    }
  }
  
  public boolean isBlock(Block block)
  {
    return (block != null) && (block.getType() == this.blockType) && ((this.blockData == -1) || (block.getData() == this.blockData));
  }
  
  public String toString()
  {
    return this.blockType != null ? this.blockType.toString() + (this.blockData != -1 ? ":" + this.blockData : "") : "null";
  }
  
  public static BlockData fromString(String string)
  {
    if ((string != null) && (!string.equalsIgnoreCase("null")))
    {
      String strMaterial = null;
      byte blockData = -1;
      if (string.contains(":"))
      {
        String[] strSplit = string.split(":");
        strMaterial = strSplit[0].toUpperCase();
        if (MainChests.isByte(strSplit[1])) {
          blockData = Byte.parseByte(strSplit[1]);
        }
      }
      else
      {
        strMaterial = string.trim().replace(" ", "").toUpperCase();
      }
      Material material = MainChests.isInteger(strMaterial) ? Material.getMaterial(Integer.parseInt(strMaterial)) : Material.getMaterial(strMaterial);
      return material != null ? new BlockData(material, blockData) : new BlockData();
    }
    return null;
  }
}
