package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.DocumentMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentMasterRepository extends JpaRepository<DocumentMasterEntity, String> {

    Optional<DocumentMasterEntity> findByDocId(String docId);
}
