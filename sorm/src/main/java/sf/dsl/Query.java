package sf.dsl;


public interface Query<T> {
    default Class<T> getClz() {
        return null;
    }

    String getId();


    @SuppressWarnings("unchecked")
    Query from();

    @SuppressWarnings("unchecked")
    Query forUpdate();

    Query copy();

}
