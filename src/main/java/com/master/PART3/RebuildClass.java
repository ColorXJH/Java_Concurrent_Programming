package com.master.PART3;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;

import java.util.Hashtable;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 类的构建与重构
 * @date 2024-05-14 16:54
 */
public class RebuildClass {
    //为了避免在任何实例变量都发生变化时都发出通知，你可以在仅在线程所等待的逻辑状态发生变化时再发出通知，
    //1：通道与有界缓冲区
    interface Channel{
        void put(Object x) throws InterruptedException;
        Object take() throws InterruptedException;
    }
    interface BoundedBuffer extends Channel{
        int capacity();//>0
        int size();//0<=size<=capacity
    }
    //以下版本的BoundedBuffer所发出的通知次数远远少于那种每次缓冲区可用大小发生改变后都发送消息（这可能会导致毫无必要的线程唤醒）
    //，在那些重新校验保障的开销很大的情况下，通过这种方法来减少保障校验的次数可以极大的提高性能
    class BoundedBufferWithStateTracking{
        protected final Object[] array;
        protected int putPtr=0;
        protected int takePtr=0;
        protected int usedSlots=0;
        public BoundedBufferWithStateTracking(int capacity) throws IllegalArgumentException{
            if(capacity<=0){throw new IllegalArgumentException();}
            array=new Object[capacity];
        }
        public synchronized int size(){
            return usedSlots;
        }

        public int capacity(){
            return array.length;
        }

        public synchronized void put(Object x)throws InterruptedException{
            while (usedSlots== array.length)wait();
            array[putPtr]=x;
            putPtr=(putPtr+1)% array.length;//循环递增
            if(usedSlots++==0){
                notifyAll();
            }
        }
        public synchronized Object take() throws InterruptedException{
            while (usedSlots==0)wait();
            Object x=array[takePtr];
            array[takePtr]=null;
            takePtr=(takePtr+1)/array.length;
            if(usedSlots--== array.length){
                notifyAll();
            }
            return x;
        }

    }
    //状态变量
    class BoundedCounterWithStateVariable{
        static final int BOTTOM=0,MIN=0,MAX=100,MIDDLE=1,TOP=2;
        protected int state=BOTTOM;
        protected long count=MIN;
        protected void updateState(){
            int oldState=state;//记录更新前的状态
            if(count==MIN)state=BOTTOM;
            else if(count==MAX)state=TOP;
            else state=MIDDLE;
            if(state!=oldState&&oldState!=MIDDLE){
                notifyAll();
            }
        }

        public synchronized long count(){
            return count;
        }

        public synchronized void inc() throws InterruptedException{
            while(state==TOP){
                wait();
            }
            ++count;
            updateState();
        }

        public synchronized void dec() throws InterruptedException{
            while (state==BOTTOM)wait();
            --count;
            updateState();
        }
    }
    //冲突集合
        //不能同时出现方法对的集合，比如：{(store,retrieve),(retrieve,retrieve)}
        //基于冲突集合的类可以使用before/after这种设计，即基本操作被那些维护着独占关系的代码所环绕，下面的机制可以通过任何的before/after模式实现
            //1：对于每个方法，定义一个计数变量，用以标识该方法是否在执行中
            //2：把每个基本操作都隔离入非公共的方法中
            //3：编写那些基本操作的公共版本，即在那些基本操作的前后添加上before/after的控制
                //1：每个同步的before操作都必须先等待所有非冲突的方法结束，这可从计数变量得知，随后before操作增加与该方法相关的计数变量的值
                //2：每个同步的after操作减少该方法的计数变量的值，并发出通知以唤醒等待中的线程
    class Inventory{
        protected final Hashtable items=new Hashtable();
        protected final Hashtable suppliers=new Hashtable();
        protected int storing =0;
        protected int retrieving=0;
        protected void doStore(String description,Object item,String supplier){
            items.put(description,item);
            suppliers.put(supplier,description);
        }

