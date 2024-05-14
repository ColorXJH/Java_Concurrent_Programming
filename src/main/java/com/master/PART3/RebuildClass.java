package com.master.PART3;

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
}
