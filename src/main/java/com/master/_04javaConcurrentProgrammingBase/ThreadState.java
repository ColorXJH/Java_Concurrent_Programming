package com.master._04javaConcurrentProgrammingBase;

/**
 * @ClassName: ThreadState
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 线程状态，使用jstack工具（打开终端，输入jstack,或者到jdk的bin目录下执行该命令）
 * @Datetime: 2023/11/7 22:59
 * @author: ColorXJH
 */
public class ThreadState {
    public static void main(String[] args) {
        new Thread(new TimeWaiting(),"TimeWaiting Thread").start();
        new Thread(new Waiting(),"Waiting Thread").start();
        //使用两个Blocked线程，一个获取锁成功，另一个被阻塞
        new Thread(new Blocked(),"Blocked Thread-1").start();
        new Thread(new Blocked(),"Blocked Thread-2").start();
    }
    //该线程不断的进行睡眠
    static class TimeWaiting implements Runnable{
        @Override
        public void run() {
            while (true){
                SleepUtils.second(100);
            }
        }
    }
    //该线程在Waiting.class实例上等待
    static class Waiting implements Runnable{
        @Override
        public void run() {
            while (true){
                synchronized (Waiting.class){
                    try {
                        Waiting.class.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    //该线程在Blocked.class实例上加锁后，不会释放该锁
    static class Blocked implements Runnable{
        @Override
        public void run() {
            synchronized (Blocked.class){
                while (true){
                    SleepUtils.second(100);
                }
            }
        }
    }
}


//运行该示例，打开终端或者命令提示符，键入“jps”，
//接着再键入“jstack  id号码
