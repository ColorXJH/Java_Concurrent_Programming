package com.master.PART4;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 线程中的服务
 * @date 2024-06-03 10:44
 */
public class ServiceInThread {
    //1：向单项消息中加入一个回调
    //2：依靠Thread.join
    //3：基于Future构建
    //4：创建工作者线程

    //1：完成回调：有时在结构上和观察者模式一样
        //某个应用程序可以提供几个功能，其中的一个或多个功能需要读取文件，在读取文件的过程中，不希望其他功能被禁止
        //一种解决方案就是：创建一个文件读取服务来异步的读取文件，当服务完成之后，向应用程序发送一条消息，而此后的应用程序就可以执行相应的功能

    //接口：通常需要两种方法：1：与任务正常完成有关的 2：调用过程中异常引发的错误
    interface FileReader{
        void read(String fileName,FileReaderClient client);
    }
    interface FileReaderClient{
        void readCompleted(String fileName,byte[]data);
        void readFailed(String fileName, IOException ie);
    }

    //实现
    class FileReaderApp implements FileReaderClient{
        protected FileReader reader=new AFileReader();

        @Override
        public void readCompleted(String fileName, byte[] data) {
            System.out.println(fileName+" is read some data:"+new String(data));
        }

        @Override
        public void readFailed(String fileName, IOException ie) {
            System.out.println(fileName+" is read error :");
            ie.printStackTrace();
        }
    }
    class AFileReader implements FileReader{
        @Override
        public void read(String fileName, FileReaderClient client) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doRead(fileName,client);
                }
            }).start();
        }
        protected void doRead(String fn,FileReaderClient client){
            byte[] buffer=new byte[1024];
            try {
                FileInputStream s=new FileInputStream(fn);
                s.read(buffer);
                if(client!=null){client.readCompleted(fn,buffer);}
            }catch (IOException ie){
                if(client!=null) client.readFailed(fn,ie);
            }
        }
    }

    //保护回调方法：
        //回调方法自己可以包含保护机制，它可以挂起对每个输入的回调处理，直到客户端可以处理它
    class FileApplication implements FileReaderClient{
        protected FileReader reader=new AFileReader();
        private String[] fileNames;
        private int currentCompletion;//index of reday file
        @Override
        public void readCompleted(String fileName, byte[] data) {
            //wait until ready to process this callback
            while(fileName.equals(fileNames[currentCompletion])){
                try {
                    wait();
                }catch (InterruptedException ie){
                    return ;
                }
            }
            //process data..
            //wake up any other thread waiting on this condition
            ++currentCompletion;
            notifyAll();
        }

        @Override
        public void readFailed(String fileName, IOException ie) {
              //similar...
        }

        public synchronized void readFiles(){
            currentCompletion=0;
            for(int i=0;i< fileNames.length;i++){
                reader.read(fileNames[i],this);
            }
        }
    }
}
