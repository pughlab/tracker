package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.ComparableExpressionBase;
import com.mysema.query.types.path.DatePath;

import java.sql.Types;

/**
 * Query mapping for the date attributes table. 
 * @author stuartw
 */
public class QCaseAttributeDates extends QCaseAttributeBase<java.sql.Date> {

    private static final long serialVersionUID = 1605860926;

    public static final QCaseAttributeDates caseAttributes = new QCaseAttributeDates("case_attribute_dates");

    public QCaseAttributeDates(String variable) {
        super(forVariable(variable), "null", "CASE_ATTRIBUTE_DATES");
    }

    public final DatePath<java.sql.Date> value = createDate("value", java.sql.Date.class);

    @Override
    public Path<java.sql.Date> getValuePath(Class<? extends Object> cls) {
    	return value;
    };

	@Override
	public ComparableExpressionBase<? extends Comparable<?>> getValue() {
		return value;
	}

	@Override
	public OrderSpecifier<? extends Comparable<?>> getValueOrderSpecifier(boolean ascending) {
		return ascending ? value.asc() : value.desc();
	}

    {
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.DATE).withSize(10));
    }
}

