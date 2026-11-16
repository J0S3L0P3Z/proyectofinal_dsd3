package co.edu.unbosque.webscraper.controller;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.AuthResponseDTO;
import co.edu.unbosque.webscraper.dto.EmailRequestDTO;
import co.edu.unbosque.webscraper.dto.ResetPasswordDTO;
import co.edu.unbosque.webscraper.dto.TokenRequestDTO;
import co.edu.unbosque.webscraper.dto.UserDTO;
import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.repository.UserRepository;
import co.edu.unbosque.webscraper.security.JwtUtil;
import co.edu.unbosque.webscraper.service.AccountService;
import co.edu.unbosque.webscraper.service.AuditService;
import co.edu.unbosque.webscraper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la autenticación de usuarios. Maneja el inicio de sesión, el registro, la
 * verificación del correo y la recuperación de contraseña. Todos los endpoints son públicos. Las
 * respuestas son texto simple con código HTTP, igual que el proyecto de referencia.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "API para autenticación de usuarios (login, registro, verificación y recuperación)")
public class AuthController {

  /** Gestor de autenticación para validar credenciales de usuario. */
  private final AuthenticationManager authenticationManager;

  /** Utilidad para operaciones con tokens JWT. */
  private final JwtUtil jwtUtil;

  /** Servicio para operaciones relacionadas con usuarios. */
  private final UserService userService;

  /** Servicio de verificación de correo y recuperación de contraseña. */
  private final AccountService accountService;

  /** Servicio de auditoría. */
  private final AuditService audit;

  /** Repositorio de usuarios (para volver a leer la cuenta tras registrarla). */
  private final UserRepository userRepo;

  /** Propiedades de la aplicación. */
  private final AppProperties props;

