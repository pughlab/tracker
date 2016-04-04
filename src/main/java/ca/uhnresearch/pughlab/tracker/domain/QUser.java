package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.User;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

/**
 * Model for a user object. We do not store the primary key here, as we never expose it for anyone to
 * use. This is intentional. The username should be the main handle. 
 * 
 * @author stuartw
 */
public class QUser extends com.mysema.query.sql.RelationalPathBase<User> {

	private static final long serialVersionUID = -8721922821765112884L;

	public static final QUser users = new QUser("users");

    public final StringPath username = createString("username");

    public final StringPath email = createString("email");

    public final StringPath displayName = createString("displayName");

    public final StringPath givenName = createString("givenName");

    public final StringPath familyName = createString("familyName");

    public QUser(String variable) {
        super(User.class, forVariable(variable), "null", "USERS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(username, ColumnMetadata.named("USERNAME").withIndex(2).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(email, ColumnMetadata.named("EMAIL").withIndex(3).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(displayName, ColumnMetadata.named("DISPLAY_NAME").withIndex(4).ofType(Types.VARCHAR).withSize(64));
        addMetadata(givenName, ColumnMetadata.named("GIVEN_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(64));
        addMetadata(familyName, ColumnMetadata.named("FAMILY_NAME").withIndex(6).ofType(Types.VARCHAR).withSize(64));
    }
}

