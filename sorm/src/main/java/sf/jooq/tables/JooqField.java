package sf.jooq.tables;

import org.jooq.*;

/**
 * 列定义
 * @param <R>
 * @param <T>
 * @param <X>
 * @param <U>
 */
public class JooqField<R extends Record, T, X, U> {
    private String name;
    private DataType<T> type;
    private Table<R> table;
    private String comment;
    private Converter<X, U> converter;
    private Binding<T, X> binding;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType<T> getType() {
        return type;
    }

    public void setType(DataType<T> type) {
        this.type = type;
    }

    public Table<R> getTable() {
        return table;
    }

    public void setTable(Table<R> table) {
        this.table = table;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Converter<X, U> getConverter() {
        return converter;
    }

    public void setConverter(Converter<X, U> converter) {
        this.converter = converter;
    }

    public Binding<T, X> getBinding() {
        return binding;
    }

    public void setBinding(Binding<T, X> binding) {
        this.binding = binding;
    }
}