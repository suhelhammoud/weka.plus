package sensetivity;

public class RunT {
    public static void main(String[] args) {
        get();
//        get("Ahmad", 2, "Salem", true, 0.0);
    }


    public static void get() {
        System.out.println("calling get() without parameters");

    }

    public static void get(Object... a) {
        if (a == null) {
            System.out.println("a is null");
        } else {
            System.out.println("a is NOT null");
            for (int i = 0; i < a.length; i++) {
                Object v = a[i];
                String typeName = v.getClass().getTypeName();
                System.out.println("i :" + i + " = " + a[i] + ", " + typeName);
            }
        }
    }
}
