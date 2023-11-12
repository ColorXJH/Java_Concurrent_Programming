package com.master._04javaConcurrentProgrammingBase;

import java.sql.Connection;
import java.util.LinkedList;

/**
 * @ClassName: ConnectionPool
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 数据库连接池
 * @Datetime: 2023/11/12 11:20
 * @author: ColorXJH
 */
public class ConnectionPool {
    /**
     * 它通过构造函数初始化连接的最大上限，通过一个双向队列
     * 来维护连接，调用方需要先调用fetchConnection(long)方法来指定在多少毫秒内超时获取连接，
     * 当连接使用完成后，需要调用releaseConnection(Connection)方法将连接放回线程池
     */
    private LinkedList<Connection>pool=new LinkedList<>();
    public ConnectionPool(int initialSize){
        if(initialSize>0){
            for (int i = 0; i < initialSize; i++) {
                pool.addLast(ConnectionDriver.createConnection());
            }
        }
    }

    public void releaseConnection(Connection connection){
        if(connection!=null){
            synchronized (pool){
                //连接释放后需要进行通知，这样其他消费者能够感知到连接池中已经归还了一个连接
                pool.addLast(connection);
                pool.notifyAll();
            }
        }
    }
    //在mills内无法获取到连接，则返回null
    public Connection fetchConnection(long mills) throws InterruptedException {
        synchronized (pool){
            //完全超时的时候（可有可无？）
            //因为在mills<0完全超时的情况下，else代码块中，仍然符合如果连接池不为空就返回连接池，如果为空就返回null的情况
            if(mills<=0){
                while (pool.isEmpty()){
                    pool.wait();
                }
                //空的会抛出异常
                return pool.removeFirst();
            }else{
                long future=System.currentTimeMillis()+mills;
                long remaining=mills;
                while (pool.isEmpty()&&remaining>0){
                    pool.wait(remaining);
                    remaining=future-System.currentTimeMillis();
                }
                Connection result=null;
                if(!pool.isEmpty()){
                    //空的会抛出异常
                    result=pool.removeFirst();
                }
                return result;
            }
        }
    }
}
