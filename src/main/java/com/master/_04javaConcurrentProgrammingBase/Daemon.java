package com.master._04javaConcurrentProgrammingBase;

import org.omg.CORBA.TRANSACTION_MODE;

/**
 * @ClassName: Daemon
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: Daemon后台线程
 * @Datetime: 2023/11/7 23:51
 * @author: ColorXJH
 */
public class Daemon {
    public static void main(String[] args) {
        Thread thread=new Thread(new DaemonRunner(),"DaemonRunner");
        thread.setDaemon(true);
        thread.start();
    }

    static class DaemonRunner implements Runnable{
        @Override
        public void run() {
            try {
                SleepUtils.second(100);
            } finally {
                System.out.println("Daemon Thread finally run.");
            }
        }
    }
}
//运行该程序，控制台不会输出任何内容，main线程（非Daemon线程）在启动了线程DaemonRunner之后，随着main方法执行完毕而终止，此时java虚拟集中
//已经没有了非Daemon的线程，虚拟及需要退出，则java虚拟集中的所有的Daemon线程需要立刻终止，所以DaemonRunner需要终止，但是其中的finally块并没有执行
//所以在构建Daemon线程的时候，不能依靠finally块中的内容来确保执行关闭或者清理资源的逻辑
