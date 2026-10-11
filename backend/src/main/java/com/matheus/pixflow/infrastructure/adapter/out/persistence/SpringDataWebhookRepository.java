package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWebhookRepository extends JpaRepository<WebhookDeliveryJpaEntity, UUID> {
    List<WebhookDeliveryJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<WebhookDeliveryJpaEntity> findByChargeIdOrderByCreatedAtDesc(UUID chargeId);
}