        protected Object doRetrieve(String description){
            Object x=items.get(description);
            if(x!=null){
                items.remove(description);
            }
            return x;
        }
        //存取不能同时  存存可以同时
        public void store(String description,Object item,String supplier)throws InterruptedException{
            synchronized (this){//before-action
                while (retrieving!=0)wait();
                ++storing; //记录执行状态
            }
            try{
                doStore(description,item,supplier);
            }finally {//after-action
                synchronized (this){
                    if(--storing==0){
                        notifyAll();
                    }
                }
            }
        }
        //取存不能同时 取取不能同时
        //retrieve操作不应该何store操作并发执行，因为store方法正在存入的对象可能就是正在被请求出的对象，而你并不想为此返回错误数据
        //两个或两个以上的retrieve操作不应该同时执行，因为其中一个操作正在取出的对象可能正式另一个操作所请求的
        public Object retrieve(String description) throws InterruptedException{
            synchronized (this){//before-action
                while (storing!=0||retrieving!=0)wait();
                ++retrieving;
            }
            try{
               return doRetrieve(description);
            }finally {//after-action
                synchronized (this){
                    if(--retrieving==0){
                        notifyAll();
                    }
                }
            }
        }
    }
    //变种及扩展
        //也可以用在乐观方法里，在其中冲突被称为无效关系，他们通常这样被实现：在事务提交前中断冲突的操作，而不是一直等到安全时才继续执行这些操作
        //当retrieve方法正在执行中时，store方法可以开始执行，但是反过来却不行，及某个方法a仅在c执行后才会同b冲突

    //构建子类
        //读出者与写入者：是这样一组并发构建：他么有着相同的基础，但是一个负责控制那些执行读出操作的线程，另一个负责控制那些执行写入操作的线程
        //他们并发控制的策略不一样
    //以下版本是通用的，其子类基本不需要做太多的修改，值得一提的是，对等待读出者的计数在这个版本中实际上是没有必要的,因为实际上并没有依赖于他的策略，
    //但是由于它的存在，你可以通过让allowReader和allowWriter方法中的谓词依赖于这个值，来调整控制策略，例如，你可以修改这些方法中的条件，来决定
    //优先处理等待队列中计数器值更大的线程
    abstract class ReadWrite{
        protected int activeReaders=0;//threads executing reader
        protected int activeWriters=0;//always zero or one
        protected int waitingReaders=0;//thread not yet in read
        protected int waitingWriters=0;//some for write
        protected abstract void doRead();//implement in subclasses
        protected abstract void doWrite();
        public void read()throws InterruptedException{
            beforeRead();
            try {
                doRead();
            }finally {
                afterRead();
            }
        }
        public void write()throws InterruptedException{
            beforeWrite();
            try{
                doWrite();
            }finally {
                afterWrite();
            }
        }
        //如果当前有等待中的写入者，读出者线程将被阻塞，写入者不可以降级为读出者
        protected boolean allowReader(){
            return waitingWriters==0&&activeWriters==0;
        }
        //如果当前存在活动的读出者线程和写人者线程，该写入者线程将被阻塞
        protected boolean allowWriter(){
            return activeReaders==0&&activeWriters==0;
        }
        protected synchronized void beforeRead() throws InterruptedException{
            ++waitingReaders;
            while (!allowReader()){
                try{
                    wait();
                }catch (InterruptedException ie){
                    --waitingReaders;//rollback state
                    throw ie;
                }
            }
            --waitingReaders;
            ++activeReaders;
        }
        protected synchronized void afterRead(){
            --activeReaders;
            notifyAll();
        }
        protected synchronized void beforeWrite()throws InterruptedException{
            ++waitingWriters;
            while (!allowWriter()){
                try {
                    wait();
                }catch (InterruptedException ie){
                    --waitingWriters;
                    throw ie;
                }
            }
            --waitingWriters;
            ++activeWriters;
        }
        protected synchronized void afterWrite(){
            --activeWriters;
            notifyAll();
        }

    }
    //这个类或它的子类可以被重新包装以下以支持ReadWriteLock接口，可以通过内部类来实现（类似的策略也被用在juc的ReadWriteLock中，不过其中包含了一些技巧来避免不必要的通知）
    interface MyReadWriteLock{
        Sync readLock();
        Sync writeLock();
    }
    class RWLock extends ReadWrite implements MyReadWriteLock {
        class RLock implements Sync {

            @Override
            public void acquire() throws InterruptedException {
                beforeRead();
            }

            @Override
            public boolean attempt(long msecs) throws InterruptedException {
                return beforeReade(msecs);
            }

            @Override
            public void release() {
                afterRead();
            }
        }

        class WLock implements Sync{

            @Override
            public void acquire() throws InterruptedException {
                beforeWrite();
            }

