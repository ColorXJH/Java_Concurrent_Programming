package com.master.PART4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Queue;

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






















}
