package co.edu.unbosque.proyectomodulotres.service;

import java.io.File;

public interface NotificacionEmailService {

	void enviarNotificacion(String correoDestino, String nombreUsuario, String url, String estado, String detalle,
			File archivoPDF);

	void enviarCodigoRecuperacion(String correoDestino, String nombreUsuario, String codigo);
}
