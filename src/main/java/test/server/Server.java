package test.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

public class Server {

  private static ServerSocket ss;

  static final int PORT = 1978;

  public static void main(String args[]) {
    //String rootPath = args[0];
    String rootPath = "D:\\dev\\test";
    Socket socket = null;

    try {
      ss = new ServerSocket(PORT);
    } catch (IOException e) {
      e.printStackTrace();

    }
    Statistic stat = new Statistic();
    Timer time = new Timer(); // Instantiate Timer Object
    StatisticWriter st = new StatisticWriter(stat,
                                             "D:\\dev\\test\\stat.txt"
    ); // Instantiate SheduledTask class
    time.schedule(st, 0, 8000);

    while (true) {
      try {
        socket = ss.accept();
      } catch (IOException e) {
        System.out.println("I/O error: " + e);
      }
      // new threa for a client
      new EchoThread(socket, stat).start();
    }
  }


}
