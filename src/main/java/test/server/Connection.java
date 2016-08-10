package test.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


public class Connection extends Thread {
    private final Logger log = LoggerFactory.getLogger(Connection.class);
    private Socket socket;
    private Statistic stat;
    private String rootDir;

    public Connection(Socket clientSocket, Statistic stat, String rootDir) {

        this.socket = clientSocket;
        this.stat = stat;
        this.rootDir = rootDir;
    }

    public void run() {

        PrintWriter out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String userInput;
            while ((userInput = in.readLine()) != null) {
                log.info("Receive command from " + socket.getRemoteSocketAddress() + ": " + userInput);
                if (userInput.equalsIgnoreCase(Command.LS.toString())) {
                    out.println(getListFiles());
                    out.flush();
                } else if (userInput.toUpperCase().startsWith(Command.GET.toString())) {
                    List<String> c = Arrays.asList(userInput.split(" "));
                    String filename = c.get(1);
                    log.info("Send file " + filename + " to " + socket.getRemoteSocketAddress());
                    if (sendFile(filename, out)) {
                        stat.addToStatistic(filename);
                    }
                } else if (userInput.equalsIgnoreCase(Command.CLOSE.toString())) {
                    log.info("Close connection to " + socket.getRemoteSocketAddress());
                    break;
                }
            }
            socket.close();
        } catch (SocketException e) {
            log.info("remote client broke connection " + socket.getRemoteSocketAddress());
            try {
                socket.close();
            } catch (IOException e1) {
                log.error("Close socket error: " + e1);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getListFiles() {
        StringBuffer sb = new StringBuffer();

        Arrays.asList(new File(rootDir).listFiles())
                .stream()
                .filter(File::isFile)
                .forEach(f -> sb.append(f.getName() + "\n"));

        return sb.toString();
    }

    private boolean sendFile(String filePath, PrintWriter pw) throws IOException {
        File downloadFile = new File(rootDir, filePath);
        if (!downloadFile.exists()) {
            log.info("Downloading file " + filePath + " not found!");
            return false;
        }
        FileInputStream fis = new FileInputStream(downloadFile.getPath());
        byte[] buffer = new byte[1024];
        int count;
        while ((count = fis.read(buffer)) > 0) {
            Charset latin1Charset = Charset.forName("UTF-8");
            CharBuffer charBuffer = latin1Charset.decode(ByteBuffer.wrap(buffer));
            pw.print(charBuffer.array());
        }
        pw.flush();
        fis.close();
        return true;
    }
}