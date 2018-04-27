package querydsl;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathInits;
import db.domain.User;

import javax.annotation.Nullable;

public class QUser extends EntityPathBase<User> {

    public QUser(String variable) {
        super(User.class, variable);
    }

    public QUser(Class<? extends User> type, String variable) {
        super(type, variable);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata) {
        super(type, metadata);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, @Nullable PathInits inits) {
        super(type, metadata, inits);
    }

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

}
