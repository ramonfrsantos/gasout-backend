package br.com.gasoutapp.application.dto.audit;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonSerialize
@Data
public class RevisionDTO implements Serializable {
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	Object entity;
	Object revisionDetails;
	Object revisionType;
	Object updatedAttributes;
}