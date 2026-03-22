package com.meeting.meeting_room_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "meeting_room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;

    private Integer capacity;

    private String location;

    private String equipment;

    private String status;

    private Long tenantId;

    public Long getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getName() {
        return roomName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getLocation() {
        return location;
    }

    public String getEquipment() {
        return equipment;
    }

    public String getStatus() {
        return status;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

}