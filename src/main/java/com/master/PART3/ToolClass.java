package com.master.PART3;


/**
 * @author ColorXJH
 * @version 1.0
 * @description: 工具类的实现
 * @date 2024-05-20 17:23
 */
public class ToolClass {
    //如何在普通的接口上添加获得-释放（acquire-release）协议，随后通过例子演示了如何使用协同操作的技巧来将类分割
    //以获得必要的并发控制，并在随后将其合并以提交性能，最后讨论如何将等待的线程隔离以管理通知

    //1：获得-释放 协议
    interface Sync{
        void acquire()throws InterruptedException;
        void release();
        boolean attempt(long times)throws InterruptedException;
    }

    class Semaphore implements Sync{
        protected long permits;//current available permits
        public Semaphore(long permits){
            this.permits=permits;
        }

        @Override
        public void acquire() throws InterruptedException {
            if(Thread.interrupted()){
                throw new InterruptedException();
            }
            synchronized (this){
                try {
                    while (permits<=0)wait();
                    permits--;
                }catch (InterruptedException ie){
                    notify();
                    throw ie;
                }
            }
        }

        @Override
        public synchronized void release() {
            permits++;
            notify();
        }

        @Override
        public boolean attempt(long times) throws InterruptedException {
            if(Thread.interrupted())throw new InterruptedException();
            synchronized (this){
                if(permits>0){
                    permits--;
                    return true;
                }
                else if(times<=0){
                    return false;
                }else{
                    try {
                        long startTime=System.currentTimeMillis();
                        long waitTime=times;
                        for(;;){
                            wait(waitTime);
                            if(permits>0){
                                permits--;
                                return true;
                            }
                            else{
                                long now=System.currentTimeMillis();
                                //是否是等待后立刻唤醒并重新获得锁，如果是0或者负数说明等待至少整个时间段，如果大于0说明提前抢到锁，
                                //但是没有许可证，接着等待剩余的时间，如果一直没到就返回false
                                waitTime=times-(now-startTime);
                                if(waitTime<=0) return false;
                            }
                        }
                    }catch (InterruptedException ie){
                        notify();
                        throw ie;
                    }
                }
            }
        }
    }
    //委托操作
    //将不同的等待条件委托给不同的辅助对象，适用于纯等待与通知机制
        //1：由于辅助类必须访问公共的状态，所以无法完全把每个辅助类按自包含方式隔离起来，在辅助类之间，对公共状态的不同访问必须被正确的同步
        //2：每个可能影响其他辅助类的受保护条件的辅助类，必须提供对这种影响的有效通知，以避免活跃性问题
        //3：和wait机制有关的辅助方法的同步，必须注意避免出现嵌套的监视器问题，

    /**
     * void doM()throws InterruptedException{
     *     for(;;){
     *         synchronized(this){
     *             synchronized(rep){
     *                 boolean canAct=inRightState(rep);
     *                 if(canAct){
     *                     update(rep);
     *                     break;
     *                 }
     *             }
     *             wait();
     *         }
     *     }
     *     host.signalChange();
     * }
     *
     */

    public static void main(String[] args) {
        System.out.println((System.currentTimeMillis()-1716438485000l)/1000/60);
    }
    //有界缓冲区
        //将辅助对象分为分别执行put和take的，对执行put操作的辅助类，计数器的值从数组的容量开始，对于执行take操作的辅助类
        //计数器的值是从0开始的，exchange只有在存储槽的数量大于0的时候才可以执行

    final class BoundedBufferWithDelegates{//委托
        private Object[] array;
        private Exchanger putter;
        private Exchanger taker;
        public BoundedBufferWithDelegates(int capacity)throws IllegalArgumentException{
            if(capacity<=0){throw new IllegalArgumentException();}
            array=new Object[capacity];
            putter=new Exchanger(capacity);
            taker=new Exchanger(0);
        }
        public void put(Object x)throws InterruptedException{
            putter.exchange(x);
        }
        public Object take()throws InterruptedException{
            return taker.exchange(null);
        }
        void removeSlotNotification(Exchanger h){
            if(h==putter)taker.addSlotsNotification();
            else putter.addSlotsNotification();
        }
        protected class Exchanger{
            protected int ptr=0;
            protected int slots;
            protected int waiting=0;
            Exchanger(int n){
                slots=n;
            }
            synchronized void addSlotsNotification(){
                ++slots;
                if(waiting>0){
                    notify();//unlock a single waiting thread
                }
            }
            Object exchange(Object x)throws InterruptedException{
                Object old=null;
                synchronized (this){
                    while (slots<=0){
                        ++waiting;
                        try{
                            wait();
                        }catch (InterruptedException ie){
                            notify();
                            throw ie;
                        }finally {
                            --waiting;
                        }
                    }
                    --slots;
                    old=array[ptr];
                    array[ptr]=x;
                    ptr=(ptr+1)% array.length;
                }
                removeSlotNotification(this);
                return old;
            }
        }

    }

