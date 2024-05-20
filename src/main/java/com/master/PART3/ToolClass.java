package com.master.PART3;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 工具类的实现
 * @date 2024-05-20 17:23
 */
public class ToolClass {
    //如何在普通的接口上添加获得-释放（acquire-release）协议，随后通过例子演示了如何使用协同操作的技巧来将类分割
    //以获得必要的并发控制，并在随后将其合并以提交性能，最后讨论如何将等待的线程隔离以管理通知

    //1：获得-释放 协议
    interface Sync{
        void acquire()throws InterruptedException;
        void release();
        boolean attempt(long times)throws InterruptedException;
    }
}
