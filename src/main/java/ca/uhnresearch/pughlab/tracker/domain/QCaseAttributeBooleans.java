package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.ComparableExpressionBase;
import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

/**
 * Query mapping for the boolean attributes table. 
 * @author stuartw
 */
public class QCaseAttributeBooleans extends QCaseAttributeBase<Boolean> {

    private static final long serialVersionUID = -1225368846;

    public static final QCaseAttributeBooleans caseAttributes = new QCaseAttributeBooleans("case_attribute_booleans");

    public final BooleanPath value = createBoolean("value");
    
    public QCaseAttributeBooleans(String variable) {
        super(forVariable(variable), "null", "CASE_ATTRIBUTE_BOOLEANS");
    }
    
    @Override
    public ComparableExpressionBase<? extends Comparable<?>> getValue() {
    	return value;
    };
    
    @Override
    public Path<Boolean> getValuePath(Class<? extends Object> cls) {
    	return value;
    };

    @Override
    public OrderSpecifier<? extends Comparable<?>> getValueOrderSpecifier(boolean ascending) {
    	return ascending ? value.asc() : value.desc();
    };

    {
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.BIT));
    }
}

