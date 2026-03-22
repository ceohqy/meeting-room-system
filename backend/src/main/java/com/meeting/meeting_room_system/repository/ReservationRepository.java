package com.meeting.meeting_room_system.repository;

import com.meeting.meeting_room_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 查询时间冲突
    @Query("SELECT r FROM Reservation r WHERE r.roomId = ?1 AND r.reservationDate = ?2 AND r.startTime < ?4 AND r.endTime > ?3")
    List<Reservation> findConflict(Long roomId,
                                   LocalDate reservationDate,
                                   LocalTime startTime,
                                   LocalTime endTime);

    // 查询某会议室预约
    List<Reservation> findByRoomId(Long roomId);

    // 查询某用户预约
    List<Reservation> findByUserId(Long userId);

    // 查询某一天预约数量
    long countByReservationDate(LocalDate reservationDate);

    // 按租户查询某一天预约数量
    long countByReservationDateAndTenantId(LocalDate reservationDate, Long tenantId);

    // 会议室使用统计（全部，按本月，降序TOP10）
    @Query(value =
            "SELECT meeting_room.room_name,COUNT(*) " +
                    "FROM reservation " +
                    "LEFT JOIN meeting_room ON reservation.room_id = meeting_room.id " +
                    "WHERE YEAR(reservation.reservation_date) = YEAR(CURDATE()) " +
                    "AND MONTH(reservation.reservation_date) = MONTH(CURDATE()) " +
                    "GROUP BY meeting_room.room_name " +
                    "ORDER BY COUNT(*) DESC LIMIT 10",
            nativeQuery = true)
    List<Object[]> countRoomUsage();

    // 会议室使用统计（按租户，按本月，降序TOP10）
    @Query(value =
            "SELECT meeting_room.room_name,COUNT(*) " +
                    "FROM reservation " +
                    "LEFT JOIN meeting_room ON reservation.room_id = meeting_room.id " +
                    "WHERE reservation.tenant_id = ?1 " +
                    "AND YEAR(reservation.reservation_date) = YEAR(CURDATE()) " +
                    "AND MONTH(reservation.reservation_date) = MONTH(CURDATE()) " +
                    "GROUP BY meeting_room.room_name " +
                    "ORDER BY COUNT(*) DESC LIMIT 10",
            nativeQuery = true)
    List<Object[]> countRoomUsageByTenantId(Long tenantId);

    // 按租户统计总数
    long countByTenantId(Long tenantId);

    // 按状态和租户统计
    long countByStatusAndTenantId(String status, Long tenantId);

    // 按状态统计
    long countByStatus(String status);

    // 按日期范围查询（全部）
    @Query("SELECT r FROM Reservation r WHERE r.reservationDate >= ?1 AND r.reservationDate <= ?2")
    List<Reservation> findByDateRange(LocalDate from, LocalDate to);

    // 按日期范围查询（按租户）
    @Query("SELECT r FROM Reservation r WHERE r.tenantId = ?1 AND r.reservationDate >= ?2 AND r.reservationDate <= ?3")
    List<Reservation> findByTenantIdAndDateRange(Long tenantId, LocalDate from, LocalDate to);

    // 时段分布（全部）
    @Query(value = "SELECT HOUR(start_time), COUNT(*) FROM reservation GROUP BY HOUR(start_time)", nativeQuery = true)
    List<Object[]> countByHour();

    // 时段分布（按租户）
    @Query(value = "SELECT HOUR(start_time), COUNT(*) FROM reservation WHERE tenant_id = ?1 GROUP BY HOUR(start_time)", nativeQuery = true)
    List<Object[]> countByHourAndTenantId(Long tenantId);

}