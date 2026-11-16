package co.edu.unbosque.webscraper.service;

import java.util.List;

/**
 * Interfaz genérica que define las operaciones CRUD básicas de un servicio. Los métodos de escritura
 * devuelven un código entero (0 = éxito) para que el controlador decida el estado HTTP.
 *
 * @param <T> Tipo de DTO con el que trabaja el servicio
 */
public interface CRUDOperation<T> {

  /**
   * Crea un nuevo registro.
   *
   * @param data Datos del registro
   * @return 0 si se creó; otro valor indica el motivo del fallo
   */
  public int create(T data);

  /**
   * Obtiene todos los registros.
   *
   * @return Lista de registros
   */
  public List<T> getAll();

  /**
   * Elimina un registro por su id.
   *
   * @param id Identificador del registro
   * @return 0 si se eliminó, 1 si no existe
   */
  public int deleteById(Long id);

  /**
   * Actualiza un registro por su id.
   *
   * @param id Identificador del registro
   * @param newData Nuevos datos
   * @return 0 si se actualizó; otro valor indica el motivo del fallo
   */
  public int updateById(Long id, T newData);

  /**
   * Cuenta los registros.
   *
   * @return Cantidad de registros
   */
  public long count();

  /**
   * Indica si existe un registro con ese id.
   *
   * @param id Identificador del registro
   * @return true si existe
   */
  public boolean exist(Long id);
}
