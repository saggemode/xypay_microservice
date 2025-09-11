package com.xypay.xypay.repository;

import com.xypay.xypay.domain.KYCDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KYCDocumentRepository extends JpaRepository<KYCDocument, Long> {
}
