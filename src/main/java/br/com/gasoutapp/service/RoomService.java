package br.com.gasoutapp.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.SensorDetailsDTO;
import br.com.gasoutapp.exception.RoomAlreadyExistsException;
import br.com.gasoutapp.exception.RoomNotFoundException;
import br.com.gasoutapp.exception.UserNotFoundException;
import br.com.gasoutapp.repository.RoomRepository;
import br.com.gasoutapp.repository.UserRepository;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAllUserRooms(String login) {
        User user = userRepository.findByLogin(login);
        return roomRepository.findAllByUser(user);
    }

    public ResponseEntity<Object> createRoom(RoomDTO dto) {
        List<Room> newUserRooms;
        User newUser;

        User user = userRepository.findByLogin(dto.getUser().getEmail());
        if (user == null) {
            throw new UserNotFoundException();
        }
        newUser = user;

        List<Room> rooms = roomRepository.findAllByUser(user);
        for (Room room : rooms) {
            if (room.getName().equals(dto.getName())) {
                throw new RoomAlreadyExistsException();
            }
        }
        newUserRooms = rooms;

        Room newRoom = new Room();

        newRoom.setUser(user);
        newRoom.setName(dto.getName());
        newRoom.setUserEmail(user.getEmail());
        newRoom = roomRepository.save(newRoom);

        newUserRooms.add(newRoom);

        newUser.setRooms(newUserRooms);
        userRepository.save(newUser);

        URI locationRoom = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(newRoom.getId())
        .toUri();

    return ResponseEntity.created(locationRoom).build();
    }

    public Room sendRoomSensorValue(SensorDetailsDTO dto, String login) {
        Room newRoom = new Room();

        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new UserNotFoundException();
        }

        List<Room> rooms = roomRepository.findAllByUser(user);
        for (Room room : rooms) {
            if (room.getName().equalsIgnoreCase(dto.getName())) {
                newRoom = room;
                newRoom.setSensorValue(dto.getSensorValue());
                newRoom.setAlarmOn(dto.isAlarmOn());
                newRoom.setNotificationOn(dto.isNotificationOn());
                newRoom.setSprinklersOn(dto.isSprinklersOn());
                roomRepository.save(newRoom);
            }
        }

        return newRoom;
    }

    public void deleteRoom(String id) {
        Room room = roomRepository.getById(id);
        if (room == null) {
            throw new RoomNotFoundException();
        } else {
            room.setDeleted(true);
            roomRepository.save(room);
        }
    }

    public Optional<Room> findRoomById(String id) {
        return roomRepository.findById(id);
    }
}