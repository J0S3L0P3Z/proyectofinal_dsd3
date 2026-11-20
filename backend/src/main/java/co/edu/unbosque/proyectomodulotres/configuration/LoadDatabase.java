package co.edu.unbosque.proyectomodulotres.configuration;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

@Configuration
public class LoadDatabase {

	@Value("${app.admin.correo}")
	private String correoAdministrador;

	@Value("${app.admin.contrasena}")
	private String contrasenaAdministrador;

	@Bean
	public CommandLineRunner inicializarAdministrador(UserRepository userRepository, PasswordEncoder passwordEncoder) {

		return args -> {

			User administrador = userRepository.findByUsername(correoAdministrador).orElse(null);

			if (administrador == null) {

				administrador = new User();

				administrador.setUsername(correoAdministrador);
				administrador.setNombre("Administrador");
				administrador.setApellido("Sistema");
				administrador.setRole(User.Role.ADMIN);
				administrador.setFechaRegistro(LocalDate.now());
				administrador.setEnabled(true);
				administrador.setActivo(true);
				administrador.setAccountNonExpired(true);
				administrador.setAccountNonLocked(true);
				administrador.setCredentialsNonExpired(true);

				System.out.println("Administrador inicial creado correctamente.");

			} else {

				administrador.setRole(User.Role.ADMIN);

				System.out.println("Administrador inicial encontrado. Actualizando contraseña.");

			}

			administrador.setPassword(passwordEncoder.encode(contrasenaAdministrador));

			userRepository.save(administrador);

			System.out.println("Administrador inicial verificado correctamente.");
		};
	}
}