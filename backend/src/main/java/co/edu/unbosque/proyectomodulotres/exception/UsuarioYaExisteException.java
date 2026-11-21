package co.edu.unbosque.proyectomodulotres.exception;

public class UsuarioYaExisteException extends RuntimeException {

	public UsuarioYaExisteException(String mensaje) {
		super(mensaje);
	}
}