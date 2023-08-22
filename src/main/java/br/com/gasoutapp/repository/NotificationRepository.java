package br.com.gasoutapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gasoutapp.domain.Notification;
import br.com.gasoutapp.domain.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

	List<Notification> findAllByUserOrderByDateAsc(User user);

}