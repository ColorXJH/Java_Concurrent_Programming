package com.master.PART2;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-04-07 14:01
 */
public class CopyOnWrite {
}
//如果一组包含对象状态的成员变量间需要维护互相的不变约束，那么程序员可以将这些成员变量单独放在一个对象中，从而继续保证语义上的不变约束
//这样做需要基于一个事实：不变表示对象一直维护着合法对象状态的一致性快照
class ImmutablePoint{
    private final int x;
    private final int y;

    public ImmutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

class Dot{
    protected ImmutablePoint loc;
    public Dot(int x,int y){
        loc=new ImmutablePoint(x,y);
    }

    public synchronized ImmutablePoint getLoc() {
        return loc;
    }
    protected synchronized void updateLoc(ImmutablePoint newLoc){
        loc=newLoc;
    }
    public void moveTo(int x,int y){
        updateLoc(new ImmutablePoint(x,y));
    }

    public synchronized void shiftX(int delta){
        updateLoc(new ImmutablePoint(loc.getX()+delta, loc.getY()));
    }
}
//如果状态表现形式严格限制在一个对象内部，那么就不必为加强不变访问而创建新的类，希望快速轻易的获取一致的表现形式，同时又不希望有创建对象开销时
//可以使用写拷贝技术，使用写拷贝技术，最多使用一次同步操作，就可以访问到不可变对象表现形式的所有状态，例如：写拷贝集合对象在事件和多点传输框架中用来维护监听器的集合
//常见的是基于数组的写拷贝技术，（也有其他的好的方法的实现：java.awt.EventMulticaster中使用特殊目的、基于树的结构）
    //基于数组的是常见的方法：通过迭代变量来遍历不仅速度快，还可以避免用其他方式j进行遍历时发生的ConcurrentModificationException异常
class CopyOnWriteArrayList{
    protected Object[] array=new Object[0];

    public synchronized Object[] getArray() {
        return array;
    }
    public synchronized void add(Object element){
        int len=array.length;
        Object[] newArray=new Object[len+1];
        System.arraycopy(array,0,newArray,0,len);
        newArray[len]=element;
        array=newArray;
    }
    public Iterator iterator(){
        return new Iterator() {
            protected final Object[] snapshot=getArray();
            protected int cursor=0;
            @Override
            public boolean hasNext() {
                return cursor<snapshot.length;
            }

            @Override
            public Object next() {
                try {
                    return snapshot[cursor++];
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                }
            }
        };
    }
}

//乐观更新 optimistic update
    //采用比其他写拷贝技术更弱的协议，并不是把整个状态更新的方法锁住，而知识在状态更新的开始和结束时使用同步
    //方法通常有下面的形式：
        //1：得到当前状态表示的一个拷贝（在持有锁的时候）
        //2：创建一个新的状态表示（不持有任何锁）
        //3：只有在老状态被获取后且没有被更新的情况下，才能被转换为新的状态
    //在写拷贝技术上增加的主要需求就是在处理上述步骤3时可能出现的失败：因为在当前线程有机会更新状态表示之前，其他线程就已经擅自将其更新了
    //这种潜在的失败导致了限制乐观更新技术使用范围的两点考虑：
        //1：失效协议：失败的时候，或者选择对整个方法序列进行重试，或者把是吧返回给客户端，
            //在任意时常内可能存在多线程竞争时，乐观更新并不是一个好的选择，但是在有限次数更新就成功的情况下，乐观更新比较可靠
        //2：副作用：因为会导致失败，所以在创建新状态表示过程中执行的操作，不能带来任何的不能取消的副作用：例如，不能写入文件，创建线程，或者在GUI中画图
            //除非这些操作在出错的情况下可以被有效的取消

//原子性提交
    //所有乐观技术的核心都是使用原子性提交atomic commit 来代替赋值语句，只有现有的状态表示是调用者锁期望看到的才可以有条件的转换成新的状态表示，
    //有很多方法可以用来区分和跟踪不同的状态表示，例如使用版本号，事务标识符，时间戳、签名代码，最简单的方法是基于状态对象的引用标识符
class Optimistic{
    private State state;

    public synchronized State getState() {
        return state;
    }
    //这里通常也有一些小的改变：compareAndSwap命名的版本返回当前的值，可以是新值或者旧值，取决于操作提交是否成功
    //在系统级的并发编程中，乐观技术日益流行基于这样一个事实：很多现代处理器都包含一个高效的内嵌的compareAndSwap指令或者变体
    private synchronized boolean commit(State assumed,State next){
        if(state==assumed){
            state=next;
            return true;
        }
        return false;
    }
}
class State{

}

//在一个纯乐观类中：很多更新方法都使用标准形式：得到初始状态，创建新的状态表示，然后如果可能就提交，否则就循环或者抛出异常
//但是不依赖任何初始状态的方法可以实现的更为简单，即无条件的变换成新的状态，如下：
class OptimisticDot{
    protected ImmutablePoint loc;
    public OptimisticDot(int x,int y){
        loc=new ImmutablePoint(x,y);
    }

    public synchronized ImmutablePoint getLoc() {
        return loc;
    }
    public synchronized boolean commit(ImmutablePoint assumed,ImmutablePoint next){
        if(loc==assumed){
            loc=next;
            return true;
        }
        return false;
    }

    public synchronized void moveTo(int x,int y){
        loc=new ImmutablePoint(x,y);
    }

    public void shiftX(int delta){
        boolean success=false;
        do{
            ImmutablePoint old=getLoc();
            ImmutablePoint next=new ImmutablePoint(old.getX()+delta, old.getY());
            success=commit(old,next);
        }
        while (!success);
    }
    //如果上方的方法潜在的长时间干扰是一个需要考虑的因素，那么循环就可以变成指数回退策略
}
//开放容器
    //如果使用分层包设计，但是又不能或者不想把所有Part对象都对其他客户隐藏，就可以使用排序的层次式锁技术
    //如果Part对象对客户可见，就必须使用同步，但是如果部分对象频繁调用其他部分对象的方法，那么这种设计就有可能导致死锁，
        //例如：一个线程持有part1的锁，想要对part2进行调用，另一个线程坐着相反的操作
        //要解决这种问题：可以使用严格的对象制设计策略来消除这种影响，part对象依赖host上的锁来实现它的同步控制，
            //如果客户端必须先获得host的锁，那么这种形式的死锁就不会发生
    //2选1的两种策略
        //1：使用内部锁会使得对已经存在的类的更新比较难，并且会增加类对其上下文的依赖
        //2：当任何一个客户忘记使用协议的时候，外部所都会失效

//内部规则
    //在使用内部包容锁的情况下，每一个part对象都对其需要动态独占控制的方法使用其容器的同步锁，在效率最高的情况下：
    //每一个part对象都有一个final成员变量，他在part对象创建的时候初始化，并且在需要使用锁的情况下都是用它，也可以接收
    //part对象方法内其他不相关的锁。
class Part{
    protected final Object lock;
    protected AnotherPart anotherPart;
    public Part(Object owner){
        lock=owner;
    }
    public Part(){//如果没有就使用自己
        lock=this;
    }

    public void anAction(){
        synchronized (lock){
            anotherPart.help();
        }
    }
}

class AnotherPart{
    public void help(){}
}
//作为一种设计策略，可以把大多数或者所有的类都定义成整个更新