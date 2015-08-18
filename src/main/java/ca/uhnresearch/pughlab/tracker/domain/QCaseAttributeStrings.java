package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.CaseAttributeStrings;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

/**
 * Query mapping for the string attributes table. 
 * @author stuartw
 */
public class QCaseAttributeStrings extends QCaseAttributeBase<CaseAttributeStrings> {

    private static final long serialVersionUID = -1985998853;

    public static final QCaseAttributeStrings caseAttributeStrings = new QCaseAttributeStrings("case_attribute_strings");

    public final StringPath value = createString("value");

    public final com.mysema.query.sql.PrimaryKey<CaseAttributeStrings> primary = createPrimaryKey(id);

    public QCaseAttributeStrings(String variable) {
        super(CaseAttributeStrings.class, forVariable(variable), "null", "case_attribute_strings");
        addMetadata();
    }

    public void addMetadata() {
    	super.addMetadata();
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.VARCHAR).withSize(4096));
    }
}

