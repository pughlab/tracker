package ca.uhnresearch.pughlab.tracker.domain;

import java.sql.Types;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.NumberPath;

import ca.uhnresearch.pughlab.tracker.dto.CaseAttributeNumbers;

/**
 * Query mapping for the number attributes table. We could represent numbers as strings, 
 * but in practice this allows us to sort more effectively, which is a useful feature.
 * @author stuartw
 */
public class QCaseAttributeNumbers extends QCaseAttributeBase<CaseAttributeNumbers> {

    private static final long serialVersionUID = -1985998853;

    public static final QCaseAttributeStrings caseAttributeStrings = new QCaseAttributeStrings("case_attribute_numbers");

    public final NumberPath<Double> value = this.createNumber("value", Double.class);

    public final com.mysema.query.sql.PrimaryKey<CaseAttributeNumbers> primary = createPrimaryKey(id);

    public QCaseAttributeNumbers(String variable) {
        super(CaseAttributeNumbers.class, forVariable(variable), "null", "case_attribute_numbers");
        addMetadata();
    }

    public void addMetadata() {
    	super.addMetadata();
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(3).ofType(Types.DOUBLE));
    }
}

