package com.master._05javaLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 通过有界队列示例来掩饰Condition的使用方法
 * @date 2023-11-20 16:01
 */
public class BoundedQueue<T> {
    private Object[] items;
    //添加的下标，删除的下标和数组当前的数量
    private int addIndex, removeIndex, count;
    private Lock lock = new ReentrantLock();
    private Condition notEmpty = lock.newCondition();
    private Condition notFull = lock.newCondition();

    public BoundedQueue(int size) {
        items = new Object[size];
    }

    //添加一个元素，如果数组满，则添加线程进入等待状态，直到有“空位”
    public void add(T t) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();
            items[addIndex] = t;
            if (++addIndex == items.length)
                addIndex = 0;
            ++count;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    //由头部删除一个元素，如果数组空，则删除线程进入等待状态，直到有新元素添加
    public T remove() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            Object x = items[removeIndex];
            if (++removeIndex == items.length)
                removeIndex = 0;
            --count;
            notFull.signal();
            return (T) x;
        } finally {
            lock.unlock();
        }
    }

}

/**
 * BoundedQueue通过add(T t)方法添加一个元素，通过remove()方法移出一个元素,以添加方法为例:
 * 首先需要获得锁，目的是确保数组修改的可见性和排他性。当数组数量等于数组长度时，
 * 表示数组已满，则调用notFull.await()，当前线程随之释放锁并进入等待状态。如果数组数量不
 * 等于数组长度，表示数组未满，则添加元素到数组中，同时通知等待在notEmpty上的线程，数
 * 组中已经有新元素可以获取。
 *
 * 在添加和删除方法中使用while循环而非if判断，目的是防止过早或意外的通知，只有条件
 * 符合才能够退出循环。回想之前提到的等待/通知的经典范式，二者是非常类似的
 */