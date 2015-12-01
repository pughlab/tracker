package ca.uhnresearch.pughlab.tracker.domain;

import java.sql.Types;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.ComparableExpressionBase;
import com.mysema.query.types.path.NumberPath;

/**
 * Query mapping for the number attributes table. We could represent numbers as strings, 
 * but in practice this allows us to sort more effectively, which is a useful feature.
 * @author stuartw
 */
public class QCaseAttributeNumbers extends QCaseAttributeBase<Double> {

    private static final long serialVersionUID = -1985998853;

    public static final QCaseAttributeNumbers caseAttributes = new QCaseAttributeNumbers("case_attribute_numbers");

    public QCaseAttributeNumbers(String variable) {
        super(forVariable(variable), "null", "CASE_ATTRIBUTE_NUMBERS");
    }

    public final NumberPath<Double> value = createNumber("value", Double.class);
    
    @Override
    public ComparableExpressionBase<? extends Comparable<?>> getValue() {
    	return value;
    };
    
    @Override
    public Path<Double> getValuePath(Class<? extends Object> cls) {
    	return (Path<Double>) value;
    };

	@Override
	public OrderSpecifier<? extends Comparable<?>> getValueOrderSpecifier(boolean ascending) {
		return ascending ? value.asc() : value.desc();
	}

    {
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.DOUBLE));
    }

}

