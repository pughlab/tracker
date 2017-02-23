package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QStudy.studies;
import static ca.uhnresearch.pughlab.tracker.domain.QRole.roles;

import ca.uhnresearch.pughlab.tracker.domain.QRole;
import ca.uhnresearch.pughlab.tracker.domain.QStudy;
import ca.uhnresearch.pughlab.tracker.dto.Role;

import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

public class RoleStudyProjection extends MappingProjection<Role> {

	private static final long serialVersionUID = 5391772059404559453L;
	
	public RoleStudyProjection(QRole role, QStudy study) {
        super(Role.class, role.id, role.name, study.name, study.id);
    }

	@Override
	protected Role map(Tuple tuple) {
    	final Role product = new Role();
        product.setId(tuple.get(roles.id));
        product.setName(tuple.get(roles.name));
        product.setStudyName(tuple.get(studies.name));
        product.setStudyId(tuple.get(studies.id));
        return product;
	}

}
