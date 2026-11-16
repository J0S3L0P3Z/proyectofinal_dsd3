package co.edu.unbosque.webscraper.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Petición para establecer una nueva contraseña con el token recibido por correo.
 *
 * @param token Token de un solo uso
 * @param newPassword Nueva contraseña (debe cumplir la política de contraseñas)
 */
public record ResetPasswordDTO(@NotBlank String token, @NotBlank String newPassword) {}
