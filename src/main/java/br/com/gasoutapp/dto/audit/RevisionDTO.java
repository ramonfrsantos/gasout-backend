package br.com.gasoutapp.dto.audit;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@JsonSerialize
@Getter
@Setter
public class RevisionDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Object entity;
	Object revisionDetails;
	Object revisionType;
	Object updatedAttributes;
}