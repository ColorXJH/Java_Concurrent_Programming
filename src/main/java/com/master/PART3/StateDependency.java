package com.master.PART3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.EventObject;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 状态依赖
 * @date 2024-04-08 17:10
 */
public class StateDependency {
}
//执行任何操作通常有两个必要条件
    //1：外部条件：对象收到了执行操作的请求
    //2：内部条件：对象处于可进行操作的状态
//独占技术主要关注于维持不变约束，而状态依赖的并发控制 更加关注前提条件和结果条件
    //具体来说：当客户调用主体对象的方法时，这个操作不一定有基于状体的前提条件，
    //或者说其以来的对象导致其不能满足自身的结束条件
//大多数有状态依赖操作的类的设计问题都要考虑如下因素：如何顾及到所有的可能出现的消息以及状态的组合，从而使得设计具有完整性
//在一个理想的系统中，所有方法都不应有基于状态的前提条件，并且总是能够满足结束条件
    //但是很多行为的本质就是依赖状态的，并且无法被编写成在所有状态下都满足结束条件
//基于活跃性优先和安全性优先的角度，
    //1：乐观的"先试再看"--》调用者需要处理调用失败的情况
        //乐观法依赖于异常机制以及当结果条件无法满足时的相关提示机制的存在
    //2：保守的”先测再做“
        //保守法依赖于那些提供保障的构建，这些构建能够在操作所需的前提条件满足时发出提示
class ClientUsingSocket{
    Socket retryUnitConnected()throws InterruptedException{
        long delayTime=5000+(long)(Math.random()*5000);
        for(;;){
            try{
                return new Socket();
            }catch (Exception e){
                Thread.sleep(delayTime);
                delayTime=delayTime*3/2+1;//increase 50%
            }
        }
    }
}

//异常处理器
    //可以使用一个before/after类来构建一个异常处理器
class ServiceException extends Exception {

}
interface ServerWithException{
    void service()throws ServiceException;
}
class ServiceImpl implements ServerWithException{

    @Override
    public void service() throws ServiceException {

    }
}

interface ServiceExceptionHandler{
    void handle(ServiceException e);
}
class HandlerImpl implements ServiceExceptionHandler{

    @Override
    public void handle(ServiceException e) {
        System.out.println("HandlerImpl.handle");
    }
}

class HandledService implements ServerWithException{
    final ServerWithException server=new ServiceImpl();
    final ServiceExceptionHandler handler=new HandlerImpl();
    @Override
    public void service() throws ServiceException {
        try {
            server.service();
        } catch (ServiceException e) {
            handler.handle(e);
        }
    }
}

//异常回调
interface ServiceUsingCallback{
    void anotherService(ServiceExceptionHandler handler);
}

class ExceptionEvent extends EventObject{
    public final Throwable throwable;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ExceptionEvent(Object source,Throwable tx) {
        super(source);
        throwable=tx;
    }

}

class ExceptionEventListener{
    public void exceptionOccured(ExceptionEvent ee){

    };
}

class ServiceIssuingExceptionEvent{
    private final CopyOnWriteArrayList handlers=new CopyOnWriteArrayList();
    public void addHandler(ExceptionEventListener listener){
        handlers.add(listener);
    }
    public void service(){
        boolean fail=true;
        if(fail){
            Throwable ex=new ServiceException();
            ExceptionEvent ee=new ExceptionEvent(this,ex);
            for(Iterator it=handlers.iterator();it.hasNext();){
                ExceptionEventListener listener= (ExceptionEventListener) it.next();
                listener.exceptionOccured(ee);
            }
        }
    }
}

//取消
    //中断：实现取消最常见的方法都依赖于每个线程的中断，中断状态可以通过调用Thread,interrupt来设置，
        //调用Thread.isInterrupted来查询，调用Thread.interrupted来查询并消除，以及有时通过InterruptedException异常来响应
//Thread.interrupted();//查询中断状态并清除标记
//Thread.currentThread().isInterrupted();//查询中断状态
//Thread.currentThread().interrupt();//设置中断状态
    //在Object.wait(),Thread.join,Thread.sleep,以及他们的派生方法中，中断检测是自动执行的，这些方法在遇到中断时，会抛出InterruptedException
    //异常以中断当前的操作，使得线程可以被唤醒并执行取消操作。
    //抛出InterruptedException 异常会重置线程的中断状态，如果在catch块中不加以设置，中断状态会被清除。
        //如果需要保持传递该中断状态，则一般使用Thread.currentThread().interrupt();来重新设置中断状态
        //如果你所创建的线程中的代码需要调用其他并不能正常维护中断状态的代码，你可以这么做：使用一个成员变量来记录取消状态
            //无论何时，在调用interrupt方法时都去设置它，且无论何时从那些无法维护取消状态的调用中返回前都检测这个变量
