package com.master.PART3;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import javafx.beans.binding.ObjectExpression;
import javafx.scene.control.SeparatorMenuItem;
import sun.security.krb5.internal.PAData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 使用并发工具类
 * @date 2024-05-17 11:20
 */
public class UseConcurrentTools {
    //1:计数信号机 counting semaphore
        //1.1 互斥锁：信号机可以被用于实现互斥锁，只需要将初始化的许可证数置为1即可
    class Mutex implements Sync{
        private Semaphore semaphore=new Semaphore(1);
        @Override
        public void acquire() throws InterruptedException {
            semaphore.acquire();
        }

        @Override
        public boolean attempt(long msecs) throws InterruptedException {
            return semaphore.tryAcquire(msecs, TimeUnit.MILLISECONDS);
        }

        @Override
        public void release() {
            semaphore.release();
        }
    }
    //相对于被用作锁，信号机在计数和发信号时更有价值


    //资源池
    /**
     * try{
     *      Object r=pool.getItem();
     *      try{
     *          use(r);
     *      }finally{
     *          pool.returnIterm(r)
     *      }
     * }catch(InterruptedException ie){
     *
     * }
     */
    class Pool{
        protected ArrayList items=new ArrayList();
        protected HashSet busy=new HashSet();
        protected final Semaphore available;
        public Pool(int n){
            available=new Semaphore(n);
            initializeItems(n);
        }

        public Object getItem() throws InterruptedException{
            available.acquire();
            return doGet();
        }
        public void returnItem(Object x){
            if(doReturn(x)){
                available.release();
            }
        }

        protected void initializeItems(int n){
            for(int i=0;i<n;i++){
                items.add(i,new Object());
            }
        }
        protected synchronized Object doGet(){
            Object x=items.remove(items.size()-1);
            busy.add(x);
            return x;
        }
        protected synchronized boolean doReturn(Object x){
            if(busy.remove(x)){
                items.add(x);
                return true;
            }
            else return false;
        }


    }

    //有界缓冲区：基于一下思想设计一个BoundedBuffer
        //1:最初，对于一个大小为n的缓冲区来说，有n个放入许可证和0个取出许可证
        //2:一个take操作必须先获取一个取出许可证，随后释放一个放入许可证
        //3:一个put操作必须先获取一个放入许可证，随后释放一个取出许可证
    class BufferArray{
        protected final Object[] array;
        protected int putPtr=0;
        protected int takePtr=0;
        BufferArray(int n){
            array=new Object[n];
        }

        synchronized void insert(Object x){
            array[putPtr]=x;
            putPtr=(putPtr+1)%array.length;
        }
        synchronized Object extract(){
            Object x=array[takePtr];
            array[takePtr]=null;
            takePtr=(takePtr+1)%array.length;
            return x;
        }
    }

    class BoundedBufferWithSemaphores{
        protected final BufferArray buff;
        protected final Semaphore putPermits;
        protected final Semaphore takePermits;
        BoundedBufferWithSemaphores(int capacity){
            if(capacity<=0) throw new IllegalArgumentException();
            buff=new BufferArray(capacity);
            putPermits=new Semaphore(capacity);
            takePermits=new Semaphore(0);
        }

        public void put(Object x)throws InterruptedException{
            putPermits.acquire();
            buff.insert(x);
            takePermits.release();
        }
        public Object take()throws InterruptedException{
            takePermits.acquire();
            Object extract = buff.extract();
            putPermits.release();
            return extract;
        }
        //变体，支持阻塞0 或者超时策略
        public Object poll(long msecs) throws InterruptedException{
            if(!takePermits.tryAcquire(msecs,TimeUnit.MICROSECONDS)) return null;
            Object  x=buff.extract();
            putPermits.release();
            return x;
        }
        public boolean offer(Object x,long times)throws InterruptedException{
            if(!putPermits.tryAcquire(times,TimeUnit.MICROSECONDS)){return false;}
            buff.insert(x);
            takePermits.release();
            return true;
        }
    }

    //同步通道
    interface Channel{
        void put(Object x)throws InterruptedException;
        Object take()throws InterruptedException;
    }
    //信号机可以被用来实现同步通道，可以使用与有界缓冲区相同的方法，再加上一个信号机以确保只有当put操作所提供的某项内容被取走之后，这个put操作才可以执行
    class SynchronousChanel implements Channel{
        protected  Object item=null;
        protected final Semaphore putPermit;
        protected final Semaphore takePermit;
        protected final Semaphore taken;

