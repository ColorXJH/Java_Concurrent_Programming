package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 对于同步方法的分解或者锁的分解
 *
 * @date 2024-04-02 15:14
 */
public class BreakDown {
}


//如果一个类的行为相互独立，互不干扰，或者是不冲突的字部份，那么就值得用细粒度的对象来创建新的类，浙西而辅助对象的行为有主机来代理
class  Shape{
    protected double x=0.0;
    protected double y=0.0;
    protected double width=0.0;
    protected double height=0.0;

    public synchronized double x(){
        return x;
    }
    public synchronized double y(){
        return y;
    }
    public synchronized double getWidth(){
        return width;
    }
    public synchronized double getHeight(){
        return height;
    }

    public synchronized void adjustLocation(){
        x=3;
        y=4;
    }

    public synchronized void adjustDimensions(){
        width=3;
        height=4;
    }
    //通过分解类来降低粒度是最直接的尝试
    class PassThroughShape{
        protected final AdjustableLoc loc=new AdjustableLoc(0,0);
        protected final AdjustableDis dis=new AdjustableDis(0,0);
        public double x(){
            return loc.getX();
        }
        public double y(){
            return loc.getY();
        }
        public double width(){
            return dis.getWidth();
        }
        public double height(){
            return dis.getHeight();
        }


    }

    class AdjustableLoc{
        protected double x;
        protected double y;

        public AdjustableLoc(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public synchronized double getX() {
            return x;
        }

        public synchronized double getY() {
            return y;
        }
        public synchronized void adjust(){
            x=3;
            y=4;
        }


    }
    class AdjustableDis{
        protected double width;
        protected double height;

        public AdjustableDis(double width, double height) {
            this.width = width;
            this.height = height;
        }

        public synchronized double getWidth() {
            return width;
        }

        public synchronized double getHeight() {
            return height;
        }
        public synchronized void adjust(){
            width=3;
            height=4;
        }
    }

    //如果你不能或者不想分解类，可以分解与每个子类功能相关的同步锁，这种操作与上面的把类分解成辅助类，然后再集合所有的辅助类的表示和方法思想类似
    class LockSplitShape{
        protected double x=0.0;
        protected double y=0.0;
        protected double width=0.0;
        protected double height=0.0;

        protected final Object locLock=new Object();
        protected final Object disLock=new Object();

        public double x(){
            synchronized (locLock){
                return x;
            }
        }

        public void adjustLocation(){
            synchronized (locLock){
                x=5;
                y=7;
            }
        }
        public double getHeight(){
            synchronized (disLock){
                return height;
            }
        }
    }

    //有一些类用于管理相互独立的属性和集合特性，每一个这样的集合都可以独立于其他的集合使用，例如Person可以有
    //age,name,成员变量，这些成员变量的改变和Person作为整体所作的任何其他操作无关
    //如果程序员需要同步保护，以避免对数据的并发修改，那么不能只通过简单的把成员变量声明为volatile,程序员可以使用
    //一种简单的分解方法，把同步保护分担给用来保护对简单类型进行简单操作的对象，这样的类扮演着和Integer和Double类似的绝嗣
    //知识这样的类不是用于保证不变性，而是用于保证原子性
        //换句话说：这些类通过将同步保护的责任委托给专门用于保护简餐操作的对象，从而实现了对属性和集合特性的原子性操作，使得他们能够在并发环境下安全的使用
    class SynchronizedInt{
        private int value;
        public synchronized int getValue(){
            return value;
        }
        public synchronized int setValue(int v){//返回前一个value
            int oldValue=value;
            value=v;
            return oldValue;
        }
        public synchronized int increment(){
            return ++value;
        }

    }
    //类是juc包下面的原子类
}