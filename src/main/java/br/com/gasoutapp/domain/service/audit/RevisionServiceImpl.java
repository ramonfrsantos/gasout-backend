package br.com.gasoutapp.domain.service.audit;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

import static br.com.gasoutapp.infrastructure.utils.JsonUtil.convertToObjectArray;

@Service
public class RevisionServiceImpl implements RevisionService {

	@Autowired
	private EntityManagerFactory factory;

	@Override
	public <T> List<RevisionDTO> getRevisions(String id, Class<T> classType) {
		var auditQuery = getAuditQuery(id ,classType);

		List<RevisionDTO> details = new ArrayList<>();

		for (Object revision : auditQuery.getResultList()) {
			var r = new RevisionDTO();
			var objArray = convertToObjectArray(revision);

			r.setEntity(objArray[0]);
			r.setRevisionDetails(objArray[1]);
			r.setRevisionType(objArray[2]);
			r.setUpdatedAttributes(objArray[3]);
			
			details.add(r);
		}

		return details;
	}

	private <T> AuditQuery getAuditQuery(String id, Class<T> classType) {
		var auditReader = AuditReaderFactory.get(factory.createEntityManager());

		return auditReader.createQuery().forRevisionsOfEntityWithChanges(classType, true)
				.add(AuditEntity.id().eq(id));
	}
}