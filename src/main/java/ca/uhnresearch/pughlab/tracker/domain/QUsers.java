package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUsers is a Querydsl query type for Users
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QUsers extends com.mysema.query.sql.RelationalPathBase<Users> {

    private static final long serialVersionUID = 259483715;

    public static final QUsers users = new QUsers("users");

    public final StringPath apikey = createString("apikey");

    public final StringPath email = createString("email");

    public final DateTimePath<java.sql.Timestamp> expires = createDateTime("expires", java.sql.Timestamp.class);

    public final BooleanPath forcePasswordChange = createBoolean("forcePasswordChange");

    public final StringPath hash = createString("hash");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath locked = createBoolean("locked");

    public final StringPath username = createString("username");

    public final com.mysema.query.sql.PrimaryKey<Users> primary = createPrimaryKey(id);

    public QUsers(String variable) {
        super(Users.class, forVariable(variable), "null", "users");
        addMetadata();
    }

    public QUsers(String variable, String schema, String table) {
        super(Users.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUsers(Path<? extends Users> path) {
        super(path.getType(), path.getMetadata(), "null", "users");
        addMetadata();
    }

    public QUsers(PathMetadata<?> metadata) {
        super(Users.class, metadata, "null", "users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(apikey, ColumnMetadata.named("apikey").withIndex(5).ofType(Types.VARCHAR).withSize(128));
        addMetadata(email, ColumnMetadata.named("email").withIndex(4).ofType(Types.VARCHAR).withSize(128));
        addMetadata(expires, ColumnMetadata.named("expires").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(forcePasswordChange, ColumnMetadata.named("force_password_change").withIndex(7).ofType(Types.BIT).notNull());
        addMetadata(hash, ColumnMetadata.named("hash").withIndex(3).ofType(Types.VARCHAR).withSize(60));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(locked, ColumnMetadata.named("locked").withIndex(6).ofType(Types.BIT).notNull());
        addMetadata(username, ColumnMetadata.named("username").withIndex(2).ofType(Types.VARCHAR).withSize(24).notNull());
    }

}

