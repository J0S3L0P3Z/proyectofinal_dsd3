package co.edu.unbosque.webscraper.dto;

import java.util.List;
import java.util.Map;

/**
 * Estadísticas generales para el panel de administración.
 *
 * @param totalUsers Usuarios registrados
 * @param enabledUsers Usuarios habilitados
 * @param newUsersLast7Days Usuarios nuevos en la última semana
 * @param totalJobs Scrapings realizados
 * @param jobsLast24Hours Scrapings en las últimas 24 horas
 * @param jobsByStatus Cantidad de jobs por estado
 * @param successRatePercent Porcentaje de jobs terminados con éxito sobre los terminados
 * @param avgLoadTimeMs Tiempo promedio de carga de las páginas
 * @param jobsPerDay Jobs por día en los últimos 30 días
 * @param topDomains Dominios más consultados
 * @param errorsByCode Errores agrupados por código del catálogo
 * @param aiProviders Rendimiento de cada IA
 */
public record AdminStatsDTO(
    long totalUsers,
    long enabledUsers,
    long newUsersLast7Days,
    long totalJobs,
    long jobsLast24Hours,
    Map<String, Long> jobsByStatus,
    double successRatePercent,
    Double avgLoadTimeMs,
    List<DayCount> jobsPerDay,
    List<NameCount> topDomains,
    List<NameCount> errorsByCode,
    List<ProviderStat> aiProviders) {

  /** Un nombre con su cantidad. */
  public record NameCount(String name, long count) {}

  /** Una fecha (aaaa-mm-dd) con su cantidad. */
  public record DayCount(String day, long count) {}

  /** Respuestas correctas y fallidas de una IA y su tiempo promedio. */
  public record ProviderStat(String provider, long ok, long failed, Double avgDurationMs) {}
}
