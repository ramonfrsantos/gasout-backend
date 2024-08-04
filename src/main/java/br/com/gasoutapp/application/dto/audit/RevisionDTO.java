package br.com.gasoutapp.application.dto.audit;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@JsonSerialize
@Data
public class RevisionDTO {
	Object entity;
	Object revisionDetails;
	Object revisionType;
	Object updatedAttributes;
}