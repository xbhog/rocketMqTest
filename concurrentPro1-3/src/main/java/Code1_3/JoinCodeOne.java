package Code1_3;

/**
 * @author xbhog
 * @describe: join函数测试
 * @date 2022/7/9
 */
public class JoinCodeOne {
    public static void main(String[] args) throws InterruptedException {
        Thread threadOne = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread threadTwo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //启动子线程
        threadOne.start();
        threadTwo.start();
        System.out.println("wait all child thread over!");
        //不加等待，不管子线程执没执行完，main函数执行完成后程序结束
        threadOne.join();
        threadTwo.join();
        System.out.println("all child thread over");
    }
}
