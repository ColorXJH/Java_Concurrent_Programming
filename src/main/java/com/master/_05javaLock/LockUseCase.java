package com.master._05javaLock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName: LockUseCase
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: Lock锁接口的使用
 * @Datetime: 2023/11/13 21:24
 * @author: ColorXJH
 */
public class LockUseCase {
    private Lock lock=new ReentrantLock();
    public void doSomethingThreadSafe(){
        //不要将获取锁的过程写在try中,如果获取锁时发生了异常，异常抛出的同时，也会导致锁无法释放
        lock.lock();
        try {

        }finally {
            //finally中释放锁，保证锁获取到之后能够释放
            lock.unlock();
        }
    }
}

/**
 * 在Java中，如果在try块中抛出了异常，然后被catch块捕获，finally块中的返回值和catch块中的返回值都会被忽略，
 * 实际上会使用try块中的返回值（如果有）。
 * 如果try块中没有返回值，或者try块中的代码抛出了异常而没有返回值，那么finally块中的返回值和catch块中的返回值都会被忽略。
 */

/**
 * 对于synchronized关键字来说，如果一个线程在等待获取锁时被中断，它会继续等待获取锁，而不会立即响应中断。
 * 换句话说，一旦线程进入synchronized代码块或方法，它会一直等待获取锁，直到获取到锁或者等待超时。
 * 在这种情况下，线程不会立即响应中断，而是会继续等待获取锁，直到获取到锁或者等待超时。
 * 这是与Lock接口的实现类不同的地方，因为Lock接口的实现类可以响应中断并及时释放锁资源。
 */