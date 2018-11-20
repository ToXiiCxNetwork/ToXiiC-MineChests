package net.toxiic.minechests.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CustomMap<K, V>
  extends HashMap
{
  public Set<Map.Entry<K, V>> entrySet()
  {
    return super.entrySet();
  }
  
  public V get(Object key)
  {
    return (V)get(key, null);
  }
  
  public V get(Object key, V defValue)
  {
    Object value = super.get(key);
    return (V)(value != null ? value : defValue);
  }
  
  public List<K> getKey(V value)
  {
    List<K> keyList = new ArrayList();
    Set<Map.Entry<K, V>> entrySet = entrySet();
    for (Map.Entry<K, V> mapEntry : entrySet) {
      if (((mapEntry.getValue() == null) && (value == null)) || ((value != null) && (value.equals(mapEntry.getValue())))) {
        keyList.add(mapEntry.getKey());
      }
    }
    if (keyList.isEmpty()) {
      keyList.add(null);
    }
    return keyList;
  }
}