            @Override
            public boolean attempt(long msecs) throws InterruptedException {
                return beforeWrite(msecs);
            }

            @Override
            public void release() {
                afterWrite();
            }
        }
        public boolean beforeReade(long times) throws InterruptedException{
            ++waitingReaders;
            while (!allowReader()){
                try{
                    wait();
                }catch (InterruptedException ie){
                    --waitingReaders;//rollback state
                    throw ie;
                }
            }
            --waitingReaders;
            ++activeReaders;
            return true;
        }
        public boolean beforeWrite(long times) throws InterruptedException{
            ++waitingWriters;
            while (!allowWriter()){
                try {
                    wait();
                }catch (InterruptedException ie){
                    --waitingWriters;
                    throw ie;
                }
            }
            --waitingWriters;
            ++activeWriters;
            return true;
        }
        @Override
        protected void doRead() {
            System.out.println("read is running...");
        }

        @Override
        protected void doWrite() {
            System.out.println("write is running...");
        }
        protected final RLock rlock=new RLock();
        protected final WLock wLock=new WLock();

        @Override
        public Sync readLock() {
            return rlock;
        }

        @Override
        public Sync writeLock() {
            return wLock;
        }
    }

    //将保障分层
        //保障可以被添加到那些被编写为阻碍模式的基本数据结构中
    class StackEmptyException extends Exception{}
    class Stack{
        public synchronized boolean isEmpty(){
            //blablabla
            return true;
        }
        public synchronized void push(Object x){
            //blablabla
        }
        public synchronized Object pop()throws StackEmptyException{
            if(isEmpty()){
                throw new StackEmptyException();
            }
            return null;
        }
    }

    class WaitingStack extends Stack{
        //被覆盖以通知那些被waitingPop所阻塞的线程
        @Override
        public synchronized void push(Object x) {
            super.push(x);
            notifyAll();
        }
        public synchronized Object waitingPop() throws InterruptedException{
            while (isEmpty())wait();
            try {
                return super.pop();
            }catch (StackEmptyException ie){
                throw new Error("Internal implementation error");
            }
        }
    }

    //避免某些些做常见的扩展性之一就是：把保障和通知都封装在可覆盖的方法中，然后按照如下的形式构建公共方法：
    /**
     * public synchronized void anAction(){
     *     awaitForThisAction();
     *     doAction();
     *     NotifyOtherByThisAction();
     * }
     */

    //限制及嵌套的监视器(潜在死锁问题)
    class PartWithGuard{
        protected boolean cond=false;
        //这里可能造成死锁，一个线程进来之后释放了锁处于等待状态，但是外部的锁一直跟随这个等待的线程没有被释放，所以外部的锁一直无法被其他线程获取
        synchronized void await()throws InterruptedException{
            while (!cond)wait();
            //any other code
        }
        synchronized void signal(boolean c){
            cond=c;
            notifyAll();
        }
    }

    class Host{
        protected final PartWithGuard part=new PartWithGuard();
        synchronized void rely()throws InterruptedException{
            part.await();
        }
        synchronized void set(boolean c){
            part.signal(c);
        }
    }
    //上述方法中会出现的问题是：假设一个线程T调用了host.rely方法导致了其在part中被阻塞，而当T被阻塞时
    //他所拥有的host的锁还是保持的，因此没有其他的线程能够通过执行host.set来解除这个锁
    //当普通的同步方法调用其他一样普通但是却使用了wait的同步方法时，这种嵌套问题就可能会导致意想不到的的死锁

    //有两个方法可以避免嵌套的监视器所带来的死锁问题，第一：在那些调用part对象方法的的host对象方法中不使用同步，
    //当host对象的调用是无状态的时候就可以使用这种方法
        //2：如果part对象的方法必须访问被锁定的host对象的状态，则可以重新定义part类，让其使用一种扩展形式的分层的容器锁
        //这种方法将把host对象作为监视器，如下
    class OwnedPartWithGuard{
        protected boolean cond=false;
        final Object lock;
        OwnedPartWithGuard(Object x){lock=x;}
        void await() throws InterruptedException{
            synchronized (lock){
                while (!cond)lock.wait();
                //...
            }
        }
        void signal(boolean c){
            synchronized (lock){
                cond=c;
                lock.notifyAll();
            }
        }

    }

}
