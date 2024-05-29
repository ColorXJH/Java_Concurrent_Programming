package com.master.PART4;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 创建线程
 * @date 2024-05-24 17:10
 */
public class CreateThread {
    //new Thread(aRunnable).start()
        //基于任务和基于参与者的模式
    class Handler{
        void process(Socket s){
            DataInputStream in=null;
            DataOutputStream out=null;
            try{
                in=new DataInputStream(s.getInputStream());
                out=new DataOutputStream(s.getOutputStream());
                int request=in.readInt();
                int result=-request;
                out.writeInt(result);

            }catch (IOException e){}
            finally {
                try{if(in!=null)in.close();}
                catch (IOException e){}
                try{if(out!=null)out.close();}
                catch (IOException e){}
                try{s.close();}
                catch (IOException e){}
            }
        }
    }

    class Webservice implements Runnable{
        static final int PORT=1040;
        Handler handler=new Handler();
        @Override
        public void run() {
            try{
                ServerSocket socket=new ServerSocket(PORT);
                for(;;){
                    final Socket connect=socket.accept();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handler.process(connect);
                        }
                    }).start();
                }
            }catch (IOException e){

            }
        }

    }
    //单向消息
        //事件：如鼠标单击等等
        //通知：如状态改变，警告
        //记录：邮件消息，股票报价
        //激活：创建小程序，后台线程
        //命令：打印请求
        //中继：消息转发和分派

    //1:开放调用
    //2：每一个消息一个线程
    //3：执行者
    interface Executor{
        void execute(Runnable runnable);
    }
    class PlainThreadExecutor implements Executor{

        @Override
        public void execute(Runnable runnable) {
            new Thread(runnable).start();
        }
    }
    class Helper{
        void handle(){}
    }
    class HostWithExecutor{
        protected long localState;
        protected final Helper helper=new Helper();
        protected final Executor executor;
        public HostWithExecutor(Executor executor){
            this.executor=executor;
        }
        protected synchronized void updateState(){
            localState=200;
        }
        public void req(){
            updateState();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    helper.handle();
                }
            });
        }


    }


    //工作者线程 worker thread
    class PlainWorkerPool implements Executor{
        protected final Queue workQueue;
        public PlainWorkerPool(Queue channel,int workers){
            workQueue=channel;
            for(int i=0;i<workers;i++){
                active();
            }
        }
        protected void active(){
            Runnable runLoop=new Runnable() {
                @Override
                public void run() {
                        for(;;){
                            Runnable r= (Runnable) workQueue.poll();
                            r.run();
                        }
                }
            };
            new Thread(runLoop).start();
        }
        @Override
        public void execute(Runnable runnable) {

        }
    }

    //设计选择
    class TimerDaemon{
         class TimerTask implements Comparable{
            final Runnable command;
            final long execTime;
            public TimerTask(Runnable runnable,long time){
                command=runnable;execTime=time;
            }
            @Override
            public int compareTo(Object o) {
                long otherExecTime=((TimerTask)o).execTime;
                return execTime<otherExecTime?-1:execTime==otherExecTime?0:1;
            }

        }
        class PriorityQueue{
             void put(TimerTask t){}
            TimerTask least(){return null;}
            void removeLeast(){};
            boolean isEmpty(){return false;}


        }
        protected final PriorityQueue pq=new PriorityQueue();
        public synchronized void executeAfterDelay(Runnable r,long t){
            pq.put(new TimerTask(r,t+System.currentTimeMillis()));
            notifyAll();
        }
        public synchronized void executeAt(Runnable r, Date time){
            pq.put(new TimerTask(r,time.getTime()));
            notifyAll();
        }
        protected synchronized Runnable take()throws InterruptedException{
            for(;;){
                while (pq.isEmpty())wait();
                TimerTask t=pq.least();
                long now=System.currentTimeMillis();
                long waitTime=now-t.execTime;
                if(waitTime<=0){
                    pq.removeLeast();
                    return t.command;
                }else{
                    wait(waitTime);
                }
            }
        }
        public TimerDaemon(){
            active();
        }
        void active(){

        }

    }

    //轮询和事件驱动的io
        //事件驱动的任务
    class SessionTask implements Runnable{
        private static final int BUFFSIZE=1024;
        protected final Socket socket;
        protected final InputStream input;
        SessionTask(Socket s)throws IOException{
            socket=s;
            input=socket.getInputStream();
        }
        void processCommand(byte[] buffer,int bytes){}
        void cleanup(){}

        @Override
        public void run() {
            byte[] commandBuffer=new byte[BUFFSIZE];
            try{
              for(;;){
                  int bytes=input.read(commandBuffer,0,BUFFSIZE);
                  if(bytes!=BUFFSIZE){
                      break;
                  }
                  processCommand(commandBuffer,bytes);
              }
            }
            catch (IOException ie){
                cleanup();
            }
            finally {
                try {
                    input.close();socket.close();
                }catch (IOException ignore){

                }
            }
        }
    }

    //一个基于会话的设计可以通过以下方式转换为一个事件驱动的设计
        //1:隔离基本命令的功能，将其放在重新设计的任务的run方法中，每次先读取命令，然后再执行相关操作
        //2：定义run方法，无论什么时候，当输入可读的时候，（或发生IO异常的情况下）它都可以被重复的触发
        //3：手动维护完整的状态，从而在每个会话完成之后，因为输入已经被耗尽或连接已经被关闭，而实事件操作不再被触发
    class IOEventTask implements Runnable{
        private static final int BUFFSIZE=1024;
        protected final Socket socket;
        protected final InputStream inputStream;
        protected volatile boolean done=false;
        IOEventTask(Socket s)throws IOException{
            socket=s;inputStream=socket.getInputStream();
        }
        void processCommand(byte[] buffer,int bytes){}
        void cleanup(){}
        boolean done(){return done;}
        InputStream input(){return inputStream;}
        @Override
        public void run() {
            if(done)return;
            byte[]commandBuffer=new byte[BUFFSIZE];
            try {
                int bytes=inputStream.read(commandBuffer,0,BUFFSIZE);
                if(bytes!=BUFFSIZE)done=true;
                else processCommand(commandBuffer,bytes);
            }catch (IOException ie){
                cleanup();
                done=true;
            }
            finally {
                if(!done){return;}
                try {
                    inputStream.close();
                    socket.close();
                }catch (IOException IE){

                }
            }
        }
    }

    //触发
    class PollingWorker implements Runnable{
        private List tasks=new ArrayList<>();
        private long sleepTime;
        void register(IOEventTask t){tasks.add(t);}
        void deregister(IOEventTask t){tasks.remove(t);}
        @Override
        public void run() {
            try{
                for(;;){
                    for(Iterator ir=tasks.iterator(); ir.hasNext();){
                        IOEventTask t= (IOEventTask) ir.next();
                        if(t.done)deregister(t);
                        else{
                            boolean  trigger;
                            try{
                                trigger=t.input().available()>0;
                            }catch (IOException e){
                                trigger=true;
                            }
                            if(trigger)t.run();
                        }
                    }
                    Thread.sleep(sleepTime);
                }
            }catch (InterruptedException ie){

            }
        }
    }

    //消息表现
    abstract class Box{
        protected Color color=Color.white;
        public synchronized Color getColor(){return color;}
        public synchronized void setColor(Color c){color=c;}
        public abstract Dimension size();
        public abstract Box duplicate();
        public abstract void show(Graphics g,Point origin);
    }















}