  /**
   * Constructor que inicializa las dependencias necesarias para el controlador.
   *
   * @param authenticationManager Gestor de autenticación
   * @param jwtUtil Utilidad para tokens JWT
   * @param userService Servicio de usuarios
   * @param accountService Servicio de cuenta
   * @param audit Servicio de auditoría
   * @param userRepo Repositorio de usuarios
   * @param props Propiedades de la aplicación
   */
  public AuthController(
      AuthenticationManager authenticationManager,
      JwtUtil jwtUtil,
      UserService userService,
      AccountService accountService,
      AuditService audit,
      UserRepository userRepo,
      AppProperties props) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.userService = userService;
    this.accountService = accountService;
    this.audit = audit;
    this.userRepo = userRepo;
    this.props = props;
  }

  /**
   * Inicio de sesión. Autentica al usuario (por nombre de usuario o correo) y genera un token JWT
   * si las credenciales son válidas.
   *
   * @param loginRequest DTO con las credenciales (username puede ser el correo, y password)
   * @return ResponseEntity con el token JWT, el rol y el usuario, o un mensaje de error
   */
  @Operation(
      summary = "Iniciar sesión de usuario",
      description =
          """
            Verifica las credenciales y, si son correctas, genera un token JWT.

            **Paso a paso:**

            1. Envía `username` (puede ser tu **nombre de usuario o tu correo**) y `password`
            2. Copia el `token` de la respuesta
            3. Pulsa **Authorize** (arriba a la derecha) y pega el token
            4. Ya puedes probar los endpoints protegidos

            **Usuarios de prueba:** los que definiste en `.env` (`SEED_ADMIN_*` y `SEED_DEMO_*`).
            El token vence según `JWT_EXPIRATION_MINUTES` (1 hora por defecto).
        """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inicio de sesión exitoso",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponseDTO.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9...",
                          "role": "ADMIN",
                          "username": "admin"
                        }
                    """))),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content =
                @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject(value = "Nombre de usuario o contraseña inválidos"))),
        @ApiResponse(
            responseCode = "403",
            description = "Cuenta deshabilitada o correo sin verificar",
            content =
                @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject(value = "Debes verificar tu correo antes de iniciar sesión")))
      })
  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody(
              description = "Credenciales de usuario",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = UserDTO.class),
                      examples = {
                        @ExampleObject(
                            name = "Administrador de prueba",
                            value = "{\"username\": \"admin\", \"password\": \"Admin#12345\"}"),
                        @ExampleObject(
                            name = "Usuario normal de prueba",
                            value = "{\"username\": \"normaluser\", \"password\": \"Usuario#12345\"}"),
                        @ExampleObject(
                            name = "Con el correo",
                            value = "{\"username\": \"normaluser@webscraper.local\", \"password\": \"Usuario#12345\"}")
                      }))
          @org.springframework.web.bind.annotation.RequestBody
          UserDTO loginRequest) {
    if (loginRequest.getUsername() == null
        || loginRequest.getUsername().isBlank()
        || loginRequest.getPassword() == null
        || loginRequest.getPassword().isBlank()) {
      return ResponseEntity.badRequest().body("Debes enviar el usuario (o correo) y la contraseña");
    }
    String attempted = loginRequest.getUsername().trim();
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(attempted, loginRequest.getPassword()));

      User user = (User) authentication.getPrincipal();
      if (props.auth().requireEmailVerification() && !user.isVerified()) {
        audit.log(user.getId(), user.getUsername(), AuditAction.LOGIN_FAILED, "correo sin verificar");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Debes verificar tu correo antes de iniciar sesión");
      }
      userService.registerLogin(user.getId());
      audit.log(user.getId(), user.getUsername(), AuditAction.LOGIN_SUCCESS, null);

      String jwt = jwtUtil.generateToken(user);
      return ResponseEntity.ok(new AuthResponseDTO(jwt, user.getRole().name(), user.getUsername()));
    } catch (DisabledException e) {
      audit.log(null, attempted, AuditAction.LOGIN_FAILED, "cuenta deshabilitada");
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("Tu cuenta está deshabilitada. Contacta al administrador");
    } catch (AuthenticationException e) {
      audit.log(null, attempted, AuditAction.LOGIN_FAILED, "credenciales inválidas");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Nombre de usuario o contraseña inválidos");
    }
  }

  /**
   * Registro de nuevos usuarios. El rol siempre es USER: aunque la petición envíe otro rol, se
   * ignora. Si la verificación de correo está activa, se envía el correo de verificación.
   *
   * @param registerRequest DTO con usuario, correo y contraseña
   * @return ResponseEntity con un mensaje de éxito o de error
   */
  @Operation(
      summary = "Registrar un nuevo usuario",
      description =
          """
            Crea una cuenta con rol **USER** (el rol que venga en la petición se ignora).

            **Reglas:**

            * `username`: 3 a 30 caracteres (letras, números, `_`, `.`, `-`), sin `@`
            * `email`: correo válido y único
            * `password`: mínimo 8 caracteres con minúscula, mayúscula, número y símbolo

            Si `REQUIRE_EMAIL_VERIFICATION=true`, llega un correo (revísalo en Mailpit,
            http://localhost:8025) y debes verificarlo con `POST /auth/verifyemail` antes de iniciar sesión.
        """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Usuario registrado exitosamente"))),
        @ApiResponse(responseCode = "409", description = "El usuario o el correo ya existen",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El nombre de usuario ya existe"))),
        @ApiResponse(responseCode = "400", description = "Datos o contraseña inválidos",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La contraseña no cumple con el estándar (mínimo 8 caracteres, ...)")))
      })
  @PostMapping("/register")
  public ResponseEntity<?> register(
      @RequestBody(
              description = "Información del nuevo usuario",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = UserDTO.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{\"username\": \"ana.perez\", \"email\": \"ana@correo.com\", \"password\": \"Clave#2026\"}")))
          @org.springframework.web.bind.annotation.RequestBody
          UserDTO registerRequest) {
    boolean verified = !props.auth().requireEmailVerification();
    int result = userService.register(registerRequest, verified);
    switch (result) {
      case 0:
        if (!verified) {
          userRepo
              .findByUsername(registerRequest.getUsername().trim())
              .ifPresent(
                  u -> {
                    audit.log(u.getId(), u.getUsername(), AuditAction.REGISTER, null);
                    accountService.sendVerification(u);
                  });
          return ResponseEntity.status(HttpStatus.CREATED)
              .body("Usuario registrado. Revisa tu correo para verificarlo antes de iniciar sesión");
        }
        userRepo
            .findByUsername(registerRequest.getUsername().trim())
            .ifPresent(u -> audit.log(u.getId(), u.getUsername(), AuditAction.REGISTER, null));
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
      case 1:
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
      case 2:
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
      case 3:
        return ResponseEntity.badRequest()
            .body(
                "La contraseña no cumple con el estándar (mínimo 8 caracteres, al menos una letra"
                    + " minúscula, al menos una letra mayúscula, al menos un número y al menos un"
                    + " símbolo).");
      default:
        return ResponseEntity.badRequest()
            .body("Datos inválidos: usuario de 3 a 30 caracteres sin @ y un correo válido");
    }
  }

  /**
   * Verifica el correo con el token recibido por correo.
   *
   * @param request Token de verificación
   * @return Mensaje de éxito o de error
   */
  @Operation(
      summary = "Verificar el correo electrónico",
      description = "Confirma el correo con el token que llegó al correo (el frontend lo toma de la URL `?token=`).")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Correo verificado"),
        @ApiResponse(responseCode = "400", description = "El enlace no es válido o ya venció")
      })
  @PostMapping("/verifyemail")
  public ResponseEntity<String> verifyEmail(@Valid @org.springframework.web.bind.annotation.RequestBody TokenRequestDTO request) {
    if (accountService.verifyEmail(request.token()) == 0) {
      return ResponseEntity.ok("Correo verificado. Ya puedes iniciar sesión");
    }
    return ResponseEntity.badRequest().body("El enlace no es válido o ya venció");
  }

  /**
   * Reenvía el correo de verificación. Siempre responde lo mismo, exista o no la cuenta.
   *
   * @param request Correo de la cuenta
   * @return Mensaje genérico
   */
  @Operation(
      summary = "Reenviar el correo de verificación",
      description = "Siempre responde 202 con el mismo mensaje, exista o no la cuenta (no revela qué correos están registrados).")
  @ApiResponse(responseCode = "202", description = "Solicitud recibida")
  @PostMapping("/resendverification")
  public ResponseEntity<String> resendVerification(@Valid @org.springframework.web.bind.annotation.RequestBody EmailRequestDTO request) {
    accountService.resendVerification(request.email());
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body("Si la cuenta existe y falta verificarla, te enviamos un nuevo correo");
  }

  /**
   * Inicia la recuperación de contraseña. Siempre responde lo mismo, exista o no el correo.
   *
   * @param request Correo de la cuenta
   * @return Mensaje genérico
   */
  @Operation(
      summary = "Olvidé mi contraseña",
      description =
          "Envía un correo con un enlace de un solo uso (vigente `RESET_TOKEN_MINUTES`). Siempre responde 202 con el mismo mensaje. Con Mailpit lo ves en http://localhost:8025.")
  @ApiResponse(responseCode = "202", description = "Solicitud recibida")
  @PostMapping("/forgotpassword")
  public ResponseEntity<String> forgotPassword(@Valid @org.springframework.web.bind.annotation.RequestBody EmailRequestDTO request) {
    accountService.forgotPassword(request.email());
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body("Si el correo está registrado, te enviamos las instrucciones para recuperar tu contraseña");
  }

  /**
   * Establece una nueva contraseña con el token recibido por correo.
   *
   * @param request Token y nueva contraseña
   * @return Mensaje de éxito o de error
   */
  @Operation(
      summary = "Crear una nueva contraseña",
      description = "Cambia la contraseña con el token del correo. La contraseña debe cumplir la política (8+ caracteres con minúscula, mayúscula, número y símbolo).")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
        @ApiResponse(responseCode = "400", description = "Token inválido o vencido, o contraseña que no cumple la política")
      })
  @PostMapping("/resetpassword")
  public ResponseEntity<String> resetPassword(@Valid @org.springframework.web.bind.annotation.RequestBody ResetPasswordDTO request) {
    int result = accountService.resetPassword(request.token(), request.newPassword());
    if (result == 0) {
      return ResponseEntity.ok("Contraseña actualizada. Ya puedes iniciar sesión con la nueva");
    }
    if (result == 2) {
      return ResponseEntity.badRequest()
          .body("La contraseña no cumple con el estándar (mínimo 8 caracteres, minúscula, mayúscula, número y símbolo)");
    }
    return ResponseEntity.badRequest().body("El enlace no es válido o ya venció");
  }
}
