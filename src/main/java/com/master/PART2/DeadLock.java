package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 两个线程持有各自的锁都在等待对方释放锁资源
 * @date 2024-02-26 15:40
 */
public class DeadLock {
    //两个或多个线程都有权访问两个或多个对象，并且每个线程都在已经得到一个锁的情况下等待其他线程已经得到的锁
    private long value;
    synchronized long getValue(){
        return value;
    }
    synchronized void setValue(long v){
        value=v;
    }
    synchronized void swapValue(DeadLock other ){
        long t=getValue();
        long v=other.getValue();
        setValue(v);
        other.setValue(t);
    }
}
