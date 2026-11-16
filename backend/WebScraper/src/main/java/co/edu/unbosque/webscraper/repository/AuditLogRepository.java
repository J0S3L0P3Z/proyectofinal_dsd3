package co.edu.unbosque.webscraper.repository;

import co.edu.unbosque.webscraper.model.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio de los registros de auditoría, con filtros dinámicos mediante especificaciones.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
}
