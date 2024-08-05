package br.com.gasoutapp.domain.service.audit;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;

import java.util.List;

public interface RevisionService {
	<T> List<RevisionDTO> getRevisions(String id, Class<T> classType);
}