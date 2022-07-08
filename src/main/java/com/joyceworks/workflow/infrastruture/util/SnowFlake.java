package com.joyceworks.workflow.infrastruture.util;

public class SnowFlake {

  /**
   * 起始的时间戳
   */
  private static final long START_STMP = 1553153540L;

  /**
   * 每一部分占用的位数
   */
  private static final long SEQUENCE_BIT = 16; // 序列号占用的位数

  private static final long MACHINE_BIT = 5; // 机器标识占用的位数
  private static final long DATACENTER_BIT = 1; // 数据中心占用的位数

  /**
   * 每一部分的最大值
   */
  private static final long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);

  private static final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
  private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

  /**
   * 每一部分向左的位移
   */
  private static final long MACHINE_LEFT = SEQUENCE_BIT;

  private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
  private static final long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

  private final long datacenterId; // 数据中心
  private final long machineId; // 机器标识
  private long sequence = 0L; // 序列号
  private long lastStmp = -1L; // 上一次时间戳

  public SnowFlake(long datacenterId, long machineId) {
    if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
      throw new IllegalArgumentException(
          "datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
    }
    if (machineId > MAX_MACHINE_NUM || machineId < 0) {
      throw new IllegalArgumentException(
          "machineId can't be greater than MAX_MACHINE_NUM or less than 0");
    }
    this.datacenterId = datacenterId;
    this.machineId = machineId;
  }

  /**
   * 产生下一个ID
   */
  public synchronized long nextId() {
    long currStmp = getNewstmp();
    if (currStmp < lastStmp) {
      throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
    }

    if (currStmp == lastStmp) {
      // 相同毫秒内，序列号自增
      sequence = (sequence + 1) & MAX_SEQUENCE;
      // 同一毫秒的序列数已经达到最大
      if (sequence == 0L) {
        currStmp = getNextMill();
      }
    } else {
      // 不同毫秒内，序列号置为0
      sequence = 0L;
    }

    lastStmp = currStmp;

    return (currStmp - START_STMP) << TIMESTMP_LEFT // 时间戳部分
        | datacenterId << DATACENTER_LEFT // 数据中心部分
        | machineId << MACHINE_LEFT // 机器标识部分
        | sequence; // 序列号部分
  }

  private long getNextMill() {
    long mill = getNewstmp();
    while (mill <= lastStmp) {
      mill = getNewstmp();
    }
    return mill;
  }

  private long getNewstmp() {
    return System.currentTimeMillis() / 1000;
  }
//
//  public static void main(String[] args) {
//    HashMap<Long, Integer> counter = new HashMap<>();
//    ConcurrentHashMap<Long, Integer> map = new ConcurrentHashMap<>();
//    SnowFlake snowFlake = new SnowFlake(1, 1);
//    ArrayList<Long> longs = new ArrayList<>();
//
//    for (int i = 0; i < 1000000; i++) {
//      long id = snowFlake.nextId();
//      longs.add(id);
//    }
//    Map<Long, List<Long>> collect = longs.stream().collect(Collectors.groupingBy(item -> item));
//    for (Entry<Long, List<Long>> entry : collect.entrySet()) {
//      if (entry.getValue().size() > 1) {
//        System.out.println(entry.getKey());
//      }
//    }
//  }
}
