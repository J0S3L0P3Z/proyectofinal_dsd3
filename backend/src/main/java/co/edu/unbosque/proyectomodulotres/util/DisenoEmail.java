package co.edu.unbosque.proyectomodulotres.util;

public class DisenoEmail {

	public static String crearHTML(String nombreUsuario, String url, String estado, String detalle) {

		String colorEstado;

		if ("COMPLETADO".equalsIgnoreCase(estado)) {
			colorEstado = "#198754";
		} else {
			colorEstado = "#dc3545";
		}

		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				</head>
				<body style="margin:0; padding:30px; background-color:#6B422D;
				             font-family:Arial,sans-serif;">
				    <div style="max-width:600px; margin:auto;
				                background-color:white; border-radius:10px;
				                overflow:hidden;">
				        <div style="background-color:#40C9A2; padding:25px;
				                    text-align:center; color:white;">
				            <h2 style="margin:0;">ScrapingDoo!</h2>
				            <p style="margin-bottom:0;">
				                Notificación del análisis web
				            </p>
				        </div>
				        <div style="padding:30px; color:#333333;">
				            <h3>Hola, %s</h3>
				            <p>
				                Te informamos que el proceso de scraping
				                de tu página web ha finalizado.
				            </p>
				            <p><strong>URL analizada:</strong></p>
				            <p style="word-break:break-all;">%s</p>
				            <p><strong>Estado del proceso:</strong></p>
				            <p style="color:%s; font-weight:bold;">%s</p>
				            <p><strong>Detalles:</strong></p>
				            <p>%s</p>
				            <hr style="border:0; border-top:1px solid #dddddd;">
				            <p>
				                Se adjunta el reporte PDF con la información
				                del proceso.
				            </p>
				        </div>
				        <div style="background-color:#40C9A2; height:20px;"></div>
				        <div style="background-color:#20A987; height:6px;"></div>
				        <div style="background-color:#f1f3f5; padding:18px;
				                    text-align:center; color:#666666;
				                    font-size:12px;">
				            <p>ScrapingDoo!</p>
				            <p>Notificación automática. No responder a este correo.</p>
				        </div>
				    </div>
				</body>
				</html>
				""".formatted(escaparHTML(nombreUsuario), escaparHTML(url), colorEstado, escaparHTML(estado),
				escaparHTML(detalle));
	}

	public static String crearTextoPlano(String nombreUsuario, String url, String estado, String detalle) {

		return "Hola, " + nombreUsuario + "\n\n" + "El proceso de scraping ha finalizado.\n\n" + "URL analizada: " + url
				+ "\n" + "Estado: " + estado + "\n" + "Detalles: " + detalle + "\n\n" + "Se adjunta el reporte PDF.\n\n"
				+ "ScrapingDoo!";
	}

	public static String crearHTMLRecuperacion(String nombreUsuario, String codigo) {

		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				</head>
				<body style="margin:0; padding:30px; background-color:#6B422D;
				             font-family:Arial,sans-serif;">
				    <div style="max-width:600px; margin:auto;
				                background-color:white; border-radius:10px;
				                overflow:hidden;">
				        <div style="background-color:#40C9A2; padding:25px;
				                    text-align:center; color:white;">
				            <h2 style="margin:0;">ScrapingDoo!</h2>
				            <p style="margin-bottom:0;">Recuperación de contraseña</p>
				        </div>
				        <div style="padding:30px; color:#333333;">
				            <h3>Hola, %s</h3>
				            <p>
				                Recibimos una solicitud para recuperar tu contraseña.
				                Utiliza el siguiente código:
				            </p>
				            <div style="background-color:#f1f3f5; padding:20px;
				                        text-align:center; border-radius:8px;">
				                <h1 style="color:#20A987; letter-spacing:8px;
				                           margin:0;">%s</h1>
				            </div>
				            <p>
				                Este código vence en <strong>10 minutos</strong>
				                y solamente puede utilizarse una vez.
				            </p>
				            <p>
				                Si no solicitaste recuperar tu contraseña,
				                puedes ignorar este correo.
				            </p>
				        </div>
				        <div style="background-color:#40C9A2; height:20px;"></div>
				        <div style="background-color:#20A987; height:6px;"></div>
				        <div style="background-color:#f1f3f5; padding:18px;
				                    text-align:center; color:#666666;
				                    font-size:12px;">
				            <p>ScrapingDoo!</p>
				            <p>Correo automático de seguridad. No responder.</p>
				        </div>
				    </div>
				</body>
				</html>
				""".formatted(escaparHTML(nombreUsuario), escaparHTML(codigo));
	}

	public static String crearTextoPlanoRecuperacion(String nombreUsuario, String codigo) {

		return "Hola, " + nombreUsuario + "\n\n" + "Recibimos una solicitud para recuperar tu contraseña.\n\n"
				+ "Tu código de recuperación es: " + codigo + "\n\n"
				+ "El código vence en 10 minutos y solamente puede utilizarse una vez.\n\n"
				+ "Si no solicitaste recuperar tu contraseña, ignora este correo.\n\n" + "ScrapingDoo!";
	}

	private static String escaparHTML(String texto) {

		if (texto == null) {
			return "";
		}

		return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
