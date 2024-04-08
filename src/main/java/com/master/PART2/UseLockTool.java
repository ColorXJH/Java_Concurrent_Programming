package com.master.PART2;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 使用锁工具
 * @date 2024-04-08 9:19
 */
public class UseLockTool {

}
//内部的synchronized方法和块可以满足很多基于锁的应用，但是有下面的限制
    //1：如果某线程试图得到锁，而这个锁已经被其他线程持有，那么就没有办法回退，也没有办法在等待一段时间之后放弃等待
        //或者在某个中断之后取消获取锁的企图，这些使得线程很难从活跃性问题中恢复
    //2：没有办法改变锁的语义形式，例如重入性，读/写保护，公平性方面的改变
    //3：没有对同步的访问控制，任何一个方法都可以对其可访问的对象执行synchronized(obj)操作，这样导致由于所需要的锁
        //已经被占用而引起的拒绝服务问题
    //4：方法和块内的同步，使得只能够对严格的块结构使用锁，例如，不能在一个方法中获得锁，而在另一个方法中释放锁。
//锁工具类提供的结局方案是笨拙的代码模式和更少的对使用正确性的自动保证为代价的，比起synchronized方法或者块来说
//使用锁时应该更加小心，并且要严格遵守相应的规则，这些结构可能需要更大的开销，因为他们比内建的同步更难优化

//Mutex:互斥独占锁：mutual exclusion lock 缩写
class MyMutex implements Sync{
    //遵循 获得-释放 协议的标准接口

    //和同步块的入口操作相似
    @Override
    public void acquire() throws InterruptedException {

    }
    //只有在规定的时间内得到锁才会返回true
    @Override
    public boolean attempt(long msecs) throws InterruptedException {
        return false;
    }
    //和同步块的释放锁操作相似
    @Override
    public void release() {

    }
}
//以上和内建的同步机制不同的是：如果当前线程在试图获取锁的时候被中断，acquire和release方法会抛出InterruptedException异常
    //这一点增加了使用的复杂性但是提供了编写响应良好的健壮代码来处理取消操作的机制
//互斥独占锁可以向内建锁一样使用。如下：
    //synchronized(lock){.....;}
    //替换成如下更长的before/after结构
    //try{
    //      mutex.acquire();
    //      try{
    //          body;
    //      }finally{
    //          mutex.release();
    //      }
    // }catch(InterruptedException e){
    //      response to thread cancellation during acquire
    // }
//和synchronized不同的是：标准Mutex类中的锁不能重入，如果锁已经被执行acquire操作的线程持有，
    //这个线程还会再acquire操作上被阻塞，可以重新定义一个ReentrantLock,一个简单的Mutex类可以满足很多锁的应用
class ParticleUsingMutex{
    protected int x;
    protected int y;
    protected final Random rng=new Random();
    protected final MyMutex myMutex=new MyMutex();

