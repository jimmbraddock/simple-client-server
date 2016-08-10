package test.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

public class Server {

    private static ServerSocket ss;

    private static final int PORT = 1978;
    private static final String HOST = "localhost";
    private static final Logger logger = LoggerFactory.getLogger(ServerSocket.class);

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Invalid arguments! First argument should be path to root directory, " +
                    "second should be path to statistic file");
            return;
        }
        String rootPath = args[0];
        String statFile = args[1];
        Socket socket = null;
        logger.info("Start server");
        try {
            ss = new ServerSocket(PORT, 50, InetAddress.getByName(HOST));
        } catch (IOException e) {
            logger.error("ServerSocket created with error: " + e);
        }
        logger.info("Create statistic scheduler");
        Statistic stat = new Statistic();
        Timer time = new Timer();
        StatisticWriter sw = new StatisticWriter(stat, statFile);
        time.schedule(sw, 0, 8000);

        while (true) {
            try {
                socket = ss.accept();
                logger.info("Open new connection to " + socket.getRemoteSocketAddress());
            } catch (IOException e) {
                logger.error("I/O error: " + e);
            }
            new Connection(socket, stat, rootPath).start();
        }
    }


}
