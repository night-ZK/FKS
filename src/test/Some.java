package test;

public class Some {
    public static void main(String[] args) {
        Integer i = 1;
        Integer a = new Integer(1);
        int a1 = 1;
        System.out.println( a1 == i);

        String s = "s1";
        String s1 = "s1";
        String s2 = new String("s1");
        System.out.println(s.equals(s2));
    }
}
