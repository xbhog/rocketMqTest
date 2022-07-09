package Code1_3;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author xbhog
 * @describe: 线程的三种创建方式
 * @date 2022/7/9
 */

public class createThreadWays {
    public static class MyThread extends Thread{
        @Override
        public void run(){
            System.out.println("创建线程");
        }
    }
    public static class RunnableThread implements Runnable{

        @Override
        public void run() {
            System.out.println("创建线程：runnable");
        }
    }
    public static class CallableThread implements Callable{

        @Override
        public String call() throws Exception {
            return "创建线程：callable";
        }
    }
    public static void main(String[] args) {
        MyThread myThread = new MyThread();
        //启动线程
        myThread.start();
        RunnableThread thread = new RunnableThread();
        //创建两个线程
        new Thread(thread).start();
        new Thread(thread).start();
        //创建Callable实现，比较麻烦
        CallableThread callableThread = new CallableThread();
        FutureTask<String> futureTask = new FutureTask<String>(callableThread);
        //启动线程
        new Thread(futureTask).start();
        try {
            String result = futureTask.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
