package com.master.PART1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 通过反射机制实现方法适配器
 * @date 2024-02-18 10:58
 */
public class ReflectAdapterMethod {
    public static void main(String[] args) {

    }
}
//需要适配的类
class Adaptee{
    void specialRequest(String message){
        System.out.println("Adaptee.specialRequest  "+message);
    }
}
//方法适配器类
class MethodAdapter{
    private Adaptee adaptee;

    public MethodAdapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
    //通用的方法调用方法
    Object invokeMethod(String methodName,Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] parameterType=new Class[args.length];
        for (int i=0;i<args.length;i++){
            parameterType[i]=args[i].getClass();
        }
        Method method=Adaptee.class.getMethod(methodName,parameterType);
        return method.invoke(adaptee,args);
    }
}