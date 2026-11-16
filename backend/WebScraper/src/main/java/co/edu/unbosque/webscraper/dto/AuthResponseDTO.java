package co.edu.unbosque.webscraper.dto;

/**
 * Respuesta del inicio de sesión: el token JWT, el rol y el nombre del usuario autenticado.
 *
 * @param token Token JWT (se envía luego como "Authorization: Bearer {token}")
 * @param role Rol del usuario (USER o ADMIN)
 * @param username Nombre de usuario
 */
public record AuthResponseDTO(String token, String role, String username) {}