//有两种情况会导致线程处于睡眠状态而无法去检查中断的状态或者捕获InterruptedException异常，
    //1：被同步锁：当线程在等待获得某个同步方法/代码段中的锁时，是无法响应中断请求的，解决办法：可以换成使用锁工具类，在使用这些类时，代码只会在访问锁对象
        //自身时才会造成阻塞，而不会在锁对象用来保护其他代码的同步访问时造成阻塞，且自身的阻塞是非常短的
    //2：IO操作所阻塞：

//理解以下内容：传统的io阻塞线程，但是释放了cpu资源，使得cup可以执行其他线程，当然新io不会阻塞线程，io完成后会通知该线程
//在传统的阻塞IO模型中，当一个线程执行一个IO操作时，确实会等待直到IO操作完成才能继续执行。
//这时，线程会被阻塞，无法执行其他任务，因为它需要等待IO操作完成。
//这种情况下，线程会释放CPU资源，以便其他线程可以使用CPU执行任务。
//然而，关键在于理解IO操作对线程的影响。
//在阻塞IO模型中，虽然线程被阻塞，但线程实际上是在等待IO操作完成期间释放了CPU资源，而不是CPU被IO操作阻塞。
//这意味着在传统的阻塞IO模型中，CPU资源仍然可以用于执行其他线程的任务，只是正在执行IO操作的线程没有使用CPU。
//因此，尽管IO操作会导致线程被阻塞，但并不会阻塞CPU的并发执行，其他线程仍然可以利用CPU执行任务。

//在某些情况下，线程可能因为正在进行阻塞的 I/O 操作而无法主动去检查中断的状态或捕获 InterruptedException 异常。
//具体来说，当线程在执行阻塞 I/O 操作时，它会等待直到 I/O 操作完成才能继续执行其他任务。
//在这个等待期间，线程无法主动地去检查中断状态，因为它处于睡眠状态（或者说阻塞状态），只有等待 I/O 操作完成后才会唤醒。
//然而，如果在线程执行阻塞的 I/O 操作期间调用了 Thread.interrupt() 方法，线程会抛出 InterruptedException 异常并退出阻塞状态。
//这种情况下，线程可以响应中断，但并不是通过主动检查中断状态来实现的，而是通过抛出异常来响应中断。
class CancelableReader{
    private Thread readerThread;
    private FileInputStream dataFile;
    public synchronized void startReaderThread()throws IllegalStateException, FileNotFoundException {
        if(readerThread!=null){throw new IllegalStateException();}
        dataFile=new FileInputStream("data");
        readerThread=new Thread(new Runnable() {
            @Override
            public void run() {
                doRead();
            }
        });
        readerThread.start();
    }

    protected synchronized void closeFile(){
        if(dataFile!=null){
            try{
                dataFile.close();
            }catch (IOException exception){
                dataFile=null;
            }
        }
    }

    protected  void doRead(){
        try{
            while (!Thread.interrupted()){
                try{
                    int c=dataFile.read();
                    if(c==-1){
                        break;
                    }else{
                        //do something;
                    }
                }catch (IOException e){
                    break;
                }
            }
        }finally {
            closeFile();
            synchronized (this){
                readerThread=null;
            }
        }
    }

    public synchronized void cancelReaderThread(){
        if(readerThread!=null){
            readerThread.interrupt();
        }
        closeFile();
    }

    void attemptRead(InputStream stream,long timeout){
        long startTime=System.currentTimeMillis();
        try{
            for(;;){
                if(stream.available()>0){
                    int c=stream.read();
                    if(c!=-1){
                        //处理数据
                    }else{
                        break;
                    }
                }else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                    long now=System.currentTimeMillis();
                    if(now-startTime>=1000){
                        //判定失败
                    }
                }
            }
        }catch (IOException exception){
            //失败
        }
    }
}
//大多数其他取消IO操作的例子都源于上述方式，中断那些等待io输入的线程，而那些被等待输入的内容

//stop异步终止
    //这是一个过时的方法：Thread.stop会导致线程突然抛出ThreadDeath异常（同interrupt方法一样，stop并不会中断
      //那些等待获得锁或者IO操作的线程，她也不能够保证能中断那些调用了wait,sleep,join方法的线程，和interrupt的区别）
