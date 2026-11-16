package co.edu.unbosque.webscraper.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Petición que lleva el token recibido por correo (verificación de correo).
 *
 * @param token Token de un solo uso
 */
public record TokenRequestDTO(@NotBlank String token) {}
