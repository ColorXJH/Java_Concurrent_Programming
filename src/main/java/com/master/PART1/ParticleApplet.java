package com.master.PART1;

import java.applet.Applet;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-01-23 14:06
 */
public class ParticleApplet extends Applet {
    Thread[] threads=null;
    final ParticleCanvas canvas=new ParticleCanvas(100);
    public void init(){
        add(canvas);
    }

    Thread makeThread(final Particle p){
        //内部类之所以方便和使用的一个原因是，他们可以直接获取所有适当的上下文变量，在这里是p和canvas
        //而不需要创建一个额外的类来保存他们
        //同时他也有一个副作用：这些可以被内部类直接获取的方法参数和本地变量都必须被声明为final,这样做的目的是
        //为了保证这些变量的值都可以被无歧义的获得，否则如果在makeThread方法内部生成了Runnable对象之后,p被重新能赋值，
        //那么当Runnable执行的时候便会无法确定应该使用p原先的值还是新赋予的值
        Runnable runLoop=new Runnable() {
            @Override
            public void run() {
                //定义一个无限循环，只有当前线程中断时这个循环才会退出
                try{
                    for(;;){
                      //粒子移动
                      p.move();
                      //告知画布重新绘制
                      canvas.repaint();
                      //降低工作效率，配合视觉感知速率
                      Thread.sleep(100);
                    }
                }catch (InterruptedException e){
                    return ;
                }
            }
        };
        return new Thread(runLoop);
    }

    public synchronized void start(){
        int n=10;
        if(threads==null){
            Particle[] particles=new Particle[n];
            for(int i=0;i<n;i++){
                particles[i]=new Particle(50,50);
            }
            canvas.setParticles(particles);
            threads=new Thread[n];
            for(int i=0;i<n;i++){
                //Thread类本身也实现了Runnable接口，通常的策略是把Runnable接口当作一个单独的类来实现，并把它作为参数传递给
                //Thread的构造函数，这样就不用担心Runnable类中使用的同步方法和同步块与在相应线程类中所使用的其他任何方法之间的潜在操作所带来的影响
                //这种分离允许独立控制相关的操作和运行这些操作的上下文，同一个Runnable对象既可以传递给多个使用不同方式初始化的Thread对象，
                //也可以传递给其他的轻量级执行者（executor）,继承了Thread类的对象不能再同时继承其他类了
                threads[i]=makeThread(particles[i]);
                threads[i].start();
            }
        }
    }

    public synchronized void stop(){
        if(threads!=null){
            for(int i=0;i<threads.length;i++){
                threads[i].interrupt();
            }
            threads=null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ParticleApplet applet=new ParticleApplet();
        applet.start();
        Thread.sleep(3000);
        applet.stop();
    }
}

//Thread对象还拥有一个后台线程的状态属性，这个属性只能通过Thread实例被启动之前设置，如果setDemo方法被设置为真，则表明：
//当所有的非后台线程都终止后，jvm就会退出，并且会立刻终止这个线程（系统后台进程是一个持续的进程，会“一直”存在于系统之中）
//任何一个调用线程所拥有的同步锁都不会被这个新线程所持有，它指的是一个新线程不会自动继承调用线程的同步锁

//线程的优先级：为了能够在不同的平台上实现jvm,java语言并不会保证线程会被平等的调度或是对待，甚至不严格保证线程的执行
//默认情况下，每一个新线程的优先级小于创建他哪个线程的优先级
//任何线程的优先级都可以通过调用setPriority方法动态的改变，一个线程所允许的最大优先级由它所处的ThreadGroup决定

//下面给出一些线程优先级不同类型任务的约定
//10        关键问题
//7-9       交互，事件驱动
//4-6       io相关
//2-3       后台计算
//1         在没有其他成勋运行的情况下运行


//线程的中断状态
    //每一个线程都有一个对应的中断状态，用布尔类型变量表示，可以通过设置Thread实例方法:t.interrupt()将这个状态设备之为true
        // (如果该t正在处理Object.wait(),Thread.sleep(),或者是Thread.join()方法，这种情况下将抛出异常并将状态标志设置为false)
    //线程的静态方法Thread.interrupted()用来测试当前线程状态，同时将中断标志设置重置为false
    //线程实例的t.isInterrupted()也是用来测试当前线程状态的，但是他不清除线程中断标志
    //当线程摘执行wait().sleep(),join()方法时，如果线程被中断，这些方法将会抛出InterruptedException,同时，抛出异常并不改变线程的中断状态
        //中断标志仍然为false
    //yield,一个建议，告知JVM,当系统中有其他未处在运行状态的活动线程，调度程序可以从这些线程中选择一个运行而放弃当前线程
        //在单cpu的jvm实现上起到相应作用，只要这些实现不使用分时抢占式的调度机制，
    //线程组ThreadGroup:
        //层次结构  线程组管理   异常处理    安全性     线程组嵌套