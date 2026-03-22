package com.meeting.meeting_room_system.service;

import com.meeting.meeting_room_system.entity.Room;
import com.meeting.meeting_room_system.exception.BusinessException;
import com.meeting.meeting_room_system.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> listAll() {
        return roomRepository.findAll();
    }

    public Room getById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    @Transactional
    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Room room) {
        if (!roomRepository.existsById(room.getId())) {
            throw new BusinessException("会议室不存在");
        }
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new BusinessException("会议室不存在");
        }
        roomRepository.deleteById(id);
    }
}
