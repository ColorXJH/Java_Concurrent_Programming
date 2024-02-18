package com.master.PART1;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 行为设计模式，将一个请求封装成一个对象，从而使得可以参数化其他对象，以请求的方式执行某些操作
 * @date 2024-02-18 9:17
 */
public class CommandObject {
    public static void main(String[] args) {
        //创建接收者对象
        Receiver receiver=new CommandObject().new Receiver();
        //创建命令对象并设置接收者
        Command command=new CommandObject().new CommandImpl(receiver);
        //创建调用者对象
        Invoker invoker=new CommandObject().new Invoker();
        //设置命令对象
        invoker.setCommand(command);
        //执行命令
        invoker.executeCommand();
    }
    //Command接口
    interface Command{
        void execute();
    }
    //Command实现类
    class CommandImpl implements Command{
        private Receiver receiver;

        CommandImpl(Receiver receiver) {
            this.receiver = receiver;
        }

        @Override
        public void execute() {
            receiver.action();
        }
    }
    //Receiver接收者类
    class Receiver{
        void action(){
            System.out.println("Receiver.action");
        }
    }
    //Invoker调用者类
    class Invoker{
        private Command command;

        public void setCommand(Command command) {
            this.command = command;
        }

        void executeCommand(){
            command.execute();
        }
    }
}
