package com.master._04javaConcurrentProgrammingBase;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * @ClassName: MuitiThread
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: JMX==JAVA管理扩展 java management extensions
 * @Datetime: 2022/12/31 18:17
 * @author: ColorXJH
 */
public class MultiThread {
    public static void main(String[] args) {
        //获取java线程管理MXBean
        ThreadMXBean threadMXBean= ManagementFactory.getThreadMXBean();
        //不需要获取同步的monitor和synchronizer信息，仅获取线程和线程堆栈信息
        ThreadInfo[] threadInfos= threadMXBean.dumpAllThreads(false,false);
        //遍历线程信息，仅打印线程id和线程名称信息
        for (ThreadInfo threadInfo:threadInfos){
            System.out.println("["+threadInfo.getThreadId()+"]"+threadInfo.getThreadName());
        }
    }
    //[6]Monitor Ctrl-Break IDEA 通过反射的方式，开启一个随着我们运行的jvm进程开启与关闭的一个监听线程
    //[5]Attach Listener    附加监听器
    //[4]Signal Dispatcher  分发处理发送给JVM信号的线程
    //[3]Finalizer          调用对象finalize方法的线程
    //[2]Reference Handler  清除Reference的线程
    //[1]main               main线程，用户程序入口
}
