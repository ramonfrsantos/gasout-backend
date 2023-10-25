package br.com.gasoutapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gasoutapp.domain.notification.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

	List<Notification> findAllByUserEmailOrderByDateAsc(String userEmail);

}