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

    class BasicBox extends Box{
        protected final Dimension size;
        public BasicBox(int xdim,int ydim){
            size=new Dimension(xdim,ydim);
        }

        @Override
        public synchronized Dimension size() {
            return size;
        }

        @Override
        public void show(Graphics g, Point origin) {
            g.setColor(getColor());
            g.fillRect(origin.x,origin.y, size.width,size.height);
        }

        @Override
        public synchronized Box duplicate() {
            Box p=new BasicBox(size.width,size.height);
            p.setColor(getColor());
            return p;
        }
    }

    abstract class JoinedPair extends Box{
        protected Box fst;
        protected Box snd;
        protected JoinedPair(Box a,Box b){
            fst=a;
            snd=b;
        }
        public synchronized void flip(){
            Box temp=fst;
            fst=snd;
            snd=temp;
        }
    }

    class HorizontallyJoinedPair extends  JoinedPair{
        protected final Dimension size;
        public HorizontallyJoinedPair(Box l,Box r){
            super(l,r);
            size=new Dimension();
        }

        @Override
        public Dimension size() {
            return size;
        }

        @Override
        public synchronized Box duplicate() {
            HorizontallyJoinedPair p=new HorizontallyJoinedPair(fst.duplicate(),snd.duplicate());
            p.setColor(getColor());
            return p;
        }

        @Override
        public void show(Graphics g, Point origin) {

        }

    }

    class VerticallyJoinedPair extends JoinedPair{
        protected VerticallyJoinedPair(Box a, Box b) {
            super(a, b);
        }

        @Override
        public Dimension size() {
            return null;
        }

        @Override
        public Box duplicate() {
            return null;
        }

        @Override
        public void show(Graphics g, Point origin) {

        }
        //similar to upper
    }
    class WrappedBox extends Box{
        protected Dimension wrapperSize;
        protected Box inner;
        public WrappedBox(Box innerBox ,Dimension size){
            inner=innerBox;
            wrapperSize=size;
        }

        @Override
        public Dimension size() {
            return null;
        }

        @Override
        public Box duplicate() {
            return null;
        }

        @Override
        public void show(Graphics g, Point origin) {

        }
    }

    //接口
    interface PushSource{
        void produce();
    }
    interface PushStage{
        void putA(Box p);
    }

    interface DualInputPushStage extends PushStage{
        void pushB(Box p);
    }

    //适配器
    class DualInputAdapter implements PushStage{
        protected final DualInputPushStage stage;
        public DualInputAdapter(DualInputPushStage dualInputPushStage){
            stage=dualInputPushStage;
        }

        @Override
        public void putA(Box p) {
            stage.pushB(p);
        }
    }
    //接收器
    class DevNull implements PushStage{
        @Override
        public void putA(Box p) {

        }
    }
    class SingleOutputPushStage{
        private PushStage next1=null;
        protected synchronized PushStage getNext1(){
            return next1;
        }
        public synchronized void setNext1(PushStage next){
            next1=next;
        }
    }
    class DualOutputPushStage extends SingleOutputPushStage{
        private PushStage next2=null;

        public PushStage getNext2() {
            return next2;
        }

        public void setNext2(PushStage next2) {
            this.next2 = next2;
        }
    }
    //线性阶段
    class Painter extends SingleOutputPushStage implements  PushStage{
        protected final Color color;
        public Painter(Color color){
            this.color=color;
        }
        @Override
        public void putA(Box p) {
            p.setColor(color);
            getNext1().putA(p);
        }
    }
    class Wrapper extends SingleOutputPushStage implements PushStage{
        protected final int ticketness;
        public Wrapper(int ticketness){
            this.ticketness=ticketness;
        }

        @Override
        public void putA(Box p) {
            Dimension d=new Dimension(ticketness,ticketness);
            getNext1().putA(new WrappedBox(p,d));
        }
    }
    class Flipper extends SingleOutputPushStage implements PushStage{
        @Override
        public void putA(Box p) {
            if(p instanceof JoinedPair){
                ((JoinedPair)p).flip();
                getNext1().putA(p);
            }
        }
    }
    //组合器
    abstract class Joiner extends SingleOutputPushStage implements DualInputPushStage{
        protected Box a=null;//incoming form putA
        protected Box b=null;//incoming from putB
        protected abstract Box join(Box a,Box b);

        protected synchronized Box joinFromA(Box p){
            while (a!=null)
                try{
                    wait();
                }catch (InterruptedException ie){
                    return null;
                }
                a=p;
                return tryJoin();
        }
        protected synchronized Box joinFromB(Box p){
            while (b!=null)
                try {
                    wait();
                }catch (InterruptedException interruptedException){
                    return null;
                }
            b=p;
            return tryJoin();
        }

        protected synchronized Box tryJoin(){
            if(a==null||b==null){
                return null;
            }
            Box joined=join(a,b);
            a=b=null;
            notifyAll();
            return  joined;
        }

        @Override
        public void putA(Box p) {
            Box j=joinFromA(p);
            if(j!=null)getNext1().putA(j);
        }

        public void putB(Box b){
            Box j=joinFromB(b);
            if(j!=null)
                getNext1().putA(j);
        }
    }

    class HorizontalJoiner extends Joiner{
        @Override
        protected Box join(Box a, Box b) {
            return new HorizontallyJoinedPair(a,b);
        }

        @Override
        public void pushB(Box p) {

        }
    }
    class VerticalJoiner extends Joiner{

        @Override
        public void pushB(Box p) {

        }

        @Override
        protected Box join(Box a, Box b) {
            return new VerticallyJoinedPair(a,b);
        }
    }

    //收集器
    class Collector extends SingleOutputPushStage implements DualInputPushStage{

        @Override
        public void putA(Box p) {
            getNext1().putA(p);
        }

        @Override
        public void pushB(Box p) {
            getNext1().putA(p);
        }
    }
    //双输出阶段：Dual output stage
        //交替阶段
    class Alternator extends DualOutputPushStage implements PushStage{
        protected boolean outTo2=false;
        protected synchronized boolean testAndInvert(){
            boolean b=outTo2;
            outTo2=!outTo2;
            return b;
        }

        @Override
        public void putA(Box p) {
            if(testAndInvert())
                getNext1().putA(p);
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getNext2().putA(p);
                    }
                }).start();
            }
        }
    }
    //复制阶段
    class Cloner extends DualOutputPushStage implements PushStage{

        @Override
        public void putA(Box p) {
            final Box p2=p.duplicate();
            getNext1().putA(p);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getNext2().putA(p2);
                }
            }).start();

        }
    }

    //筛选阶段：把所有复合某些条件的输入都定向到y一个通道，把其他的输入送到另外的通道
    interface BoxPredicate{
        boolean test(Box p);
    }
    class MaxSizePredicate implements BoxPredicate{
        protected final int max;
        public MaxSizePredicate(int size){
            this.max=size;
        }
        @Override
        public boolean test(Box p) {
            return p.size().height<=max&&p.size().width<=max;
        }
    }
    class Screener extends DualOutputPushStage implements PushStage{
        protected final BoxPredicate predicate;
        public Screener(BoxPredicate p){
            predicate=p;
        }

        @Override
        public void putA(Box p) {
            if(predicate.test(p)){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getNext1().putA(p);
                    }
                }).start();
            }else
                getNext2().putA(p);
        }
    }

    //发送源
        //它产生随机大小的基本盒子，配备一个自主循环的run方法，以重复调用produce方法，并使用随机的生产延时时间
    class BasicBoxSource extends SingleOutputPushStage implements PushSource,Runnable{
        protected final Dimension size;
        protected final int productionTime;
        public BasicBoxSource(Dimension s,int delay){
            size=s;
            productionTime=delay;
        }
        protected Box makeBox(){
            return new BasicBox((int)(Math.random()*size.width)+1,(int)(Math.random()*size.height)+1);
        }
        @Override
        public void produce() {
            getNext1().putA(makeBox());
        }

        @Override
        public void run() {
            try {
                for(;;){
                    produce();
                    Thread.sleep((int)(Math.random()*2*productionTime));
                }
            }catch (InterruptedException ie){

            }
        }
    }



}
