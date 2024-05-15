package com.master.PART3;

import java.util.Hashtable;

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
    //状态变量
    class BoundedCounterWithStateVariable{
        static final int BOTTOM=0,MIN=0,MAX=100,MIDDLE=1,TOP=2;
        protected int state=BOTTOM;
        protected long count=MIN;
        protected void updateState(){
            int oldState=state;//记录更新前的状态
            if(count==MIN)state=BOTTOM;
            else if(count==MAX)state=TOP;
            else state=MIDDLE;
            if(state!=oldState&&oldState!=MIDDLE){
                notifyAll();
            }
        }

        public synchronized long count(){
            return count;
        }

        public synchronized void inc() throws InterruptedException{
            while(state==TOP){
                wait();
            }
            ++count;
            updateState();
        }

        public synchronized void dec() throws InterruptedException{
            while (state==BOTTOM)wait();
            --count;
            updateState();
        }
    }
    //冲突集合
        //不能同时出现方法对的集合，比如：{(store,retrieve),(retrieve,retrieve)}
        //基于冲突集合的类可以使用before/after这种设计，即基本操作被那些维护着独占关系的代码所环绕，下面的机制可以通过任何的before/after模式实现
            //1：对于每个方法，定义一个计数变量，用以标识该方法是否在执行中
            //2：把每个基本操作都隔离入非公共的方法中
            //3：编写那些基本操作的公共版本，即在那些基本操作的前后添加上before/after的控制
                //1：每个同步的before操作都必须先等待所有非冲突的方法结束，这可从计数变量得知，随后before操作增加与该方法相关的计数变量的值
                //2：每个同步的after操作减少该方法的计数变量的值，并发出通知以唤醒等待中的线程
    class Inventory{
        protected final Hashtable items=new Hashtable();
        protected final Hashtable suppliers=new Hashtable();
        protected int storing =0;
        protected int retrieving=0;
        protected void doStore(String description,Object item,String supplier){
            items.put(description,item);
            suppliers.put(supplier,description);
        }

        protected Object doRetrieve(String description){
            Object x=items.get(description);
            if(x!=null){
                items.remove(description);
            }
            return x;
        }
        //存取不能同时  存存可以同时
        public void store(String description,Object item,String supplier)throws InterruptedException{
            synchronized (this){//before-action
                while (retrieving!=0)wait();
                ++storing; //记录执行状态
            }
            try{
                doStore(description,item,supplier);
            }finally {//after-action
                synchronized (this){
                    if(--storing==0){
                        notifyAll();
                    }
                }
            }
        }
        //取存不能同时 取取不能同时
        //retrieve操作不应该何store操作并发执行，因为store方法正在存入的对象可能就是正在被请求出的对象，而你并不想为此返回错误数据
        //两个或两个以上的retrieve操作不应该同时执行，因为其中一个操作正在取出的对象可能正式另一个操作所请求的
        public Object retrieve(String description) throws InterruptedException{
            synchronized (this){//before-action
                while (storing!=0||retrieving!=0)wait();
                ++retrieving;
            }
            try{
               return doRetrieve(description);
            }finally {//after-action
                synchronized (this){
                    if(--retrieving==0){
                        notifyAll();
                    }
                }
            }
        }
    }
    //变种及扩展
        //也可以用在乐观方法里，在其中冲突被称为无效关系，他们通常这样被实现：在事务提交前中断冲突的操作，而不是一直等到安全时才继续执行这些操作
        //当retrieve方法正在执行中时，store方法可以开始执行，但是反过来却不行，及某个方法a仅在c执行后才会同b冲突

    //构建子类
        //读出者与写入者：是这样一组并发构建：他么有着相同的基础，但是一个负责控制那些执行读出操作的线程，另一个负责控制那些执行写入操作的线程
        //他们并发控制的策略不一样

}
