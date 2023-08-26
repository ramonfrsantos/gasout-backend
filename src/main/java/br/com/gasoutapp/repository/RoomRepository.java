package br.com.gasoutapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.domain.enums.RoomNameEnum;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

	List<Room> findAllByUserEmail(String userEmail);

	Optional<Room> findByUserEmailAndName(String userEmail, RoomNameEnum roomName);
}