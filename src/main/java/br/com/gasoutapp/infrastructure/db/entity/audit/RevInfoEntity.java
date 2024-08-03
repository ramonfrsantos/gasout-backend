package br.com.gasoutapp.infrastructure.db.entity.audit;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@RevisionEntity
@Table(name = "revinfo", catalog = "audit")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RevInfoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@RevisionNumber
	private int id;

	@RevisionTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
}