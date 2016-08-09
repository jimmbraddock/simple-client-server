package test.server;

import java.util.HashMap;
import java.util.Map;

public class Statistic {
  private Map<String, Integer> stat = new HashMap<>();

  public void addToStatistic(String file) {

    synchronized (this) {
      if (stat.containsKey(file)) {
        stat.put(file, stat.get(file) + 1);
      } else {
        stat.put(file, 1);
      }
    }
  }

  public synchronized Map<String, Integer> getStat() {

    return stat;
  }
}
