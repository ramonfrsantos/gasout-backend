package br.com.gasoutapp.infrastructure.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

	List<Notification> findAllByUserEmailOrderByDateAsc(String userEmail);

}