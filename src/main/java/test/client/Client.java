package test.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.server.Command;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {
    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private final int PORT = 1978;
    private final String HOST = "localhost";
    private Socket s;
    private BufferedReader in;
    private DataInputStream dis;
    private PrintWriter out;

    public Client() {

        try {
            s = new Socket(HOST, PORT);
            Scanner consoleIn = new Scanner(System.in);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            dis = new DataInputStream(s.getInputStream());
            out = new PrintWriter(s.getOutputStream(), true);
            System.out.println(
                    "Hello friend, available commands: 1 - ls (show files in directory), " +
                            "2 - get <FileName> <SavePath> (download file), 3 - close (close connection)");
            boolean done = false;
            do {
                System.out.print("Command: ");
                String command = consoleIn.nextLine();
                if (isValidCommand(command)) {
                    out.println(command);
                    out.flush();
                    done = commandHandler(command);
                } else {
                    System.out.println("Unknown command");
                }
            } while (!done);

            System.out.println("Closing connection");
            s.close();

        } catch (IOException ex) {
            logger.error("Client error: " + ex.getMessage());
        }

    }

    private boolean commandHandler(String command) throws IOException {
        if (command.equalsIgnoreCase(Command.LS.toString())) {
            String responce = "";
            while ((responce = in.readLine()) != null) {
                if (responce.length() == 0) {
                    break;
                }
                System.out.println(responce);
            }
        } else if (command.equalsIgnoreCase(Command.CLOSE.toString())) {
            out.println(command);
            out.flush();
            if (command.equalsIgnoreCase("CLOSE")) {
                System.out.println("Ending conversation");
                return true;
            }
        }
        if (command.toUpperCase().startsWith(Command.GET.toString())) {
            List<String> getCommand = Arrays.asList(command.split(" "));
            String downloadFile = getCommand.get(2);
            receiveFile(downloadFile);
        }
        return false;
    }

    private boolean isValidCommand(String command) {
        return command.toLowerCase().matches("^ls|close|get .+\\.\\w+ .+$");
    }

    public void receiveFile(String savePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(savePath);
        byte[] buffer = new byte[1024];
        int fileSize = 1024;
        int read = 0;
        int totalRead = 0;
        int remaining = fileSize;
        while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            fos.write(buffer, 0, read);
        }
        fos.close();
    }

    public static void main(String[] args) {
        Client fc = new Client();
    }

}
