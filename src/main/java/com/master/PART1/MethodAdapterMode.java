package com.master.PART1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 方法适配器模式，是一种设计模式，它允许将一个已经存在的方法转换成另一个接口所期望的方法格式
 * 使得原本不兼容的接口可以一起工作,通常使用组合模式，将现有类的实例作为成员变量引入到适配器中，然后在适配器中实现
 * 新接口的方法，通过调用现有类的方法来实现适配。
 * @date 2024-02-18 9:45
 */
public class MethodAdapterMode {
    //ADT=Abstract Data Type 抽象数据类型
    public static void main(String[] args) {
        // 创建被适配的类的实例
        //Adaptee adaptee = new MethodAdapterMode().new Adaptee();
        // 创建适配器的实例，将被适配的类的实例传入
        //Target target = new MethodAdapterMode().new MethodAdapter(adaptee);
        // 调用目标接口的方法
        //target.request();
        System.out.println("000----000---");

        TankWithMethodAdapter tankWithMethodAdapter = new TankWithMethodAdapter();
        tankWithMethodAdapter.invokeTankOpMethod("op");
    }

    //目标接口
    interface Target {
        void request();
    }

    //需要适配的类
    class Adaptee {
        void specialRequest() {
            System.out.println("this is my special request");
        }
    }

    //方法适配器类
    class MethodAdapter implements Target {
        private Adaptee adaptee;

        MethodAdapter(Adaptee adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void request() {
            adaptee.specialRequest();
        }
    }


    //方法适配器的一些应用可以通过反射机制半自动的实现，一个通用的构造器可以在一个类中寻找一个特定的java.lang.reflect.Method对象
    //为其设置参数，调用它，并且返回结果，然而使用这种方式缺乏静态保证性，会增加额外的处理消耗，所以这种方式只是在需要处理未知的，需要动态加载的代码时c才会被使用
}

//另一种内部内的方法适配，类似于runnable和callable的run/call方法调用
//定义带有单个方法的接口，并生成一个实现类，通常使用匿名内部类的方式，然后将它作为参数传递
interface TankOp {
    void op();
}

class TankWithMethodAdapter implements TankOp {
    void runWithBeforeAfterChecks(TankOp cmd) {
        cmd.op();
    }

    void transformerWater() {
        runWithBeforeAfterChecks(new TankOp() {
            @Override
            public void op() {
                System.out.println("传递接口，用匿名内部类实现");
            }
        });
    }

    void invokeTankOpMethod(String methodName) {
        try {
            Method method = TankWithMethodAdapter.class.getMethod(methodName);
            method.invoke(this);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void op() {
        System.out.println("this is my op");
    }
    //在实际项目中，可以通过类加载器动态加载并使用新生成的类，从而实现动态的类生成和加载。
    //这样的技术在许多框架和库中得到了广泛的应用，比如Spring AOP、字节码增强库等
}
