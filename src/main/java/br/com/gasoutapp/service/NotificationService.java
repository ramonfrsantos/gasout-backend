package br.com.gasoutapp.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.domain.Notification;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.NotificationDTO;
import br.com.gasoutapp.exception.NotificationNotFoundException;
import br.com.gasoutapp.exception.UserNotFoundException;
import br.com.gasoutapp.repository.NotificationRepository;
import br.com.gasoutapp.repository.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }


    public List<Notification> getAllRecentNotifications(String login) {
        User user = userRepository.findByLogin(login);
        List<Notification> notifications = notificationRepository.findAllByUser(user);
        reverseList(notifications);

        return notifications;
    }

    public ResponseEntity<Object> createNotification(NotificationDTO dto) {
        List<Notification> newUserNotifications = new ArrayList<>();
        User newUser;
        User user = userRepository.findByLogin(dto.getEmail());
        if (user == null) {
            throw new UserNotFoundException();
        }
        newUser = user;

        List<Notification> notifications = notificationRepository.findAllByUser(user);
        if (notifications.size() >= 10) {
            setAllUserNotificationsNull(notifications, user);
        } else {
            newUserNotifications = notifications;
        }

        Notification newNotification = new Notification();

        newNotification.setUser(user);
        newNotification.setTitle(dto.getTitle());
        newNotification.setMessage(dto.getMessage());
        newNotification.setDate(new Date());

        newNotification = notificationRepository.save(newNotification);
        newUserNotifications.add(newNotification);

        newUser.setNotifications(newUserNotifications);
        userRepository.save(newUser);

        List<Notification> notificationsUserNull = notificationRepository.findAllByUser(null);
        if (notificationsUserNull.size() > 0) {
            for (Notification notification: notificationsUserNull){
                notification.setDeleted(true);
                notificationRepository.save(notification);
            }
        }

        URI locationNotification = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(newNotification.getId())
        .toUri();

        return ResponseEntity.created(locationNotification).build();
    }

    public void deleteNotification(String id) {
        Notification notification = notificationRepository.getById(id);
        if(notification == null){
            throw  new NotificationNotFoundException();
        } else {
            notification.setDeleted(true);
            notificationRepository.save(notification);
        }
    }

    public void setAllUserNotificationsNull(List<Notification> notifications, User user) {
        if (user != null) {
            User newUser;
            newUser = user;
            List<Notification> newNotificationsList = new ArrayList<>();
            newUser.setNotifications(newNotificationsList);
            userRepository.save(newUser);
        }
        for (Notification notification : notifications) {
            notification.setUser(null);
            notificationRepository.save(notification);
        }
    }

    public static <T> void reverseList(List<T> list) {
        // base case: the list is empty, or only one element is left
        if (list == null || list.size() <= 1) {
            return;
        }
        // remove the first element
        T value = list.remove(0);
        // recur for remaining items
        reverseList(list);
        // insert the top element back after recurse for remaining items
        list.add(value);
    }


    public Optional<Notification> findNotificationById(String id) {
      return notificationRepository.findById(id);
    }
}