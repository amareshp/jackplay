package myapp;

import myapp.greeter.Greeter;
import myapp.greeter.NiceGreeter;
import myapp.greeter.RudeGreeter;
import myapp.greeter.QAGreeter;

import java.util.concurrent.ThreadLocalRandom;

public class Demo implements Runnable {
  static Greeter[] greeters = new Greeter[3];
  static {
    greeters[0] = new NiceGreeter();
    greeters[1] = new RudeGreeter();
    greeters[2] = new QAGreeter();
  }

  public static void main(String[] args) throws Exception {
    int threadCount = (args.length > 0) ? Integer.parseInt(args[0]) : 3;

    for (int i=0; i<threadCount; i++) {
      Thread t = new Thread(new Demo());
      t.start();
    }
  }

  static void visitGreeters() {
    for (Greeter g : greeters) {
      try {
        g.greet(getRandomName());
      } catch (Exception e) {
      }
    }
  }

  public void run() {
    while (true) {
      try {
        Thread.sleep(getRandomSleep());
      } catch(Exception e) {}
      visitGreeters();
    }
  }

  final static String[] NAMES = new String[] {"Alfred", "Brian", "Cameron", "David", "Emily", "Fendy", "Gary",
                                              "Hayden", "Ivory", "Joe", "Kian", "Lawrence", "Max", "Nick",
                                              "Osaka", "Paul", "Quinn", "Ryan", "Sally", "Tina", "Ugo", "Vivien",
                                              "Xavier", "Yancy", "Zach"};
  static String getRandomName() {
    int idx = nextInt(0, NAMES.length);
    return NAMES[idx];
  }

  public static long getRandomSleep() {
    return nextInt(3000, 12000);
  }

  public static void sleepSmallRandom() {
    long r = nextInt(200, 500);
    try {
      Thread.sleep(r);
    } catch(Exception e) {};
  }

  public synchronized static int nextInt(int start, int endExclusive) {
      return ThreadLocalRandom.current().nextInt(start, endExclusive);
  }
}
