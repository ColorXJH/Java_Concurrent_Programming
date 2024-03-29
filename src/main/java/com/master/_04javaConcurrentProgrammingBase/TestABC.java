package com.master._04javaConcurrentProgrammingBase;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestABC {
    // 线程池
    static ThreadPool<HttpRequestHandler> threadPool = new DefaultThreadPool<>(1);
    // 根路径
    static String basePath;
    // 端口号
    static int port = 8080;
    // serverSocket
    static ServerSocket serverSocket;

    public static void setPort(int port) {
        if (port > 0) {
            TestABC.port = port;
        }
    }

    public static void setBasePath(String basePath) {
        if (basePath != null && new File(basePath).exists() && new File(basePath).isDirectory()) {
            TestABC.basePath = basePath;
        }
    }

    public static void start() throws Exception {
        serverSocket = new ServerSocket(port);
        Socket socket = null;
        while ((socket = serverSocket.accept()) != null) {
            threadPool.execute(new HttpRequestHandler(socket));
        }
        serverSocket.close();
    }

    public static void main(String[] args) {
        setPort(8080);
        setBasePath("C:/Users/ColorXJH/Desktop/html");
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    static class HttpRequestHandler implements Runnable{
        private Socket socket;
        public HttpRequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String line = null;
            BufferedReader br = null;
            BufferedReader reader = null;
            PrintWriter out = null;
            InputStream in = null;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String header = reader.readLine();
                String filePath = basePath + header.split(" ")[1];
                System.out.println("---------s-s-s-s-s-s--------------");
                System.out.println(filePath);
                out = new PrintWriter(socket.getOutputStream());
                if(filePath.endsWith("jpg") || filePath.endsWith("ico")) {
                    in = new FileInputStream(filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i= 0;
                    while((i=in.read())!=-1) {
                        baos.write(i);
                    }
                    byte[] array = baos.toByteArray();
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server:Molly");
                    socket.getOutputStream().write(array,0,array.length);
                }else {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                    out = new PrintWriter(socket.getOutputStream());
                    out.println("HTTP1.1 200 OK");
                    out.println("Server:Molly");
                    out.println("Content-Type: text/html; charset=UTF-8");
                    out.println("");
                    while((line = br.readLine())!=null) {
                        out.println(line);
                    }
                }
                out.flush();
            } catch (Exception e) {
                out.println("HTTP/1.0 500");
                out.println("");
                out.flush();
            }finally {
                close(br,in,reader,out,socket);
            }
        }


        //        // 关闭流或者Socket
//        private static void close(Closeable... closeables) {
//            if (closeables != null) {
//                for (Closeable closeable : closeables) {
//                    try {
//                        closeable.close();
//                    } catch (IOException ex) {
//                        // 忽略
//                    }
//                }
//            }
//        }
//
        private static void close(Closeable...closeables) {
            if(closeables!=null) {
                for(Closeable closeable:closeables) {
                    try {
                        closeable.close();
                    } catch (Exception e) {

                    }
                }
            }
        }

    }
}