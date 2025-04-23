package com.bantvegas.mojepriznakynew.repository;

import com.bantvegas.mojepriznakynew.model.DiagnosisRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRecordRepository extends JpaRepository<DiagnosisRecord, Long> {
    List<DiagnosisRecord> findAllByUserEmailOrderByTimestampDesc(String email);
}
