package com.master._05javaLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: Condition接口的等待通知模式，类似Object的监视器方法
 * @date 2023-11-20 15:29
 */
public class ConditionUseCase {
    Lock lock=new ReentrantLock();
    Condition condition=lock.newCondition();

    public void conditionWait() throws InterruptedException {
        lock.lock();
        try {
            //调用await()方法后，当前线程会释放锁并在此等待,而其他线程调用Condition对象的signal()方法，
            // 通知当前线程后，当前线程才从await()方法返回，并且在返回前已经获取了锁
            condition.await();
        } finally {
            lock.unlock();
        }
    }


    public void conditionSignal(){
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }

    }

}
