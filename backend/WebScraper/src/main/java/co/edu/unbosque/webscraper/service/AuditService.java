package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.AuditLog;
import co.edu.unbosque.webscraper.repository.AuditLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Registra los movimientos del usuario en la tabla audit_logs. Corre en su propia transacción,
 * así el registro queda aunque la operación principal falle (por ejemplo, un login fallido),
 * y un error de auditoría nunca tumba la petición.
 */
@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final int MAX_DETAIL = 590;

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String username, AuditAction action, String detail) {
        try {
            String d = detail == null ? null : (detail.length() > MAX_DETAIL ? detail.substring(0, MAX_DETAIL) : detail);
            repo.saveAndFlush(new AuditLog(userId, username, action, d, currentIp()));
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar la auditoría {}: {}", action, e.getMessage());
        }
    }

    private static String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes sra) {
            return sra.getRequest().getRemoteAddr();
        }
        return null;
    }
}
