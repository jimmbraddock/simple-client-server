package test.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import test.server.Command;

public class Client {

  private Socket s;

  public Client(String host, int port, String file) {

    try {
      s = new Socket(host, port);
      Scanner consoleIn = new Scanner(System.in);
      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      DataInputStream dis = new DataInputStream(s.getInputStream());
      PrintWriter socketOut = new PrintWriter(s.getOutputStream(), true);
      System.out.println(
          "Hello friend, available commands: 1 - ls (show files in directory), 2- get <FileName>");
      boolean done = false;
      do {
        // Ask for a message; we're done if none is given
        System.out.print("Command: ");
        String message = consoleIn.nextLine();

        if (isValidCommand(message)) {
          socketOut.println(message);
          if (message.equalsIgnoreCase(Command.LS.toString())) {
            String responce = "";
            while ((responce = in.readLine()) != null) {
              if (responce.length() == 0) {
                break; // blank line terminates input
              }
              System.out.println("responce: " + responce);
            }
          } else if (message.equalsIgnoreCase(Command.CLOSE.toString())) {
            socketOut.println(message);
            socketOut.flush();
            if (message.equalsIgnoreCase("CLOSE")) {
              done = true;
              System.out.println("Ending conversation");
            }
          }
          if (message.toUpperCase().startsWith(Command.GET.toString())) {
            List<String> getCommand = Arrays.asList(message.split(" "));
            receiveFile(dis, getCommand.get(2));
          }
        } else {
          System.out.println("Unknown command");
        }


//        if (!done) {
//          // THere was a message; send it to the server
//          socketOut.println(message);
//          socketOut.flush();
//          if (message.equalsIgnoreCase("CLOSE")) {
//            done = true;
//            System.out.println("Ending conversation");
//          }
//        }
//        if (message.startsWith("GET")) {
//          receiveFile(dis);
//        } else {
//          String responce = "";
//          while ((responce = in.readLine()) != null) {
//            if (responce.length() == 0) {
//              break; // blank line terminates input
//            }
//            System.out.println("responce: " + responce);
//          }
//        }
      } while (!done);

      System.out.println("Closing connection");
      s.close();

    } catch (IOException ex) {
      System.err.println(ex);
      ex.printStackTrace();
    }

  }
  public static void main(String[] args) {

    Client fc = new Client("localhost", 1978, "C:\\test.txt");
  }

  private boolean isValidCommand(String command) {

    return command.toLowerCase().matches("^ls|close|get .+\\.\\w+ .+$");
  }
  public void receiveFile(InputStream dis, String savePath) throws IOException {

    FileOutputStream fos = new FileOutputStream(savePath);
    byte[] buffer = new byte[4096];

    int filesize = 4096; // Send file size in separate msg
    int read = 0;
    int totalRead = 0;
    int remaining = filesize;
    while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
      totalRead += read;
      remaining -= read;
      System.out.println("read " + totalRead + " bytes.");
      fos.write(buffer, 0, read);
    }

    fos.close();

    //dis.close();
  }

}
