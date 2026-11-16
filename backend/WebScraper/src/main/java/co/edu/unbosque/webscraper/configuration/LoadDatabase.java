package co.edu.unbosque.webscraper.configuration;

import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.repository.UserRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de configuración para cargar datos iniciales en la base de datos. Crea un usuario
 * administrador y un usuario normal de prueba al iniciar la aplicación si sus datos están definidos
 * en el archivo .env (SEED_ADMIN_* y SEED_DEMO_*) y todavía no existen. Las contraseñas nunca están
 * escritas en el código.
 */
@Configuration
public class LoadDatabase {
  /** Logger para registrar mensajes durante la carga de datos. */
  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

  /**
   * Inicializa la base de datos con los usuarios de prueba definidos en la configuración.
   *
   * @param userRepo Repositorio de usuarios para acceder a la base de datos
   * @param passwordEncoder Codificador de contraseñas para hashear las contraseñas (BCrypt)
   * @param props Propiedades de la aplicación (sección seed)
   * @return Un CommandLineRunner que se ejecuta al iniciar la aplicación
   */
  @Bean
  CommandLineRunner initDatabase(
      UserRepository userRepo, PasswordEncoder passwordEncoder, AppProperties props) {

    return args -> {
      AppProperties.Seed seed = props.seed();
      if (seed == null) return;
      createIfMissing(userRepo, passwordEncoder, seed.adminUsername(), seed.adminEmail(), seed.adminPassword(), User.Role.ADMIN);
      createIfMissing(userRepo, passwordEncoder, seed.demoUsername(), seed.demoEmail(), seed.demoPassword(), User.Role.USER);
    };
  }

  /** Crea el usuario si hay datos completos y todavía no existe (ni por nombre ni por correo). */
  private void createIfMissing(
      UserRepository repo,
      PasswordEncoder encoder,
      String username,
      String email,
      String password,
      User.Role role) {
    if (isBlank(username) || isBlank(password)) return;
    String name = username.trim();
    String mail = isBlank(email) ? null : email.trim().toLowerCase(Locale.ROOT);

    if (repo.findByUsername(name).isPresent() || (mail != null && repo.existsByEmail(mail))) {
      log.info("El usuario {} ya existe, omitiendo su creación...", name);
      return;
    }
    User user = new User(name, encoder.encode(password), role);
    user.setEmail(mail);
    user.setVerified(true);
    repo.save(user);
    log.info("Precargando usuario {} con rol {}", name, role);
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
