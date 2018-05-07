package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        Socket socket=null;
        ServerSocket serverSocket=null;
        try {
            InetAddress iAddress = InetAddress.getLocalHost();
            System.out.println(iAddress.getHostAddress());
        }catch(UnknownHostException e){
            e.printStackTrace();
        }

        int portNumber = 5000;

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
                WorkerThread wt=new WorkerThread(socket);
                wt.start();

            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client

        }
	// write your code here
    }
    static class WorkerThread extends Thread{
        String line=null;
        BufferedReader is = null;
        PrintWriter os=null;
        protected Socket socket=null;
        public WorkerThread(Socket socket){
            this.socket=socket;
        }
        public void run(){
            try{
                System.out.println("connection accepted");
                is= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                os=new PrintWriter(socket.getOutputStream(),true);
                line=is.readLine();
                System.out.println(line);
                String output=Building.commandWorker(line);
                os.println(output);
                System.out.println(output);

                //is.close();
                //os.close();
                //socket.close();
                System.out.println("thread terminating");
            }catch(IOException e){
                System.out.println("IO error in server thread");
            }
        }

    }
}
