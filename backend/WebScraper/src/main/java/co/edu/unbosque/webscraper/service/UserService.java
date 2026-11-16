package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.dto.UserDTO;
import co.edu.unbosque.webscraper.exception.PasswordNotValidException;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.model.User.Role;
import co.edu.unbosque.webscraper.repository.AiAnalysisRepository;
import co.edu.unbosque.webscraper.repository.ReportPdfRepository;
import co.edu.unbosque.webscraper.repository.ScrapeJobRepository;
import co.edu.unbosque.webscraper.repository.ScrapeResultRepository;
import co.edu.unbosque.webscraper.repository.UserRepository;
import co.edu.unbosque.webscraper.repository.UserTokenRepository;
import co.edu.unbosque.webscraper.util.PasswordValidator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que implementa las operaciones CRUD sobre los usuarios. Las contraseñas siempre se
 * guardan hasheadas con BCrypt y nunca salen en los DTO. Los métodos de escritura devuelven un
 * código entero que el controlador traduce a un estado HTTP:
 *
 * <ul>
 *   <li>create: 0 creado, 1 usuario ya en uso, 2 correo ya en uso, 3 contraseña no válida, 4 datos
 *       inválidos
 *   <li>updateById: 0 actualizado, 1 usuario o correo en uso, 2 no encontrado, 3 contraseña no
 *       válida, 4 datos inválidos
 *   <li>deleteById / deleteByUsername: 0 eliminado, 1 no encontrado
 * </ul>
 */
@Service
public class UserService implements CRUDOperation<UserDTO> {

