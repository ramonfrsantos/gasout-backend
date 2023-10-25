package br.com.gasoutapp.domain.audit;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionListener;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@RevisionEntity(RevisionListener.class)
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}