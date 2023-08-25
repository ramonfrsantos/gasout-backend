package br.com.gasoutapp.dto;

import lombok.Data;

@Data
public class RevisionDetailsDTO {
	Object entity;
	Object revisionDetails;
	Object revisionType;
	Object updatedAttributes;
}