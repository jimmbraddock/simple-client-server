package client;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket s;

    public Client(String host, int port, String file) {
        try {
            s = new Socket(host, port);
            InputStream is = s.getInputStream();
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println("GET / HTTP/1.0");
            pw.println();
            pw.flush();
            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1) {
                String output = new String(buffer, 0, read);
                System.out.print(output);
                System.out.flush();
            };
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String file) throws IOException {
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }

        fis.close();
        dos.close();
    }

    public static void main(String[] args) {
        Client fc = new Client("localhost", 1978, "C:\\test.txt");
    }

}
