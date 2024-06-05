package com.master.PART4;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

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
            while(!fileName.equals(fileNames[currentCompletion])){
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

    //协作线程
    interface Pic{
        byte[] getImage();
    }
    interface Renderer{
        Pic render(URL src);
    }
    class StandardRender implements Renderer{

        @Override
        public Pic render(URL src) {
            return null;
        }
    }
    class PictureApp{
        private final Renderer renderer=new StandardRender();
        public void show(final URL imageSource){
            class Waiter implements Runnable{
                private Pic  result=null;

                public Pic getResult() {
                    return result;
                }
                @Override
                public void run() {
                    result = renderer.render(imageSource);
                }
            }
            Waiter waiter=new Waiter();
            Thread t=new Thread(waiter);
            t.start();
            //display others...
            try{
                t.join();
            }catch (InterruptedException ie){
                //cleanup
                return;
            }
            Pic pic=waiter.getResult();
            if(pic!=null){
                //display image
            }else{
                return ;
                //deal with assumed rendering failure
            }

        }
    }
    //不管线程是成功完成还是异常终止，Thread.join方法都将控制权交给调用者

    //Future
    class AsynchRender implements Renderer{
        private final Renderer renderer=new StandardRender();
        class FuturePic implements Pic{
            private Pic pic=null;
            private boolean ready=false;

            public synchronized void setPic(Pic pic) {
                this.pic = pic;
                ready=true;
                notifyAll();
            }

            @Override
            public byte[] getImage() {
                while (!ready)
                    try{
                        wait();//显式的等待通知操作，而没有使用Thread.join
                    }
                    catch (InterruptedException ie){
                        return null;
                    }
                return pic.getImage();
            }
        }
        @Override
        public Pic render(URL src) {
            final FuturePic p=new FuturePic();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    p.setPic(renderer.render(src));
                }
            }).start();
            return p;
        }
    }

    class PictureAppWithFuture{
        private final Renderer render=new AsynchRender();
        public void show(final URL imageSource){
            Pic pic=render.render(imageSource);
            //display others...
            byte[] im=pic.getImage();
            if(im!=null){
                //display image
            }else{
                //deal with assumed rendering failure
            }
        }
    }

    //callable 接口
    class FutureResult{
        protected Object value=null;
        protected boolean ready=false;
        protected InvocationTargetException exception=null;
        public synchronized Object get() throws InterruptedException,InvocationTargetException{
            while (!ready)wait();
            if(exception!=null){
                throw exception;
            }else return value;
        }
        public Runnable setter(final Callable function){
            return new Runnable() {
                @Override
                public void run() {
                    try{
                        set(function.call());
                    }
                    catch (Throwable e){
                        setException(e);
                    }
                }
            };
        }

        synchronized void set(Object result){
            value=result;
            ready=true;
            notifyAll();
        }
        synchronized void setException(Throwable e){
            exception=new InvocationTargetException(e);
            ready=true;
            notifyAll();
        }

    }
    //调度服务 scheduling service   磁盘的读写；很多柱面，只有一个控制读写的磁头
    class Failure extends Exception{}
    interface Disk{
        void read(int number,byte[] buffer)throws Failure;
        void write(int number,byte[] buffer)throws Failure;
    }
    //电梯算法
    abstract class DiskTask implements Runnable{
        protected final int cylinder;
        protected final byte[] buffer;
        protected Failure exception=null;
        protected DiskTask next=null;
        protected final CountDownLatch done=new CountDownLatch(1);
        DiskTask(int c,byte[] b){
            cylinder=c;buffer=b;
        }
        abstract void access()throws Failure;
        public void run(){
            try{
                access();
            }
            catch (Failure e){
                setException(e);
            }finally {
                done.countDown();
            }
        }
        void awaitCompletion()throws InterruptedException{
            done.await();
        }

        public synchronized Failure getException() {
            return exception;
        }

        public synchronized void setException(Failure exception) {
            this.exception = exception;
        }
    }

    class DiskReadTask extends DiskTask{
        DiskReadTask(int b,byte[] c){
            super(b,c);
        }
        @Override
        void access() throws Failure {

        }
    }
    class DiskWriteTask extends DiskTask{
        DiskWriteTask(int b,byte[] c){
            super(b,c);
        }
        @Override
        void access() throws Failure {

        }
    }

    class DiskTaskQueue{
        protected DiskTask thisSweep=null;
        protected DiskTask nextSweep=null;
        protected int currentCylinder=0;
        protected final Semaphore available=new Semaphore(0);
        void put(DiskTask t){
            insert(t);
            available.release();
        }
        DiskTask take()throws InterruptedException{
            available.acquire();
            return extract();
        }
        synchronized void insert(DiskTask t){
            DiskTask q;
            if(t.cylinder>=currentCylinder){
                q=thisSweep;
                if(q==null){thisSweep=t;return ;}
            }else{
                q=nextSweep;
                if(q==null){nextSweep=t;return;}
            }
            DiskTask trail=q;
            q=trail.next;
            for(;;){
                if(q==null||t.cylinder<q.cylinder){
                    trail.next=t;t.next=q;return;
                }else{
                    trail=q;q=q.next;
                }

            }

        }
        synchronized DiskTask extract(){
            if(thisSweep==null){
                thisSweep=nextSweep;
                nextSweep=null;
            }
            DiskTask t=thisSweep;
            thisSweep=t.next;
            currentCylinder=t.cylinder;
            return t;
        }
    }
    class ScheduledDisk implements Disk{
        protected final DiskTaskQueue tasks=new DiskTaskQueue();
        public void read(int c,byte[] b)throws Failure{

        }
        public void write(int c,byte[] b)throws Failure{

        }
        protected void readOrWrite(DiskTask t)throws Failure{
            tasks.put(t);
            try {
                t.awaitCompletion();
            }catch (InterruptedException ie){
                Thread.currentThread().interrupt();
                throw new Failure();
            }
            Failure f=t.getException();
            if(f!=null) throw f;
        }
        public ScheduledDisk(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        for(;;){
                            tasks.take().run();
                        }
                    }
                    catch (InterruptedException ie){

                    }
                }
            }).start();
        }
    }
}
