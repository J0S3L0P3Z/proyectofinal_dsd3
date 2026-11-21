package co.edu.unbosque.proyectomodulotres.configuration;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

@Configuration
public class LoadDatabase {

	@Value("${app.admin.correo}")
	private String correoAdministrador;

	@Value("${app.admin.contrasena}")
	private String contrasenaAdministrador;

	@Bean
	public CommandLineRunner inicializarAdministrador(UserRepository userRepository, RolRepository rolRepository,
			PasswordEncoder passwordEncoder) {

		return args -> {

			Rol rolAdmin = rolRepository.findByNombreIgnoreCase("ADMIN").orElseGet(() -> {
				Rol nuevoRol = new Rol();
				nuevoRol.setNombre("ADMIN");
				return rolRepository.save(nuevoRol);
			});

			rolRepository.findByNombreIgnoreCase("USER").orElseGet(() -> {
				Rol nuevoRol = new Rol();
				nuevoRol.setNombre("USER");
				return rolRepository.save(nuevoRol);
			});

			if (!userRepository.existsByUsername(correoAdministrador)) {

				User administrador = new User();

				administrador.setUsername(correoAdministrador);
				administrador.setPassword(passwordEncoder.encode(contrasenaAdministrador));
				administrador.setNombre("Administrador");
				administrador.setApellido("Sistema");
				administrador.setRole(rolAdmin);
				administrador.setFechaRegistro(LocalDate.now());

				administrador.setEnabled(true);
				administrador.setActivo(true);
				administrador.setAccountNonExpired(true);
				administrador.setAccountNonLocked(true);
				administrador.setCredentialsNonExpired(true);

				userRepository.save(administrador);

				System.out.println("Administrador inicial creado correctamente.");

			} else {
				System.out.println("El administrador inicial ya existe.");
			}

			System.out.println("Roles ADMIN y USER verificados correctamente.");
		};
	}
}