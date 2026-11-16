package co.edu.unbosque.webscraper.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Petición que solo lleva un correo (recuperar contraseña, reenviar verificación).
 *
 * @param email Correo electrónico
 */
public record EmailRequestDTO(@NotBlank @Email String email) {}
