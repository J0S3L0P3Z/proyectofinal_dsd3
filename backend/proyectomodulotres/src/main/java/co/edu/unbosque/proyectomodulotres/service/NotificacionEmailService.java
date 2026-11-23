package co.edu.unbosque.proyectomodulotres.service;

import java.io.File;

/**
 * Interfaz del servicio de correo. Define los correos que envía la aplicación.
 */
public interface NotificacionEmailService {

	/**
	 * Envía al usuario el resultado de un scraping, con el PDF adjunto si existe.
	 *
	 * @param correoDestino Correo del usuario
	 * @param nombreUsuario Nombre del usuario
	 * @param url URL analizada
	 * @param estado Estado del scraping
	 * @param detalle Texto con el detalle del resultado
	 * @param archivoPDF Archivo PDF a adjuntar (puede ser null)
	 */
	void enviarNotificacion(String correoDestino, String nombreUsuario, String url, String estado, String detalle,
			File archivoPDF);

	/**
	 * Envía al usuario el código para recuperar la contraseña.
	 *
	 * @param correoDestino Correo del usuario
	 * @param nombreUsuario Nombre del usuario
	 * @param codigo Código de 6 dígitos
	 */
	void enviarCodigoRecuperacion(String correoDestino, String nombreUsuario, String codigo);
}
