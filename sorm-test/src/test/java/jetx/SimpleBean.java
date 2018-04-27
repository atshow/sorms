package jetx;

public class SimpleBean {
    public String name;
    private ComBean cb;
    private int age;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComBean getCb() {
        return cb;
    }

    public void setCb(ComBean cb) {
        this.cb = cb;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}