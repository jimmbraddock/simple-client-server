package test.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class EchoThread extends Thread {
  private final String PATH = "D:\\dev\\test";
  private final Logger log = LoggerFactory.getLogger(EchoThread.class);
  private Socket socket;
  private Statistic stat;

  public EchoThread(Socket clientSocket, Statistic stat) {

    this.socket = clientSocket;
    this.stat = stat;
  }

  public void run() {

    PrintWriter out = null;
    BufferedReader in = null;
    try {
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String userInput;
      while ((userInput = in.readLine()) != null) {
        log.info("receive from " + socket.getInetAddress() + ": " + userInput);
        System.out.println("receive: " + userInput);
        if (userInput.equalsIgnoreCase(Command.LS.toString())) {
          out.println(getListFiles());
          out.flush();
        } else if (userInput.toUpperCase().startsWith(Command.GET.toString())) {
          List<String> c = Arrays.asList(userInput.split(" "));
          String filename = c.get(1);
          if (sendFile(filename, out)) {
            stat.addToStatistic(filename);
            System.out.println(stat.getStat());
          }
        } else if (userInput.equalsIgnoreCase(Command.CLOSE.toString())) {
          break;
        } else

        {
          out.println("BAD COMMAND\n");
        }
      }
      socket.close();
      //wait();
    } catch (SocketException e) {
      log.info("remote client broke connection " + socket.getRemoteSocketAddress());
      try {
        socket.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  private String getListFiles() {

    StringBuffer sb = new StringBuffer();
    try {
      Files
          .walk(Paths.get(PATH))
          .filter(Files::isRegularFile)
          .forEach(p -> sb.append(p.getFileName() + "\n"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  private boolean sendFile(String filePath, PrintWriter p) throws IOException {

    //DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    //File file1 = new File(PATH);
    File downloadFile = new File(PATH, filePath);
    if (!downloadFile.exists()) {
      p.println("File not found");
      p.flush();
      return false;
    }
    FileInputStream fis = new FileInputStream(downloadFile.getPath());
    byte[] buffer = new byte[4096];
    int count;
    while ((count = fis.read(buffer)) > 0) {
      Charset latin1Charset = Charset.forName("UTF-8");
      CharBuffer charBuffer = latin1Charset.decode(ByteBuffer.wrap(buffer));
      p.print(charBuffer.array());
      //dos.write(buffer, 0, count);
    }
    //p.println("\n");
    p.flush();
    fis.close();
    return true;
    //dos.close();
  }
}