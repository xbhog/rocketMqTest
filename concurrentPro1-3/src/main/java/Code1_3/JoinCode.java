package Code1_3;

/**
 * @author xbhog
 * @describe: join函数测试2
 * @date 2022/7/7
 */
public class JoinCode {
    /**
     * 线程A调用线程B的join方法后会被阻塞，当其线程调用了线程A的
     * intrrupt（）方法中断了线程A时，线程A会抛出 InterruptedException 异常而返回。
     * @param args
     */
    public static void main(String[] args) {
        Thread threadOne = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("threadOne begin run!");
                for (; ; ) {
                }
            }
        });
        //获取主线程
        final Thread mainThread = Thread.currentThread();
        //线程2
        Thread threadTwo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //线程中断
                mainThread.interrupt();
            }
        });
        threadOne.start();
        threadTwo.start();
        try { //等待线程one执行结束
            threadOne.join();
        } catch (InterruptedException e) {
            System.out.println("main thread"+e);
        }
    }
}
