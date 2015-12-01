package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.ComparableExpressionBase;
import com.mysema.query.types.path.StringPath;

import java.sql.Types;

/**
 * Query mapping for the string attributes table. 
 * @author stuartw
 */
public class QCaseAttributeStrings extends QCaseAttributeBase<String> {

    private static final long serialVersionUID = -1985998853;

    public static final QCaseAttributeStrings caseAttributes = new QCaseAttributeStrings("case_attribute_strings");

    public QCaseAttributeStrings(String variable) {
        super(forVariable(variable), "null", "CASE_ATTRIBUTE_STRINGS");
    }

    public final StringPath value = createString("value");
    
    @Override
    public ComparableExpressionBase<? extends Comparable<?>> getValue() {
    	return value;
    };
    
    @Override
    public Path<String> getValuePath(Class<? extends Object> cls) {
    	return value;
    };

	@Override
	public OrderSpecifier<? extends Comparable<?>> getValueOrderSpecifier(boolean ascending) {
		return ascending ? value.asc() : value.desc();
	}

    {
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(4096));
    }
}