    //合并类
    final class BoundedBufferWithMonitorObjects{
        private final Object[] array;//the elements
        private int putPtr=0;//circular indices
        private int takePtr=0;

        private int emptySlots;//slots count
        private int usedSlots=0;

        private int waitingPuts=0;//counts of waiting thread
        private int waitingTakes=0;

        private final Object putMonitor=new Object();
        private final Object takeMonitor=new Object();
        public BoundedBufferWithMonitorObjects(int capacity)throws IllegalArgumentException{
            if(capacity<=0)throw new IllegalArgumentException();
            array=new Object[capacity];
            emptySlots=capacity;
        }

        public void put(Object x)throws InterruptedException{
            synchronized (putMonitor){
                while (emptySlots<=0){
                    ++waitingPuts;
                    try {
                        putMonitor.wait();
                    }catch (InterruptedException ie){
                        putMonitor.notify();
                        throw ie;
                    }finally {
                        --waitingPuts;
                    }
                }
                --emptySlots;
                array[putPtr]=x;
                putPtr=(putPtr+1)% array.length;
            }
            synchronized (takeMonitor){//directly notify
                ++usedSlots;
                if(waitingTakes>0)
                    takeMonitor.notify();
            }
        }
        public Object take()throws InterruptedException{
            Object old=null;
            synchronized (takeMonitor){
                while (usedSlots<=0){
                    ++waitingTakes;
                    try{
                        takeMonitor.wait();
                    }catch (InterruptedException ie){
                        takeMonitor.notify();
                        throw ie;
                    }finally {
                        --waitingTakes;
                    }
                }
                --usedSlots;
                old=array[takePtr];
                array[takePtr]=null;
                takePtr=(takePtr+1)% array.length;
            }
            synchronized (putMonitor){
                ++emptySlots;
                if(waitingPuts>0)
                    notify();
            }
            return old;
        }
    }

    //特定通知
    class FIFOSemaphone extends Semaphore{
        protected final WaitQueue queue=new WaitQueue();
        public FIFOSemaphone(long init){
            super(init);
        }

        @Override
        public void acquire() throws InterruptedException {
            if(Thread.interrupted()) throw new InterruptedException();
            WaitNode node=null;
            synchronized (this){
                if(permits>0){//no need to queue
                    --permits;
                    return;
                }else{
                    node=new WaitNode();
                    queue.enq(node);
                }
            }
            //must release lock before node wait
            node.doWait();
        }

        @Override
        public synchronized void release() {
            for(;;){
                WaitNode node=queue.deq();
                if(node==null){
                    ++permits;
                    return;
                }else if(node.doNotify()){
                    return;
                }
                //else node was already released due to interruption or time-out  so must retry
            }
        }

        protected  class WaitNode{
            boolean release=false;
            WaitNode next=null;
            synchronized void doWait()throws InterruptedException{
                try{
                    while (!release)wait();
                }catch (InterruptedException ie){
                    if(!release){//通知前被打断
                        //抑制将来的通知
                        release=true;
                        throw ie;
                    }else{//通知后被打断
                        //忽略异常但是传递异常状态码
                        Thread.currentThread().interrupt();
                    }
                }
            }
            synchronized boolean doNotify(){
                if(release)return false;
                else release=true;
                notify();
                return true;
            }

            synchronized boolean doTimedWait(long times)throws InterruptedException{
                //参见3.1.2
                return false;
            }

        }
        //标准的链表队列，使用在持有锁的时候使用
        protected class WaitQueue{
            protected WaitNode head=null;
            protected WaitNode last=null;
            protected void enq(WaitNode node){
                if(last==null)head=last=node;
                else{
                    //将新节点添加到队列的末尾
                    last.next=node;
                    //更新添加后的队列尾节点的指针
                    last=node;
                }
            }
            protected WaitNode deq(){
                //获取当前头节点
                WaitNode node=head;
                //检查是否为空队列，决定后续是否为空
                if(node!=null){
                    //更新头节点指针到下一个节点
                    head=node.next;
                    //如果更新后的头节点为空标识队列为空
                    if(head==null)last=null;
                    //断开出队节点的next指针，防止潜在的内存泄露
                    node.next=null;
                }
                return node;
            }
        }
    }
}
