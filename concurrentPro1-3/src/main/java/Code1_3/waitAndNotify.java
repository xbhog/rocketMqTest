package Code1_3;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/6
 */
public class waitAndNotify {
    /**
     * 创建资源
     */
    private static volatile Object resourceA = new Object();
    public static void main(String[] args) throws InterruptedException {
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA){
                    System.out.println("a get resource lock");
                    try {
                        System.out.println("a begin wait");
                        resourceA.wait();
                        System.out.println("a end wait");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA) {
                    System.out.println("b get resourceA lock");
                    try {
                        System.out.println("b begin wait");
                        resourceA.wait();
                        System.out.println("b end wait");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        // 创建线程
        Thread threadC = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (resourceA) {
                    System.out.println("c begin notify");
                    resourceA.notifyAll();
                }
            }
        });
        threadA.start();
        threadB.start();
        Thread.sleep(1000);
        threadC.start();
        threadA.join();
        threadB.join();
        threadC.join();
        System.out.println("main over");
    }
}
