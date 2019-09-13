package test;

public enum Singleton {
    INSTANCE;
    public Singleton getInstance(){return INSTANCE;}
}
class TestSingletonByEnum{
    public static void main(String[] args) {
        Singleton.INSTANCE.getInstance();
    }
}
