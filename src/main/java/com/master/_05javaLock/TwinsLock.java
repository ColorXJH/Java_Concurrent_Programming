package com.master._05javaLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @ClassName: TwinsLock
 * @Package: com.master._05javaLock
 * @Description: 允许两个线程同时访问的同步组件
 * @Datetime: 2023/11/18 21:36
 * @author: ColorXJH
 */
public class TwinsLock implements Lock {
    private final Sync sync=new Sync(2);
    /**
     * 同步器作为一个桥梁，连接线程访问以及同步状态控制等底层技术与不同并发组件（比如
     * Lock、CountDownLatch等）的接口语义。
     */
    private static final class Sync extends AbstractQueuedSynchronizer{
        Sync(int count){
            if(count<=0){
                throw new IllegalArgumentException("COUNT  MUST MORE THAN ZERO!");
            }
            setState(count);
        }

        @Override
        protected int tryAcquireShared(int arg) {
            for(;;){
                int current=getState();
                int newCount=current-arg;
                //返回当前的剩余同步状态，如果预判断小于0（即目前已经有两个同步状态了，就不用cas操作了，直接返回）
                if(newCount<0||compareAndSetState(current,newCount)){
                    return newCount;
                }
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            for(;;){
                int current=getState();
                int newCount=current+arg;
                if(compareAndSetState(current,newCount)){
                    return true;
                }
            }
        }

        final ConditionObject newCondition(){
            return new ConditionObject();
        }
    }
    @Override
    public void lock() {
        sync.acquireShared(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquireShared(1)>=0;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1,unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.releaseShared(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}


/**
 * 在上述示例中，TwinsLock实现了Lock接口，提供了面向使用者的接口，使用者调用lock()
 * 方法获取锁，随后调用unlock()方法释放锁，而同一时刻只能有两个线程同时获取到锁。
 * TwinsLock同时包含了一个自定义同步器Sync，而该同步器面向线程访问和同步状态控制。以
 * 共享式获取同步状态为例：同步器会先计算出获取后的同步状态，然后通过CAS确保状态的正
 * 确设置，当tryAcquireShared(int reduceCount)方法返回值大于等于0时，当前线程才获取同步状
 * 态，对于上层的TwinsLock而言，则表示当前线程获得了锁。
 *
 */