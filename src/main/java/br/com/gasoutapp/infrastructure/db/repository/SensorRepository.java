package br.com.gasoutapp.infrastructure.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gasoutapp.infrastructure.db.entity.enums.SensorTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.room.Sensor;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {

	List<Sensor> findAllByRoom(Room newRoom);
	
	@Query("SELECT s.sensorValue FROM Sensor s WHERE s.room = :room AND s.sensorType = :type ORDER BY s.timestamp DESC")
    List<Double> findRecentGasValueByRoomOrderByTimestampDesc(@Param("room") Room room, @Param("type") SensorTypeEnum sensorType);

	@Query("SELECT s FROM Sensor s WHERE s.room = :room AND s.sensorType = :type ORDER BY s.timestamp ASC")
    List<Sensor> findOldestSensorByRoom(@Param("room") Room room, @Param("type") SensorTypeEnum sensorType);
}