    public ParticleUsingMutex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(){
        try {
            myMutex.acquire();
            try {
                x+=rng.nextInt(10)-5;
                y+= rng.nextInt(20)-10;
            } finally {
                myMutex.release();
            }

        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    public void draw(Graphics graphics){
        int lx,ly;
        try {
            myMutex.acquire();
            try {
                lx=x;
                ly=y;
            } finally {
                myMutex.release();
            }
        }catch (InterruptedException e){
            //在Java中，当一个线程在执行某个方法时发生了InterruptedException（通常是由于另一个线程调用了该线程的interrupt()方法），
            //该线程的中断状态会被清除，即中断状态会被设置为false。这意味着如果在catch块中不对中断状态进行处理，线程可能会继续运行，而不知道自己已经被中断了。
            Thread.currentThread().interrupt();
            return;
        }
        graphics.drawRect(lx,ly,10,20);
    }
}
//内层的try{}finally块结构模拟了synchronized的如下行为:即无论操作体如何退出都会释放锁，即使因为一个没有捕捉到的异常也不例外
//作为一个设计原则，要经常使用try{}catch(){},即使不会抛出异常，这是一个好习惯
    //如果线程在获取锁的过程中被中断，move/draw方法不会执行任何操作而立刻返回，catch语句中需要Thread.currentThread().interrupt();方式传递取消状态

//包装器
class WithMutex{
    private MyMutex mutex;

    public WithMutex(MyMutex mutex) {
        this.mutex = mutex;
    }

    public void perform(Runnable r)throws InterruptedException{
        mutex.acquire();
        try {
            r.run();
        }finally {
            mutex.release();
        }
    }
}
//上述这种方式可以被下面的类使用，他们以内部方法的形式把纯操作分离出来，再在公有方法实现的包装器内部调用这些纯操作，如下：
class ParticleUsingWrapper{
    protected int x;
    protected int y;
    protected final Random rng=new Random();
    protected final WithMutex withMutex=new WithMutex(new MyMutex());
    protected void doMove(){
        x+=rng.nextInt(10)-5;
        y+= rng.nextInt(20)-10;
    }
    public void move(){
        try {
            withMutex.perform(new Runnable() {
                @Override
                public void run() {
                    doMove();
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

//回退 back-off
    //使用attempt而不是acquire,并提供一个估计的超时时间，然后在失败时提供相应的措施
    //如下：当发现潜在的死锁现象时，就将他回退重试，在重试之间试着给出一个短的延迟，因为采用重试，所以可能导致活锁
        //如果程序员认为无限活锁现象比随机的硬件失效出现的概率低，那么这么做就是可以接收的
class CellUsingBackOff{
    private long value;
    private final MyMutex mutex=new MyMutex();
    void swapValue(CellUsingBackOff other){
        if(this==other){
            return;
        }
        for(;;){
            try {
                //获取当前对象的锁
                mutex.acquire();
                try{
                    //尝试获取交换对象的锁
                    if(other.mutex.attempt(0)){
                        //获取到两个对象的锁之后才开始处理数据
                        try{
                            long t=value;
                            value= other.value;
                            other.value=t;
                            return;
                        }finally {
                            other.mutex.release();
                        }
                    }
                    //未成功则释放当前对象锁
                }finally {
                    mutex.release();
                }
                //沉睡0.1秒  等待下一次循环
                Thread.sleep(100);
            }catch (InterruptedException exception){
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

//重新排序
    //如果在使用排序锁技术的设计中，某一种层次结构的排序锁会有最少的异常发生，那么回退操作是一种很好的用来找出这种特定层次结构的方法，在这种抢矿下
    //需要获得多个锁的代码可以以某种顺序获得锁，如果失败了就释放所有的锁，然后再以另一种顺序重试
        //以下展示了：使用了避免死锁而排列组合锁的访问顺序和策略，当失败时，他会以相反的顺序锁住对象
class CellUsingReorderedBackOff{
    private long value;
    private final MyMutex mutex=new MyMutex();
    private static boolean trySwap(CellUsingReorderedBackOff a,CellUsingReorderedBackOff b)throws InterruptedException{
        boolean success=false;
        if(a.mutex.attempt(0)){
            try {
                if(b.mutex.attempt(0)){
                    try {
                        long t=a.value;
                        a.value= b.value;
                        b.value=t;
                        success=true;
                    }finally {
                        b.mutex.release();
                    }
                }
            }finally {
                a.mutex.release();
            }
        }
        return success;
    }

    void swapValue(CellUsingReorderedBackOff other){
        if(this==other){return;}
        try{
            //它通过尝试以不同的顺序获取两个对象的锁来交换它们的值。如果第一种尝试失败，它会尝试以相反的顺序获取锁。
            //这种策略可以防止死锁的发生，因为它保证了获取锁的顺序。
                //如果交换成功（条件1返回true,会直接返回，或者条件1失败，返回false,继续判断条件2）
            while (!trySwap(this,other)&&!trySwap(other,this))
                //如果交换失败，它会等待一段时间后重试
                Thread.sleep(100);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
}

//非阻塞结构的锁
    //当获得/释放锁操作不能再同一个方法或者代码块中进行的时候，不能使用synchronized块，这时可以使用哦个Mutex
    //例如：在链表的各个节点之间进行遍历的时候，程序员可以使用Mutex实现锁传递（也称锁连接），这种情况下，要求在持有当前节点锁的同时
        //获得下一个节点的锁，但是，在获得下一个节点的锁之后，就可以释放当前的锁了
    //锁传递遍历允许非常细粒度的锁，这样也就增加了前在的并发性，但是这个是以额外的复杂性和负载为代价的，而这些代价只有在极度竞争资源的情况下才有意义
class ListUsingMutex{
    static class Node{
        Object item;
        Node next;
        MyMutex mutex=new MyMutex();//每个节点都持有自己的锁
        Node(Object o,Node n){
            item=o;
            next=n;
        }
    }
    protected Node head;//指向list第一个节点的指针
    protected Node tail; // 指向list最后一个节点的指针
    //使用内部同步去保护head域，当然也可以使用Mutex来代替，但是并没有什么必要
    protected synchronized Node getHead(){
        return head;
    }
    //从头部开始添加
    public synchronized void addHead(Object x){
        if(x==null){throw new IllegalArgumentException();}
        //同步保护head域，
        head=new Node(x,head);
    }
    //从尾部开始添加
    public synchronized void addTail(Object x) {
        if (x == null) {
            throw new IllegalArgumentException();
        }
        Node newNode = new Node(x, null);
        if (head == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode; // 更新尾节点
    }
    boolean search(Object x)throws InterruptedException{
        Node p=getHead();
        if(p==null||x==null){return false;}
        p.mutex.acquire();
        for(;;){
            Node nextp=null;
            boolean found;
            try{
                found=x.equals(p.item);
                if(!found){
                    nextp=p.next;
                    if(nextp!=null){
                        try{
                            nextp.mutex.acquire();
                        }catch (InterruptedException e){
                            throw e;
                        }
                    }
                }
            }finally {
                p.mutex.release();
            }
            if(found){
                return true;
            }
            else if(nextp==null){
                return false;
            }else{
                p=nextp;
            }
        }
    }
}

//锁的顺序化管理器
    //当必须按照某个顺序获得锁时，可以通过把顺序化方法集中在一个锁管理类中来保证与规则一致
class LockManager{
    protected void sortLocks(Sync[] locks){
        //...
    }
    public void runWithinLock(Runnable r,Sync[] locks)throws InterruptedException{
        sortLocks(locks);
        int lastLock=-1;
        InterruptedException caught=null;
        try{
            for(int i=0;i<locks.length;i++){
                locks[i].acquire();
                lastLock=i;
            }
            r.run();
        }catch (InterruptedException ie){
            caught=ie;
        }finally {
            for(int j=lastLock;j>=0;--j){
                locks[j].release();
            }
            if(caught!=null){
                throw caught;
            }
        }
    }
}

//读写锁
    //ReadWriteLocks中保存着一对相关的锁，下面是他们的一种定义
interface MyReadWriteLock{
    Sync readLock();
    Sync writeLock();
}
//读写锁的设计思想是：只要没有写线程，readLock就可以同时被多个读线程控制，writeLock是独占的，
    //以下情况，读写锁相对于普通Mutex是更好的选择
        //1:类中的方法可以彻底的分为只读内部数据的方法和只写内部数据的方法
        //2：当写方法运行的时候，不允许读操作进行，（如果写的时候允许读那么程序可以使用非同步的读方法，或者复制写作为更新数据的方法）---保证写操作的完备性
        //3：应用场景中一般的读操作多于写操作
        //4：读写锁有一些额外的开销，但是却可以允许多个线程同时读
class RWLock implements ReadWriteLock{
    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();
    @Override
    public Lock readLock() {

        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }
}
class DataRepository{
    protected final ReadWriteLock rw=new RWLock();

    public void access()throws InterruptedException{
        rw.readLock().lock();
        try{
            //read data
        }finally {
            rw.readLock().unlock();
        }
    }

    public void modify()throws InterruptedException{
        rw.writeLock().lock();
        try{
            //write data
        }finally {
            rw.writeLock().unlock();
        }
    }
}

//读写锁在普通的集合类中很有用，juc包中包含了一组适配器类，这些类可以和Collection类一起使用，在纯访问的方法中(例如contains)放置读锁，
    //在更新数据的方法中（add）放置写锁