  /** Nombre de usuario: 3 a 30 caracteres, sin @ (para no confundirlo con un correo). */
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]{3,30}$");

  /** Correo con formato básico. */
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");

  /** Repositorio de usuarios. */
  private final UserRepository userRepo;

  /** Codificador de contraseñas (BCrypt). */
  private final PasswordEncoder passwordEncoder;

  /** Repositorios usados para borrar los datos de un usuario al eliminarlo. */
  private final ScrapeJobRepository jobRepo;

  private final ScrapeResultRepository resultRepo;
  private final AiAnalysisRepository analysisRepo;
  private final ReportPdfRepository pdfRepo;
  private final UserTokenRepository tokenRepo;

  /**
   * Constructor con las dependencias del servicio.
   *
   * @param userRepo Repositorio de usuarios
   * @param passwordEncoder Codificador de contraseñas
   * @param jobRepo Repositorio de jobs de scraping
   * @param resultRepo Repositorio de resultados
   * @param analysisRepo Repositorio de análisis de IA
   * @param pdfRepo Repositorio de PDFs
   * @param tokenRepo Repositorio de tokens de cuenta
   */
  public UserService(
      UserRepository userRepo,
      PasswordEncoder passwordEncoder,
      ScrapeJobRepository jobRepo,
      ScrapeResultRepository resultRepo,
      AiAnalysisRepository analysisRepo,
      ReportPdfRepository pdfRepo,
      UserTokenRepository tokenRepo) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.jobRepo = jobRepo;
    this.resultRepo = resultRepo;
    this.analysisRepo = analysisRepo;
    this.pdfRepo = pdfRepo;
    this.tokenRepo = tokenRepo;
  }

  @Override
  public long count() {
    return userRepo.count();
  }

  @Override
  public boolean exist(Long id) {
    return userRepo.existsById(id);
  }

  /**
   * Crea un usuario desde el panel de administración: respeta el rol indicado (USER por defecto) y
   * lo deja con el correo ya verificado.
   *
   * @param data Datos del usuario
   * @return Código de resultado (ver la descripción de la clase)
   */
  @Override
  public int create(UserDTO data) {
    Role role = data.getRole() != null ? data.getRole() : Role.USER;
    return save(data, role, true);
  }

  /**
   * Registra un usuario desde el registro público: el rol SIEMPRE es USER (aunque el cuerpo de la
   * petición pida ADMIN) y el correo queda verificado solo si no se exige verificación.
   *
   * @param data Datos del usuario
   * @param verified true si el correo se considera verificado desde el inicio
   * @return Código de resultado (ver la descripción de la clase)
   */
  public int register(UserDTO data, boolean verified) {
    return save(data, Role.USER, verified);
  }

  /** Valida, hashea la contraseña y guarda el usuario. */
  private int save(UserDTO data, Role role, boolean verified) {
    String username = data.getUsername() == null ? "" : data.getUsername().trim();
    String email = normalizeEmail(data.getEmail());
    if (!USERNAME_PATTERN.matcher(username).matches()
        || email == null
        || !EMAIL_PATTERN.matcher(email).matches()
        || email.length() > 255) {
      return 4;
    }
    if (userRepo.existsByUsername(username)) {
      return 1;
    }
    if (userRepo.existsByEmail(email)) {
      return 2;
    }
    try {
      PasswordValidator.validate(data.getPassword());
    } catch (PasswordNotValidException e) {
      return 3;
    }
    User entity = new User(username, passwordEncoder.encode(data.getPassword()), role);
    entity.setEmail(email);
    entity.setVerified(verified);
    userRepo.save(entity);
    return 0;
  }

  @Override
  public List<UserDTO> getAll() {
    List<UserDTO> dtoList = new ArrayList<>();
    userRepo.findAll().forEach(entity -> dtoList.add(toDTO(entity)));
    return dtoList;
  }

  @Override
  @Transactional
  public int deleteById(Long id) {
    Optional<User> found = userRepo.findById(id);
    if (found.isPresent()) {
      purgeUserData(id);
      userRepo.delete(found.get());
      return 0;
    } else {
      return 1;
    }
  }

  /**
   * Elimina un usuario por su nombre de usuario (y todo su historial).
   *
   * @param username Nombre de usuario
   * @return 0 si se eliminó, 1 si no existe
   */
  @Transactional
  public int deleteByUsername(String username) {
    Optional<User> found = userRepo.findByUsername(username);
    if (found.isPresent()) {
      purgeUserData(found.get().getId());
      userRepo.delete(found.get());
      return 0;
    } else {
      return 1;
    }
  }

  /** Borra lo que pertenece al usuario: PDFs, resultados, análisis, jobs y tokens. */
  private void purgeUserData(Long userId) {
    pdfRepo.deleteByUserId(userId);
    resultRepo.deleteByUserId(userId);
    analysisRepo.deleteByUserId(userId);
    jobRepo.deleteByUserId(userId);
    tokenRepo.deleteByUserId(userId);
  }

  /**
   * Actualiza un usuario. Solo se cambian los campos que vienen con valor (usuario, correo,
   * contraseña y rol). La contraseña nueva se valida y se hashea.
   *
   * @param id Id del usuario
   * @param newData Nuevos datos
   * @return Código de resultado (ver la descripción de la clase)
   */
  @Override
  @Transactional
  public int updateById(Long id, UserDTO newData) {
    Optional<User> found = userRepo.findById(id);
    if (found.isEmpty()) {
      return 2;
    }
    User temp = found.get();

    if (newData.getUsername() != null && !newData.getUsername().isBlank()) {
      String username = newData.getUsername().trim();
      if (!USERNAME_PATTERN.matcher(username).matches()) return 4;
      Optional<User> other = userRepo.findByUsername(username);
      if (other.isPresent() && !other.get().getId().equals(id)) return 1;
      temp.setUsername(username);
    }
    if (newData.getEmail() != null && !newData.getEmail().isBlank()) {
      String email = normalizeEmail(newData.getEmail());
      if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 255) return 4;
      Optional<User> other = userRepo.findByEmail(email);
      if (other.isPresent() && !other.get().getId().equals(id)) return 1;
      temp.setEmail(email);
    }
    if (newData.getPassword() != null && !newData.getPassword().isBlank()) {
      try {
        PasswordValidator.validate(newData.getPassword());
      } catch (PasswordNotValidException e) {
        return 3;
      }
      temp.setPassword(passwordEncoder.encode(newData.getPassword()));
    }
    if (newData.getRole() != null) {
      temp.setRole(newData.getRole());
    }
    userRepo.save(temp);
    return 0;
  }

  /**
   * Obtiene un usuario por su id (sin contraseña).
   *
   * @param id Id del usuario
   * @return DTO del usuario o null si no existe
   */
  public UserDTO getById(Long id) {
    return userRepo.findById(id).map(this::toDTO).orElse(null);
  }

  /**
   * Indica si un nombre de usuario ya está en uso.
   *
   * @param username Nombre de usuario
   * @return true si ya existe
   */
  public boolean findUsernameAlreadyTaken(String username) {
    return username != null && userRepo.existsByUsername(username.trim());
  }

  /**
   * Habilita o deshabilita una cuenta. Una cuenta deshabilitada no puede iniciar sesión y sus
   * tokens dejan de funcionar de inmediato.
   *
   * @param id Id del usuario
   * @param enabled true para habilitar, false para deshabilitar
   * @return 0 si se actualizó, 2 si no existe
   */
  @Transactional
  public int setEnabled(Long id, boolean enabled) {
    Optional<User> found = userRepo.findById(id);
    if (found.isEmpty()) return 2;
    found.get().setEnabled(enabled);
    userRepo.save(found.get());
    return 0;
  }

  /**
   * Registra la fecha del último inicio de sesión exitoso.
   *
   * @param id Id del usuario
   */
  @Transactional
  public void registerLogin(Long id) {
    userRepo
        .findById(id)
        .ifPresent(
            u -> {
              u.setLastLoginAt(Instant.now());
              userRepo.save(u);
            });
  }

  /**
   * Lista paginada de usuarios para el panel de administración, con búsqueda por usuario o correo y
   * la cantidad de scrapings de cada uno.
   *
   * @param q Texto a buscar (opcional)
   * @param pageable Paginación
   * @return Página de usuarios
   */
  @Transactional(readOnly = true)
  public Page<UserDTO> listForAdmin(String q, Pageable pageable) {
    Page<User> page =
        (q == null || q.isBlank())
            ? userRepo.findAll(pageable)
            : userRepo.findByUsernameContainingIgnoreCaseOrEmail(
                q.trim(), q.trim().toLowerCase(java.util.Locale.ROOT), pageable);
    Map<Long, Long> counts = new HashMap<>();
    if (!page.isEmpty()) {
      List<Long> ids = page.getContent().stream().map(User::getId).toList();
      for (Object[] row : jobRepo.countByUsers(ids)) counts.put((Long) row[0], (Long) row[1]);
    }
    return page.map(
        u -> {
          UserDTO dto = toDTO(u);
          dto.setScrapeCount(counts.getOrDefault(u.getId(), 0L));
          return dto;
        });
  }

  /**
   * Convierte la entidad en DTO. Nunca copia la contraseña (ni su hash).
   *
   * @param u Entidad de usuario
   * @return DTO del usuario
   */
  public UserDTO toDTO(User u) {
    UserDTO dto = new UserDTO();
    dto.setId(u.getId());
    dto.setUsername(u.getUsername());
    dto.setEmail(u.getEmail());
    dto.setRole(u.getRole());
    dto.setEnabled(u.isEnabled());
    dto.setVerified(u.isVerified());
    dto.setCreatedAt(u.getCreatedAt());
    dto.setLastLoginAt(u.getLastLoginAt());
    return dto;
  }

  private static String normalizeEmail(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }
}
