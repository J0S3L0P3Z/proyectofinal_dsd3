package co.edu.unbosque.proyectomodulotres.exception;

public class EnvioCorreoException extends RuntimeException {

    public EnvioCorreoException(String mensaje) {
        super(mensaje);
    }
}