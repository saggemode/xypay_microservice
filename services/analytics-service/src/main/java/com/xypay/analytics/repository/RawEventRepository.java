package com.xypay.analytics.repository;

import com.xypay.analytics.domain.RawEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RawEventRepository extends JpaRepository<RawEvent, Long> {
    List<RawEvent> findBySourceOrderByReceivedAtDesc(String source);

    @Query("SELECT r FROM RawEvent r WHERE r.receivedAt BETWEEN :start AND :end ORDER BY r.receivedAt DESC")
    List<RawEvent> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}


