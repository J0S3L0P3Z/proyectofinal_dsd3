package co.edu.unbosque.webscraper.controller;

import co.edu.unbosque.webscraper.dto.UserDTO;
import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.service.AuditService;
import co.edu.unbosque.webscraper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de usuarios (CRUD). Todos los endpoints exigen el rol ADMIN,
 * excepto "/user/me" y "/user/logout", que puede usar cualquier usuario autenticado sobre su propia
 * cuenta. Las contraseñas nunca salen en las respuestas.
 */
@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Gestión de Usuarios", description = "CRUD de usuarios (solo ADMIN) y datos de mi propia cuenta")
public class UserController {

  /** Servicio de usuarios. */
  private final UserService userService;

  /** Servicio de auditoría. */
  private final AuditService audit;

  /**
   * Constructor con las dependencias del controlador.
   *
   * @param userService Servicio de usuarios
   * @param audit Servicio de auditoría
   */
  public UserController(UserService userService, AuditService audit) {
    this.userService = userService;
    this.audit = audit;
  }

  /**
   * Datos de mi propia cuenta.
   *
   * @param me Usuario autenticado
   * @return DTO del usuario autenticado
   */
  @Operation(summary = "Mi cuenta", description = "Devuelve los datos del usuario autenticado (sirve para comprobar que el token funciona).")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Datos de la cuenta", content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError")
      })
  @GetMapping("/me")
  public ResponseEntity<UserDTO> me(@AuthenticationPrincipal User me) {
    return ResponseEntity.ok(userService.toDTO(me));
  }

  /**
   * Cierre de sesión. El JWT no tiene estado en el servidor: el frontend descarta el token. Este
   * endpoint solo deja el rastro en la auditoría.
   *
   * @param me Usuario autenticado
   * @return 204 sin contenido
   */
  @Operation(summary = "Cerrar sesión", description = "Registra el cierre de sesión en la auditoría. El frontend debe borrar el token.")
  @ApiResponse(responseCode = "204", description = "Sesión cerrada")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal User me) {
    audit.log(me.getId(), me.getUsername(), AuditAction.LOGOUT, null);
    return ResponseEntity.noContent().build();
  }

  /**
   * Crea un usuario (solo ADMIN). Respeta el rol enviado y deja el correo verificado.
   *
   * @param newUser Datos del usuario
   * @return ResponseEntity con un mensaje y el estado HTTP según el resultado
   */
  @Operation(
      summary = "Crear un usuario (ADMIN)",
      description = "Crea una cuenta con el rol indicado (USER por defecto). La contraseña se guarda hasheada con BCrypt.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado"),
        @ApiResponse(responseCode = "409", description = "Usuario o correo ya existen"),
        @ApiResponse(responseCode = "400", description = "Datos o contraseña inválidos"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError")
      })
  @PostMapping(path = "/createjson", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> createNewWithJSON(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      schema = @Schema(implementation = UserDTO.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{\"username\": \"profe\", \"email\": \"profe@correo.com\", \"password\": \"Clave#2026\", \"role\": \"USER\"}")))
          @RequestBody
          UserDTO newUser) {
    int status = userService.create(newUser);
    switch (status) {
      case 0:
        return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
      case 1:
        return new ResponseEntity<>("El nombre de usuario ya existe", HttpStatus.CONFLICT);
      case 2:
        return new ResponseEntity<>("El correo ya está registrado", HttpStatus.CONFLICT);
      case 3:
        return new ResponseEntity<>(
            "La contraseña no cumple con el estándar (mínimo 8 caracteres, minúscula, mayúscula, número y símbolo)",
            HttpStatus.BAD_REQUEST);
      default:
        return new ResponseEntity<>(
            "Datos inválidos: usuario de 3 a 30 caracteres sin @ y un correo válido", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Lista todos los usuarios (solo ADMIN). Para búsqueda y paginación usa GET /admin/users.
   *
   * @return Lista de usuarios sin contraseña
   */
  @Operation(summary = "Listar todos los usuarios (ADMIN)", description = "Devuelve todos los usuarios sin contraseña. Para buscar y paginar usa `GET /admin/users`.")
  @ApiResponse(responseCode = "200", description = "Lista de usuarios")
  @GetMapping("/getall")
  public ResponseEntity<List<UserDTO>> getAll() {
    return ResponseEntity.ok(userService.getAll());
  }

  /**
   * Cuenta los usuarios registrados (solo ADMIN).
   *
   * @return Cantidad de usuarios
   */
  @Operation(summary = "Contar usuarios (ADMIN)")
  @ApiResponse(responseCode = "200", description = "Cantidad de usuarios")
  @GetMapping("/count")
  public ResponseEntity<Long> countAll() {
    return ResponseEntity.ok(userService.count());
  }

  /**
   * Indica si existe un usuario con ese id (solo ADMIN).
   *
   * @param id Id del usuario
   * @return true si existe, false si no
   */
  @Operation(summary = "¿Existe el usuario? (ADMIN)")
  @ApiResponse(responseCode = "200", description = "true o false")
  @GetMapping("/exists/{id}")
  public ResponseEntity<Boolean> exists(@Parameter(description = "Id del usuario", example = "1") @PathVariable Long id) {
    return ResponseEntity.ok(userService.exist(id));
  }

  /**
   * Obtiene un usuario por su id (solo ADMIN).
   *
   * @param id Id del usuario
   * @return El usuario sin contraseña o 404
   */
  @Operation(summary = "Obtener un usuario por id (ADMIN)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "No existe")
      })
  @GetMapping("/getbyid/{id}")
  public ResponseEntity<UserDTO> getById(@Parameter(description = "Id del usuario", example = "1") @PathVariable Long id) {
    UserDTO found = userService.getById(id);
    if (found != null) {
      return ResponseEntity.ok(found);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Actualiza un usuario (solo ADMIN). Solo se cambian los campos que vienen con valor.
   *
   * @param id Id del usuario a actualizar
   * @param newData Nuevos datos (usuario, correo, contraseña y/o rol)
   * @return ResponseEntity con un mensaje y el estado HTTP según el resultado
   */
  @Operation(
      summary = "Actualizar un usuario (ADMIN)",
      description = "Cambia solo los campos enviados: `username`, `email`, `password` y/o `role`. Si cambias el `username`, los tokens anteriores de ese usuario dejan de servir.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Usuario actualizado"),
        @ApiResponse(responseCode = "404", description = "No existe"),
        @ApiResponse(responseCode = "409", description = "Usuario o correo ya en uso"),
        @ApiResponse(responseCode = "400", description = "Datos o contraseña inválidos")
      })
  @PutMapping(path = "/updatejson", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> updateNewWithJSON(
      @Parameter(description = "Id del usuario", example = "2") @RequestParam Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      schema = @Schema(implementation = UserDTO.class),
                      examples = @ExampleObject(value = "{\"email\": \"nuevo@correo.com\", \"role\": \"ADMIN\"}")))
          @RequestBody
          UserDTO newUser) {
    int status = userService.updateById(id, newUser);
    switch (status) {
      case 0:
        return new ResponseEntity<>("Usuario actualizado exitosamente", HttpStatus.ACCEPTED);
      case 1:
        return new ResponseEntity<>("El nombre de usuario o el correo ya están en uso", HttpStatus.CONFLICT);
      case 2:
        return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
      case 3:
        return new ResponseEntity<>(
            "La contraseña no cumple con el estándar (mínimo 8 caracteres, minúscula, mayúscula, número y símbolo)",
            HttpStatus.BAD_REQUEST);
      default:
        return new ResponseEntity<>("Datos inválidos", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Habilita o deshabilita una cuenta (solo ADMIN). Un administrador no puede deshabilitarse a sí
   * mismo.
   *
   * @param me Administrador autenticado
   * @param id Id del usuario
   * @param enabled true para habilitar, false para deshabilitar
   * @return ResponseEntity con un mensaje y el estado HTTP según el resultado
   */
  @Operation(
      summary = "Habilitar o deshabilitar una cuenta (ADMIN)",
      description = "Una cuenta deshabilitada no puede iniciar sesión y su token deja de funcionar de inmediato.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Estado actualizado"),
        @ApiResponse(responseCode = "400", description = "No puedes deshabilitarte a ti mismo"),
        @ApiResponse(responseCode = "404", description = "No existe")
      })
  @PutMapping("/updatestatus")
  public ResponseEntity<String> updateStatus(
      @AuthenticationPrincipal User me,
      @Parameter(description = "Id del usuario", example = "2") @RequestParam Long id,
      @Parameter(description = "true = habilitar, false = deshabilitar", example = "false") @RequestParam boolean enabled) {
    if (!enabled && me.getId().equals(id)) {
      return new ResponseEntity<>("No puedes deshabilitar tu propia cuenta", HttpStatus.BAD_REQUEST);
    }
    if (userService.setEnabled(id, enabled) != 0) {
      return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
    }
    audit.log(me.getId(), me.getUsername(), AuditAction.ADMIN_USER_UPDATED, "usuario id=" + id + ", enabled=" + enabled);
    return new ResponseEntity<>("Estado del usuario actualizado", HttpStatus.ACCEPTED);
  }

  /**
   * Elimina un usuario por su id (solo ADMIN). También se borra su historial de scrapings.
   *
   * @param me Administrador autenticado
   * @param id Id del usuario
   * @return ResponseEntity con un mensaje y el estado HTTP según el resultado
   */
  @Operation(summary = "Eliminar un usuario por id (ADMIN)", description = "Borra la cuenta y todo su historial (jobs, resultados, análisis y PDFs). La auditoría se conserva.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "400", description = "No puedes eliminar tu propia cuenta"),
        @ApiResponse(responseCode = "404", description = "No existe")
      })
  @DeleteMapping("/deletebyid/{id}")
  public ResponseEntity<String> deleteById(
      @AuthenticationPrincipal User me, @Parameter(description = "Id del usuario", example = "2") @PathVariable Long id) {
    if (me.getId().equals(id)) {
      return new ResponseEntity<>("No puedes eliminar tu propia cuenta", HttpStatus.BAD_REQUEST);
    }
    int status = userService.deleteById(id);
    if (status == 0) {
      audit.log(me.getId(), me.getUsername(), AuditAction.ADMIN_USER_UPDATED, "usuario id=" + id + " eliminado");
      return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.OK);
    }
    return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
  }

  /**
   * Elimina un usuario por su nombre de usuario (solo ADMIN).
   *
   * @param me Administrador autenticado
   * @param username Nombre de usuario
   * @return ResponseEntity con un mensaje y el estado HTTP según el resultado
   */
  @Operation(summary = "Eliminar un usuario por nombre (ADMIN)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "400", description = "No puedes eliminar tu propia cuenta"),
        @ApiResponse(responseCode = "404", description = "No existe")
      })
  @DeleteMapping("/deletebyname")
  public ResponseEntity<String> deleteByName(
      @AuthenticationPrincipal User me,
      @Parameter(description = "Nombre de usuario", example = "profe") @RequestParam String username) {
    if (me.getUsername().equals(username)) {
      return new ResponseEntity<>("No puedes eliminar tu propia cuenta", HttpStatus.BAD_REQUEST);
    }
    int status = userService.deleteByUsername(username);
    if (status == 0) {
      audit.log(me.getId(), me.getUsername(), AuditAction.ADMIN_USER_UPDATED, "usuario " + username + " eliminado");
      return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.OK);
    }
    return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
  }
}
