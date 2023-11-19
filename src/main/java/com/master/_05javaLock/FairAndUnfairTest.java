package com.master._05javaLock;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName: FairAndUnfairTest
 * @Package: com.master._05javaLock
 * @Description: 测试类观察公平和非公平锁在获取锁时的区别，在测试用例中定义了内部
 * 类ReentrantLock2，该类主要公开了getQueuedThreads()方法，该方法返回正在等待获取锁的线
 * 程列表，由于列表是逆序输出，为了方便观察结果，将其进行反转
 * @Datetime: 2023/11/19 12:33
 * @author: ColorXJH
 */
public class FairAndUnfairTest {
    private static Lock fairLock=new ReentrantLock2(true);
    private static Lock unfairLock=new ReentrantLock2(false);
    private static CountDownLatch start;

    @Test
    public void fair(){
        testLock(fairLock);
    }
    @Test
    public void unfair(){
        testLock(unfairLock);
    }

    private void testLock(Lock lock){
        start=new CountDownLatch(1);
        for (int i = 0; i < 5; i++) {
            Thread thread=new Job(lock);
            thread.setName("thread "+i);
            thread.start();
        }
        start.countDown();
    }

    private static class Job extends Thread{
        private Lock lock;
        public Job(Lock lock){
            this.lock=lock;
        }

        @Override
        public void run() {
            try {
                start.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < 2; i++) {
                lock.lock();
                try {
                    System.out.println("Lock by ["+getName()+"],waiting by "+((ReentrantLock2)lock).getQueuedThreads());
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private static class ReentrantLock2 extends ReentrantLock{
        private static final long serialVersionUID = -6736727496956351588L;
        public ReentrantLock2(boolean fair){
            super(fair);
        }

        @Override
        protected Collection<Thread> getQueuedThreads() {
            List<Thread> arrayList=new ArrayList<>(super.getQueuedThreads());
            Collections.reverse(arrayList);
            return arrayList;
        }
    }
}


/** 非公平
 * Lock by [thread 3],waiting by []
 * Lock by [thread 3],waiting by [thread 4]
 * Lock by [thread 0],waiting by [thread 4, thread 1]
 * Lock by [thread 0],waiting by [thread 4, thread 1, thread 2]
 * Lock by [thread 4],waiting by [thread 1, thread 2]
 * Lock by [thread 4],waiting by [thread 1, thread 2]
 * Lock by [thread 1],waiting by [thread 2]
 * Lock by [thread 1],waiting by [thread 2]
 * Lock by [thread 2],waiting by []
 * Lock by [thread 2],waiting by []
 *
 */

/** 公平
 * Lock by [thread 3],waiting by []
 * Lock by [thread 2],waiting by [thread 4, thread 3]
 * Lock by [thread 4],waiting by [thread 3, thread 0, thread 1, thread 2]
 * Lock by [thread 3],waiting by [thread 0, thread 1, thread 2, thread 4]
 * Lock by [thread 0],waiting by [thread 1, thread 2, thread 4]
 * Lock by [thread 1],waiting by [thread 2, thread 4, thread 0]
 * Lock by [thread 2],waiting by [thread 4, thread 0, thread 1]
 * Lock by [thread 4],waiting by [thread 0, thread 1]
 * Lock by [thread 0],waiting by [thread 1]
 * Lock by [thread 1],waiting by []
 *
 */

/**
 * 公平性锁每次都是从同步队列中的第一个节点获取到锁，而非公平性锁出现了一个线程连续获取锁的情况
 *
 * 为什么会出现线程连续获取锁的情况呢？回顾nonfairTryAcquire(int acquires)方法，当一
 * 个线程请求锁时，只要获取了同步状态即成功获取锁。在这个前提下，刚释放锁的线程再次获
 * 取同步状态的几率会非常大，使得其他线程只能在同步队列中等待
 *
 * 非公平性锁可能使线程“饥饿”，为什么它又被设定成默认的实现呢？再次观察上表的结
 * 果，如果把每次不同线程获取到锁定义为1次切换，公平性锁在测试中进行了10次切换，而非
 * 公平性锁只有5次切换，这说明非公平性锁的开销更小
 *
 * 公平性锁保证了锁的获取按照FIFO原则，而代价是进行大量的线程切换。非公平性锁虽
 * 然可能造成线程“饥饿”，但极少的线程切换，保证了其更大的吞吐量
 */