        public SynchronousChanel(){
            putPermit=new Semaphore(1);
            takePermit=new Semaphore(0);
            taken=new Semaphore(0);
        }
        @Override
        public void put(Object x) throws InterruptedException {
            putPermit.acquire();
            item=x;
            takePermit.release();
            InterruptedException caught=null;
            for(;;){
                try{
                    taken.acquire();
                    break;
                }catch (InterruptedException ie){
                    caught=ie;
                }
            }
            if(caught!=null){
                throw caught;
            }
        }

        @Override
        public Object take() throws InterruptedException {
            takePermit.acquire();
            Object x=item;
            item=null;
            putPermit.release();
            taken.release();
            return x;
        }
    }
    //公平性与调度
    //闭锁：latch:一旦获得某个值就再也不会变化的变量或者条件，二元闭锁变量或者条件的值只能更改一次，即从初始化状态到最终状态，一个release操作将使得所有之前或者之后的acquire操作恢复执行

    //闭锁可以帮助解决以下的初始化问题，直到所有的对象和线程都完全构造完成之后，某些操作才可以执行
    class Player implements Runnable{
        protected final CountDownLatch latch;
        Player(CountDownLatch countDownLatch){
            latch=countDownLatch;
        }
        @Override
        public void run() {
              try {
                latch.await();
                play();
              }catch (InterruptedException interruptedException){

              }
        }
        protected void play(){
            System.out.println(Thread.currentThread().getName()+"现在可以开始游戏了"+System.currentTimeMillis());
        }
    }
    class Game{
        void begin(int players) throws InterruptedException{
            CountDownLatch latch=new CountDownLatch(1);
            for(int i=0;i<players;i++){
                new Thread(new Player(latch)).start();
            }
            System.out.println("现在所有人都加载进游戏了，准备开始。。。。");
            latch.countDown();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        //Game games=new UseConcurrentTools().new Game();
        //games.begin(20);
        FillAndEmpty fillAndEmpty=new UseConcurrentTools().new FillAndEmpty();
        fillAndEmpty.start();
    }


    //闭锁变量与闭锁谓词
        //闭锁谓词是很少见的几个可以用非同步忙等待循环来实现手保护方法的选择之一，如果已知一个谓词是闭锁，那么就不会有错误状态的风险
    class LatchingThermometer{
        //类中所有的相关变量都被定义为volatile或者他们的读写都再同步下执行
        private volatile boolean ready;
        private volatile float temperature;

        public double getReading(){
            while (!ready)Thread.yield();
            return temperature;
        }
        void sense(float t){
            temperature=t;
            ready=true;
        }
    }
    //交换器
        //不同线程中的两个或者两个以上的任务，每个任务再任何时间只能操作一个资源，当一个线程使用完一个资源而需要获得其他资源时，它就和其他的线程交换，
        //这个协议最常见的应用就是缓冲区交换，再这种应用中，一个线程向缓冲区中填充数据，（例如通过读入数据），当这个缓冲区被填满时，这个线程就把他交换给
        //处理缓冲区的线程，由这个线程来清空缓冲区，再这种方法中，只使用两个缓冲区且不需要拷贝，因此不再需要资源管理池了
    class FillAndEmpty{
        static final int SIZE=1024;
        protected Exchanger exchanger=new Exchanger();
        protected byte readByte(){
            return (byte) new Random().nextInt();
        }
        protected void useByte(byte b){
            System.out.println("使用这个值.."+b);
        }
        public void start(){
            new Thread(new FillingLoop()).start();
            new Thread(new EmptyingLoop()).start();
        }

        class FillingLoop implements Runnable{
            @Override
            public void run() {
                byte[] buffer=new byte[SIZE];
                int position=0;
                try {
                    for(;;){
                        if(position==SIZE){
                            buffer= (byte[]) exchanger.exchange(buffer);
                            position=0;
                        }
                        buffer[position++]=readByte();
                    }
                } catch (InterruptedException interruptedException){

                }
            }
        }
        class EmptyingLoop implements Runnable{
            @Override
            public void run() {
                byte[] buffer=new byte[SIZE];
                int position=SIZE;
                try {
                    for(;;){
                        if(position==SIZE){
                            buffer= (byte[]) exchanger.exchange(buffer);
                            position=0;
                        }
                        useByte(buffer[position++]);
                    }
                }catch (InterruptedException ie){}
            }
        }
    }

    //条件变量
    class CondVar{
        protected final Sync mutex;
        public CondVar(Sync lock){
            mutex=lock;
        }
        public void await()throws InterruptedException{}
        public boolean timeWait(long ms)throws  InterruptedException{return false;}
        public void signal(){}
        public void broadcast(){}

    }
}
