package db;

import javax.persistence.*;

@SqlResultSetMappings({@SqlResultSetMapping(name = "OrderResults",
        entities = {
                @EntityResult(entityClass = db.UserResult.class, fields = {
                        @FieldResult(name = "id", column = "order_id"),
                        @FieldResult(name = "quantity", column = "order_quantity"),
                        @FieldResult(name = "item", column = "order_item")})},
        columns = {
                @ColumnResult(name = "item_name")}
)})
public class UserResult {
}
