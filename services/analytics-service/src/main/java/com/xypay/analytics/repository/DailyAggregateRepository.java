package com.xypay.analytics.repository;

import com.xypay.analytics.domain.DailyAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface DailyAggregateRepository extends JpaRepository<DailyAggregate, Long> {
    Optional<DailyAggregate> findByDate(LocalDate date);

    List<DailyAggregate> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);
}


