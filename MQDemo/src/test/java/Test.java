/**
 * @author xbhog
 * @describe:
 * @date 2022/7/20
 */

public class Test {
    public static void main(String[] args) {
        Object a = 15103111139L;
        Object b = 15103117235L;
        Object c = 15103111065L;

        System.out.println("------------");
        long l1= (long) a;
        long l2 = (long) b;
        long l3 = (long) c;
        System.out.println(a+"--------"+b+"---------"+c);
        System.out.println("---------------");
        System.out.println((long) l1%4);
        System.out.println((long) l2%4);
        System.out.println((long) l3%4);

    }
}
