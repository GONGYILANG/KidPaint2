[33mcommit 1f3f95372d90744ec2cf1ede3a765214743f9fdb[m[33m ([m[1;36mHEAD -> [m[1;32mmain[m[33m, [m[1;31morigin/main[m[33m)[m
Author: Gong Yilang <23266775@life.hkbu.edu.hk>
Date:   Thu Nov 6 14:06:09 2025 +0800

    Add server code

[1mdiff --git a/src/KidPaintServer.java b/src/KidPaintServer.java[m
[1mnew file mode 100644[m
[1mindex 0000000..998160c[m
[1m--- /dev/null[m
[1m+++ b/src/KidPaintServer.java[m
[36m@@ -0,0 +1,134 @@[m
[32m+[m[32mimport java.io.DataInputStream;[m
[32m+[m[32mimport java.io.DataOutputStream;[m
[32m+[m[32mimport java.io.IOException;[m
[32m+[m[32mimport java.net.ServerSocket;[m
[32m+[m[32mimport java.net.Socket;[m
[32m+[m[32mimport java.util.HashMap;[m
[32m+[m[32mimport java.util.LinkedList;[m
[32m+[m
[32m+[m
[32m+[m[32mpublic class KidPaintServer {[m
[32m+[m[32m    HashMap<Socket, DataOutputStream> clientList = new HashMap<>();[m
[32m+[m[32m    int[][] data;           // local data[m
[32m+[m
[32m+[m[32m    class Point{[m
[32m+[m[32m        int x, y;[m
[32m+[m
[32m+[m[32m        Point(int x, int y) {[m
[32m+[m[32m            this.x = x;[m
[32m+[m[32m            this.y = y;[m
[32m+[m[32m        }[m
[32m+[m[32m    }[m
[32m+[m[32m    // String username;[m
[32m+[m[32m    public KidPaintServer(int port) throws IOException {[m
[32m+[m[32m        ServerSocket serverSocket = new ServerSocket(port);[m
[32m+[m
[32m+[m[32m        while(true) {[m
[32m+[m[32m            Socket clientSocket = serverSocket.accept();[m
[32m+[m
[32m+[m[32m            synchronized(clientList){[m
[32m+[m[32m                clientList.put(clientSocket, new DataOutputStream(clientSocket.getOutputStream()));[m
[32m+[m[32m            }[m
[32m+[m
[32m+[m[32m            Thread t = new WorkerThread(this, clientSocket);[m
[32m+[m[32m            t.start();[m
[32m+[m
[32m+[m[32m        }[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public void serve(Socket clientSocket, WorkerThread thread) throws IOException {[m
[32m+[m[32m        DataInputStream in = new DataInputStream(clientSocket.getInputStream());[m
[32m+[m[32m        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());[m
[32m+[m
[32m+[m[32m        byte[] buffer = new byte[1024];[m
[32m+[m[32m        while(true) {[m
[32m+[m[32m            int dataType = in.read();  // 0: name, 1: pixels, 2: text[m
[32m+[m[32m            switch(dataType) {[m
[32m+[m[32m                case 0:[m
[32m+[m[32m                    readName(in, thread);[m
[32m+[m[32m                    break;[m
[32m+[m[32m                case 1:[m
[32m+[m[32m                    readPixels(in);[m
[32m+[m[32m                    break;[m
[32m+[m[32m                case 2:[m
[32m+[m[32m                    readText(in, thread.getUserName());[m
[32m+[m[32m                    break;[m
[32m+[m[32m            }[m
[32m+[m
[32m+[m[32m        }[m
[32m+[m[32m    }[m
[32m+[m[32m    void readText(DataInputStream in, String username) throws IOException{[m
[32m+[m[32m        int size = in.readInt();            //read the length of the text[m
[32m+[m[32m        byte[] buffer = new byte[size];     //create buffer for the text[m
[32m+[m[32m        in.read(buffer,0, size);        //read the bytes into the buffer[m
[32m+[m[32m        System.out.println(new String(buffer, 0, size));  //print the text[m
[32m+[m
[32m+[m[32m        String text = username + ": " + new String(buffer, 0, size); //convert the buffer to string format[m
[32m+[m[32m            //add username to the beginning of the text[m
[32m+[m[32m        sendText(text.getBytes());      //send the modified text[m
[32m+[m[32m    }[m
[32m+[m[32m    void sendText(byte[] buffer) {[m
[32m+[m[32m        synchronized (clientList ) {        //lock clientList, so other threads cannot use it[m
[32m+[m[32m            for (DataOutputStream out : clientList.values()) {[m
[32m+[m[32m                try {[m
[32m+[m[32m                    out.write(2);         // send type ID=2, it is a text[m
[32m+[m[32m                    out.writeInt(buffer.length); // send an integer represents the length of the text[m
[32m+[m[32m                    out.write(buffer, 0 ,buffer.length);    // send the text in bytes[m
[32m+[m[32m                    out.flush();[m
[32m+[m[32m                } catch (IOException e) {[m
[32m+[m[32m                    System.out.println("This connection dropped! Ignore it.");[m
[32m+[m[32m                }[m
[32m+[m[32m            }[m
[32m+[m[32m        }[m
[32m+[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    void readName(DataInputStream in, WorkerThread thread) throws IOException {[m
[32m+[m[32m        byte[] buffer = new byte[1024];[m
[32m+[m[32m        int len = in.readInt();[m
[32m+[m[32m        in.read(buffer, 0, len);[m
[32m+[m[32m        String username = new String(buffer,0,len);[m
[32m+[m[32m        System.out.println(username);[m
[32m+[m[32m        thread.setUserName(username);[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    void readPixels(DataInputStream in) throws IOException {[m
[32m+[m[32m        int color = in.readInt();[m
[32m+[m[32m        int size = in.readInt();[m
[32m+[m[32m        LinkedList<Point> points = new LinkedList<>();[m
[32m+[m[32m        for (int i = 0; i < size; i++) {[m
[32m+[m[32m            int x = in.readInt();[m
[32m+[m[32m            int y = in.readInt();[m
[32m+[m[32m            points.add(new Point(x, y));[m
[32m+[m[32m            System.out.printf("%d --> %d, %d\n", color, x, y);[m
[32m+[m[32m            //update local data[m
[32m+[m[32m        }[m
[32m+[m
[32m+[m[32m        sendPixels(color, points);[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    void sendPixels(int color, LinkedList<Point> list){[m
[32m+[m[32m        synchronized(clientList){[m
[32m+[m[32m            for(DataOutputStream out: clientList.values()){[m
[32m+[m[32m                try {[m
[32m+[m[32m                    out.write(1);         // send 1 represents pixel info[m
[32m+[m[32m                    out.writeInt(color);    // send color in integer[m
[32m+[m[32m                    out.writeInt(list.size()); // send an integer represents the number of pixels[m
[32m+[m[32m                    for (Point p : list) {     // send pixel inform[m
[32m+[m[32m                        out.writeInt(p.x);[m
[32m+[m[32m                        out.writeInt(p.y);[m
[32m+[m[32m                    }[m
[32m+[m[32m                    out.flush();[m
[32m+[m[32m                } catch (IOException e) {[m
[32m+[m[32m                    System.out.println("One connection is dropped!");[m
[32m+[m[32m                }[m
[32m+[m
[32m+[m[32m            }[m
[32m+[m[32m        }[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public static void main(String[] args) throws IOException {[m
[32m+[m[32m        new KidPaintServer(12345);[m
[32m+[m
[32m+[m[32m    }[m
[32m+[m[32m}[m
[1mdiff --git a/src/WorkerThread.java b/src/WorkerThread.java[m
[1mnew file mode 100644[m
[1mindex 0000000..5dff355[m
[1m--- /dev/null[m
[1m+++ b/src/WorkerThread.java[m
[36m@@ -0,0 +1,33 @@[m
[32m+[m[32mimport java.io.IOException;[m
[32m+[m[32mimport java.net.Socket;[m
[32m+[m
[32m+[m[32mpublic class WorkerThread extends Thread {[m
[32m+[m[32m    String username;[m
[32m+[m[32m    Socket socket;[m
[32m+[m[32m    KidPaintServer server;[m
[32m+[m
[32m+[m[32m    public WorkerThread(KidPaintServer server, Socket socket) {[m
[32m+[m[32m        this.socket = socket;[m
[32m+[m[32m        this.server = server;[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public void setUserName(String name) {[m
[32m+[m[32m        username = name;[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public String getUserName() {[m
[32m+[m[32m        return username;[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    @Override[m
[32m+[m[32m    public void run() {[m
[32m+[m[32m        try {[m
[32m+[m[32m            server.serve(socket, this);[m
[32m+[m[32m        } catch (IOException e) {[m
[32m+[m[32m            synchronized(server.clientList){[m
[32m+[m[32m                server.clientList.remove(socket);[m
[32m+[m[32m            }[m
[32m+[m[32m            System.out.println("client disconnected!");[m
[32m+[m[32m        }[m
[32m+[m[32m    }[m
[32m+[m[32m}[m
