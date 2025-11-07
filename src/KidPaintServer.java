import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;


public class KidPaintServer {
    HashMap<Socket, DataOutputStream> clientList = new HashMap<>();
    int[][] data;           // local data

    class Point{
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    // String username;
    public KidPaintServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        while(true) {
            Socket clientSocket = serverSocket.accept();

            synchronized(clientList){
                clientList.put(clientSocket, new DataOutputStream(clientSocket.getOutputStream()));
            }

            Thread t = new WorkerThread(this, clientSocket);
            t.start();

        }
    }

    public void serve(Socket clientSocket, WorkerThread thread) throws IOException {
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        byte[] buffer = new byte[1024];
        while(true) {
            int dataType = in.read();  // 0: name, 1: pixels, 2: text
            switch(dataType) {
                case 0:
                    readName(in, thread);
                    break;
                case 1:
                    readPixels(in);
                    break;
                case 2:
                    readText(in, thread.getUserName());
                    break;
                case 3:
                    readSize(in);
                    sendInitialPixels();
                    break;
            }

        }
    }
    void readText(DataInputStream in, String username) throws IOException{
        int size = in.readInt();            //read the length of the text
        byte[] buffer = new byte[size];     //create buffer for the text
        in.read(buffer,0, size);        //read the bytes into the buffer
        System.out.println(new String(buffer, 0, size));  //print the text

        String text = username + ": " + new String(buffer, 0, size); //convert the buffer to string format
            //add username to the beginning of the text
        sendText(text.getBytes());      //send the modified text
    }
    void sendText(byte[] buffer) {
        synchronized (clientList ) {        //lock clientList, so other threads cannot use it
            for (DataOutputStream out : clientList.values()) {
                try {
                    out.write(2);         // send type ID=2, it is a text
                    out.writeInt(buffer.length); // send an integer represents the length of the text
                    out.write(buffer, 0 ,buffer.length);    // send the text in bytes
                    out.flush();
                } catch (IOException e) {
                    System.out.println("This connection dropped! Ignore it.");
                }
            }
        }

    }

    void readName(DataInputStream in, WorkerThread thread) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        in.read(buffer, 0, len);
        String username = new String(buffer,0,len);
        System.out.println(username);
        thread.setUserName(username);
    }

    void readPixels(DataInputStream in) throws IOException {
        int color = in.readInt();
        int size = in.readInt();
        LinkedList<Point> points = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            int x = in.readInt();
            int y = in.readInt();
            points.add(new Point(x, y));
            System.out.printf("%d --> %d, %d\n", color, x, y);
            // update local data
            data[y][x] = color;
        }

        sendPixels(color, points);
    }

    void sendPixels(int color, LinkedList<Point> list){
        synchronized(clientList){
            for(DataOutputStream out: clientList.values()){
                try {
                    out.write(1);         // send 1 represents pixel info
                    out.writeInt(color);    // send color in integer
                    out.writeInt(list.size()); // send an integer represents the number of pixels
                    for (Point p : list) {     // send pixel inform
                        out.writeInt(p.x);
                        out.writeInt(p.y);
                    }
                    out.flush();
                } catch (IOException e) {
                    System.out.println("One connection is dropped!");
                }

            }
        }
    }

    void readSize(DataInputStream in) throws IOException {
        int numPixels = in.readInt();
        if(data == null)
            data = new int[numPixels][numPixels];
    }

    void sendInitialPixels() {
        synchronized (clientList) {
            for (DataOutputStream out: clientList.values()) {
                try {
                    for(int i = 0;i<data.length;i++) {
                        for(int j = 0;j<data[0].length;j++) {
                            // if(data[i][j]==0) continue; // skip white pixels
                            out.write(1);
                            out.writeInt(data[i][j]);
                            out.writeInt(1); // size = 1
                            out.writeInt(j);
                            out.writeInt(i);
                        }
                    }
                } catch(IOException e) {
                    System.out.println("One connection is dropped!");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new KidPaintServer(12345);

    }
}
