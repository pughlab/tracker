package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.CaseAttributeDates;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

public class QCaseAttributeDates extends QCaseAttributeBase<CaseAttributeDates> {

    private static final long serialVersionUID = 1605860926;

    public static final QCaseAttributeDates caseAttributeDates = new QCaseAttributeDates("case_attribute_dates");

    public final DatePath<java.sql.Date> value = createDate("value", java.sql.Date.class);

    public final com.mysema.query.sql.PrimaryKey<CaseAttributeDates> primary = createPrimaryKey(id);

    public QCaseAttributeDates(String variable) {
        super(CaseAttributeDates.class, forVariable(variable), "null", "case_attribute_dates");
        addMetadata();
    }

    public void addMetadata() {
    	super.addMetadata();
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.DATE).withSize(10));
    }

}

