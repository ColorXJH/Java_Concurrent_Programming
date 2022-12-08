package com.master._03javaMemoryModel;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 可重入锁的内存语义实现
 * @date 2022/12/7 14:45
 */
public class ReentrantLockExample {
    int a=0;
    ReentrantLock lock=new ReentrantLock();
    public void writer(){
        lock.lock();//获取锁
        try {
            a++;
        }finally {
            lock.unlock();//释放锁
        }
    }
    public void reader(){
        lock.lock();
        try {
            int i=a;
        }finally {
            lock.unlock();
        }
    }
}