class C{
    private int v;
    synchronized void f(){
        v=-1;//非法值
        //如果在计算程序之前外部调用了Thread.stop方法，那么这个对象就将崩溃，当线程终止时，由于被赋值了一个非法值-1，使得该对象处于一个错误状态
        //其他线程调用这个对象时，都会导致其执行的时非法值,比如下面的g方法，其他线程调用时都会执行Integer.MAX_VALUE*2次再重新回到正值
        //计算操作 compute()
        v=1;//正确值
    }
    synchronized void g(){
        while (v!=0){
            --v;
            //do something;
        }
    }
}

//ThreadDeath 异常是一个继承自 Error 类的特殊异常，在 Java 中，它通常由 stop() 方法在一个线程上调用而抛出。
//当 stop() 方法被调用时，会向线程抛出 ThreadDeath 异常，导致线程突然中止。
//值得注意的是，ThreadDeath 异常不是 Throwable 异常的子类，而是 Error 类的子类。
//由于 ThreadDeath 异常是 Error 类的子类，它不会被检查，
//也就是说，不需要在方法签名中声明抛出它，也不需要捕获它。
//因此，当 ThreadDeath 异常被抛出时，它不会受到异常处理机制的控制，可以穿透 catch 块、finally 块等。
//对于 interrupt() 方法，当一个线程被中断时，它会收到一个 InterruptedException 异常。
//但如果该线程的 run() 方法因为 ThreadDeath 异常而结束，即使 run() 方法中包含了 try-catch 块捕获了 InterruptedException，
//线程也不会因为中断而进入到 catch 块中执行相应的处理。因此，ThreadDeath 异常会影响到 interrupt() 方法的正常处理流程。

//多阶段取消
    //有时候即使是最普通的代码也要采取远比预期还要极端的方式来取消，为了应对这种可能性，需要构建一个通用的多阶段取消工具
        //linux的关机程序就采用了这个模式，先试图使用kill -1 ,如果不行就kill -2 直到kill -9
class Terminator{
    static boolean terminate(Thread t,long maxWaitToDie){
        if(!t.isAlive()){return true;}//already dead
        //低级别打断
        t.interrupt();
        try {
            t.join(maxWaitToDie);
        } catch (InterruptedException e) {
            //ignore
        }
        if(!t.isAlive()){return true;}//success
        //高级别打断  基本上会返回true
        //最终结果，如果还没打断
        t.setPriority(Thread.MIN_PRIORITY);
        return false;
    }
}

//受保护方法
    //保守的先测在做，当前提条件无法到达时，有以下三种处理情况
        //1：阻碍 2：保护性挂起 3：超时
    //无论什么时候，当一个方法再获取资源过程中应用“要么现在拥有且执行，要么就退出且不再执行”的策略时，阻碍方法也很有效
    //保护性挂起案例：

interface BoundedCounter{//任何实现了该接口的类都要保证计数器count的值在min和max之间
    static final long MIN=0;
    static final long MAX=10;
    long count();//inv:MIN<count()<MAX
    void inc();//only allowed when count()<MAX
    void dec();//only allowed when count()>MIN

}
//保障：从某种意义上来说受保护方法是synchronized类型的方法的一种的可定制扩展，他提供了独占的一种扩展形态
//保障也可以被看着是一种特殊形式的条件，在穿行执行的程序中，一个if语句就可以检测执行方法所以需要的条件是否为真，当条件没有满足时
//就没有必要等待条件为真，因为没有其他的并发操作能使其条件为真，但是在并发程序中，异步的状态变化会在任何时候发生
//因此受保护方法带来了那些在简单条件下不会出现的活跃性问题，任何保障都隐含着一个断言：最终某些线程会使需要的状态改变出现
//或者如果这些状态改变不出现，最好的选择就是不去执行当前的操作

//有条件的等待
class BoundedCounterWithWhen{
    protected long count=0;
    public long count(){
        return count;
    }

    public void inc(){
        while(count<10){
            ++count;
        }
    }

    public void dec(){
        while (count>0){
            --count;
        }
    }
}

//基于状态的消息接收
//监控机制
    //实现受保护的方法基本上都是使用了Object.wait/notify/notifyAll策略的特殊形式
        //对于每一个需要等待的条件，写一段受保护的wait循环，如果当前受保护的条件不为真，则使当前线程阻塞
        //确保任何会改变被等待条件的方法都会通知那些正在等待z这些条件的线程，使得他们在被唤醒以后重新检查当前的受保护条件

class X{
    synchronized void w() throws InterruptedException{
        before();
        wait();
        after();
    }
    synchronized void n(){
        notifyAll();
    }
    void before(){}
    void after(){}
}
//受保护的等待
/*
 * synchronized void inc()throws InterruptedException{
 *      while(count>=MAX){
 *          wait();
 *      }
 *      ++count;
 * }
 */

//中断的等待
