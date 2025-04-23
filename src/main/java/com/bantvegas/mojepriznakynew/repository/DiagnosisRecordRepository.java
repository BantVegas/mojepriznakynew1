package com.bantvegas.mojepriznakynew.repository;

import com.bantvegas.mojepriznakynew.model.DiagnosisRecord;
import com.bantvegas.mojepriznakynew.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRecordRepository extends JpaRepository<DiagnosisRecord, Long> {

    // Používa sa v DiagnoseController pre získanie histórie z prihláseného používateľa
    List<DiagnosisRecord> findByUserOrderByTimestampDesc(User user);

    // Alternatívna verzia (ak potrebuješ podľa emailu)
    List<DiagnosisRecord> findAllByUserEmailOrderByTimestampDesc(String email);
}
