package com.master._04javaConcurrentProgrammingBase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ConnectionDriver
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: Connection接口的动态代理实现
 * @Datetime: 2023/11/12 11:22
 * @author: ColorXJH
 */
public class ConnectionDriver {
    /**
     * 由于java.sql.Connection是一个接口，最终的实现是由数据库驱动提供方来实现的，考虑到
     * 只是个示例，我们通过动态代理构造了一个Connection，该Connection的代理实现仅仅是在
     * commit()方法调用时休眠100毫秒
     */
    static class ConnectionHandler implements InvocationHandler{
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
           if(method.getName().equals("commit")){
               TimeUnit.MILLISECONDS.sleep(100);
           }
           return null;
        }
    }
    //创建一个Connection的动态代理，在commit的时候休眠100毫秒
    public static final Connection createConnection(){
        return (Connection)Proxy.newProxyInstance(ConnectionDriver.class.getClassLoader(),new Class[]{Connection.class},new ConnectionHandler());
    }
}
