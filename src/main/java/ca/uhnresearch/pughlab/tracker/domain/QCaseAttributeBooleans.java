package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.CaseAttributeBooleans;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

/**
 * Query mapping for the boolean attributes table. 
 * @author stuartw
 */
public class QCaseAttributeBooleans extends QCaseAttributeBase<CaseAttributeBooleans> {

    private static final long serialVersionUID = -1225368846;

    public static final QCaseAttributeBooleans caseAttributeBooleans = new QCaseAttributeBooleans("case_attribute_booleans");

    public final BooleanPath value = createBoolean("value");

    public final com.mysema.query.sql.PrimaryKey<CaseAttributeBooleans> primary = createPrimaryKey(id);

    public QCaseAttributeBooleans(String variable) {
        super(CaseAttributeBooleans.class, forVariable(variable), "null", "case_attribute_booleans");
        addMetadata();
    }

    public void addMetadata() {
    	super.addMetadata();
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.BIT));
    }

}

