package com.master.PART2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 享元模式 通过使用享元模式，可以将这些共享的属性和状态提取出来，并在需要时共享给多个字符或字母对象
 * @date 2024-02-18 15:24
 */
public class Flyweight {
    //在构造函数执行结束之前，不能访问对象的数据
    //构造函数应该只执行与初始化数据相关的操作
    //1：如果一个方法依赖于对象的初始化完成，那么构造函数就不应该调用该方法
    //2：如果一个对象是在其他类可存取的成员变量或表中创建，那么构造函数应该避免使用该对象的引用
    public static void main(String[] args) {
        //创建字符对象，共享字体对象，
        Font fonts1=FontFactory.getFont("宋体",12);
        Font fonts2=FontFactory.getFont("楷体",11);
        Font fonts3=FontFactory.getFont("宋体",12);
        //比较字体对象的引用
        System.out.println(fonts1==fonts2);
        System.out.println(fonts1==fonts3);
    }
}
//共享的字体对象（享元对象）
class Font{
    private String name;
    private int size;

    public Font(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Font{" +
                "name='" + name + '\'' +
                ", size=" + size +
                '}';
    }

}
//字体工厂类
class FontFactory{
    private static final Map<String,Font>fonts=new HashMap<>();
    //获取共享的字体对象
    public static Font getFont(String name,int size){
        String key=name+"_"+size;
        if(!fonts.containsKey(key)){
            fonts.put(key,new Font(name,size));
        }
        return fonts.get(key);
    }
}
