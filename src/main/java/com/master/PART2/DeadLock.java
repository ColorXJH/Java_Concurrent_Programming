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
    //swapValue是一个多方同步操作，也就是操作本身需要多个对象的锁，如果一不小心就会出现一个线程
    //调用a.swapValue(b),另一个线程调用b.swapValue(a),当出现竞态条件时都没有释放彼此的锁时，就会出现死锁现象
    synchronized void swapValue(DeadLock other ){
        long t=getValue();
        long v=other.getValue();
        setValue(v);
        other.setValue(t);
    }
}
