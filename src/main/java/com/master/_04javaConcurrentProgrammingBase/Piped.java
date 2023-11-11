package com.master._04javaConcurrentProgrammingBase;

import java.io.*;

/**
 * @ClassName: Piped
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 创建PrintThread,它用来接受main线程的输入，任何main线程的输入均通过PipedWriter写入，而PrintThread在另一端通过PipedReader将内容读出并打印
 * @Datetime: 2023/11/11 17:44
 * @author: ColorXJH
 */
public class Piped {
    public static void main(String[] args) throws IOException {
        PipedWriter out=new PipedWriter();
        PipedReader in=new PipedReader();
        //将输出流和输入流进行连接，否则将在使用时抛出IOException
        out.connect(in);
        Thread printThread=new Thread(new Print(in),"Print-Thread");
        //不直接使用System.in 使用包装流设置编码
        InputStreamReader inputStreamReader = new InputStreamReader(System.in, "UTF-8");
        printThread.start();
        int receive=0;
        try{
         while ((receive=inputStreamReader.read())!=-1){
             out.write(receive);
         }
        }finally {
            out.close();
        }
    }

    static class Print implements Runnable{
        private PipedReader in;
        public Print(PipedReader in){
            this.in=in;

        }
        @Override
        public void run() {
            int receive=0;

            try{
                while ((receive=in.read())!=-1){
                    System.out.print((char)receive);
                }
            }catch (IOException ex){

            }
        }
    }
}
