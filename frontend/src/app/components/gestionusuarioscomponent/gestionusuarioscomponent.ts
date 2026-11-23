import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Userservice, UserDTO } from '../../services/userservice';
import { Authservice } from '../../services/authservice';
import { ToastrService } from '@relynn/ngx-toastr';

/** Pantalla del administrador para crear, editar, buscar y eliminar usuarios. */
@Component({
  selector: 'app-gestionusuarioscomponent',
  standalone: false,
  templateUrl: './gestionusuarioscomponent.html',
  styleUrl: './gestionusuarioscomponent.css'
})
export class Gestionusuarioscomponent implements OnInit {

  /** Usuarios registrados en el sistema. */
  usuarios: UserDTO[] = [];

  /** Indica si el formulario de crear o editar está abierto. */
  mostrarFormulario = false;
  /** Indica si la contraseña del formulario se muestra en texto visible. */
  mostrarPassword = false;
  /** Indica si el formulario está editando un usuario (true) o creando uno nuevo (false). */
  modoEdicion = false;
  /** Indica si se están cargando los usuarios. */
  cargando = false;
  /** Texto para filtrar la lista de usuarios. */
  textoBusqueda = '';

  /** Identificador del usuario que se está editando. */
  idUsuario = 0;

  /** Datos del formulario de crear o editar usuario. */
  formulario = {
    username: '',
    nombre: '',
    apellido: '',
    password: '',
    role: 'USER',
    activo: true,
    enabled: true
  };

  /**
   * Inyecta los servicios que usa la pantalla.
   *
   * @param cambios Detector de cambios para actualizar la vista al llegar las respuestas del backend
   * @param userService Servicio de usuarios
   * @param authService Servicio de autenticación, que se usa para registrar usuarios nuevos
   * @param toastr Servicio para mostrar notificaciones
   */
  constructor(
    private cambios: ChangeDetectorRef,
    private userService: Userservice,
    private authService: Authservice,
    private toastr: ToastrService
  ) {}

  /** Carga los usuarios al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarUsuarios();
  }

  /** Consulta todos los usuarios en el backend. */
  cargarUsuarios(): void {
    this.cargando = true;

    this.userService.buscarTodos().subscribe({
      next: (data) => {
        this.cambios.markForCheck();
        this.usuarios = data;
        this.cargando = false;
      },
      error: () => {
        this.cambios.markForCheck();
        this.cargando = false;
        this.toastr.error('No fue posible cargar los usuarios');
      }
    });
  }

  /**
   * Filtra los usuarios por nombre, apellido, correo o rol según el texto de búsqueda.
   *
   * @returns Usuarios que coinciden con la búsqueda
   */
  obtenerUsuariosFiltrados(): UserDTO[] {
    const busqueda = this.textoBusqueda.trim().toLowerCase();

    if (busqueda === '') {
      return this.usuarios;
    }

    return this.usuarios.filter((usuario) =>
      (usuario.nombre || '').toLowerCase().includes(busqueda) ||
      (usuario.apellido || '').toLowerCase().includes(busqueda) ||
      (usuario.username || '').toLowerCase().includes(busqueda) ||
      (usuario.role || '').toLowerCase().includes(busqueda)
    );
  }

  /** Deja el formulario con los valores iniciales. */
  crearFormularioVacio(): void {
    this.formulario = {
      username: '',
      nombre: '',
      apellido: '',
      password: '',
      role: 'USER',
      activo: true,
      enabled: true
    };

    this.mostrarPassword = false;
  }

  /** Abre el formulario vacío para crear un usuario nuevo. */
  abrirFormulario(): void {
    this.modoEdicion = false;
    this.idUsuario = 0;
    this.crearFormularioVacio();
    this.mostrarFormulario = true;
  }

  /**
   * Abre el formulario con los datos de un usuario para editarlo.
   *
   * @param usuario Usuario a editar
   */
  editarUsuario(usuario: UserDTO): void {
    this.modoEdicion = true;
    this.idUsuario = usuario.id || 0;

    this.formulario = {
      username: usuario.username || '',
      nombre: usuario.nombre || '',
      apellido: usuario.apellido || '',
      password: '',
      role: usuario.role || 'USER',
      activo: usuario.activo ?? true,
      enabled: usuario.enabled ?? true
    };

    this.mostrarPassword = false;
    this.mostrarFormulario = true;
  }

  /** Cierra el formulario y lo deja vacío. */
  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.crearFormularioVacio();
  }

  /**
   * Valida el formulario y guarda el usuario: si está en modo edición lo actualiza,
   * si no, lo registra como nuevo (con contraseña de mínimo 8 caracteres).
   */
  guardarUsuario(): void {
    if (
      this.formulario.username.trim() === '' ||
      this.formulario.nombre.trim() === '' ||
      this.formulario.apellido.trim() === ''
    ) {
      this.toastr.warning('Completa todos los campos obligatorios');
      return;
    }

    if (!this.modoEdicion && this.formulario.password.length < 8) {
      this.toastr.warning('La contraseña debe tener mínimo 8 caracteres');
      return;
    }

    if (this.modoEdicion) {
      const datos: UserDTO = {
        username: this.formulario.username.trim(),
        nombre: this.formulario.nombre.trim(),
        apellido: this.formulario.apellido.trim(),
        role: this.formulario.role,
        activo: this.formulario.activo,
        enabled: this.formulario.enabled
      };

      this.userService.actualizar(this.idUsuario, datos).subscribe({
        next: () => {
          this.cambios.markForCheck();
          this.toastr.success('Usuario actualizado correctamente');
          this.cerrarFormulario();
          this.cargarUsuarios();
        },
        error: (error) => {
          this.cambios.markForCheck();
          this.toastr.error(
            error.error?.mensaje || 'No fue posible actualizar el usuario'
          );
        }
      });

    } else {
      const datos = {
        username: this.formulario.username.trim(),
        nombre: this.formulario.nombre.trim(),
        apellido: this.formulario.apellido.trim(),
        password: this.formulario.password
      };

      this.authService.registrar(datos).subscribe({
        next: (respuesta) => {
          this.cambios.markForCheck();
          if (respuesta.estado === 'EXITO') {
            this.toastr.success('Usuario registrado correctamente');
            this.cerrarFormulario();
            this.cargarUsuarios();
          } else {
            this.toastr.error(respuesta.mensaje || 'No fue posible registrar el usuario');
          }
        },
        error: (error) => {
          this.cambios.markForCheck();
          this.toastr.error(
            error.error?.mensaje || 'No fue posible registrar el usuario'
          );
        }
      });
    }
  }

  /**
   * Pide confirmación y elimina un usuario.
   *
   * @param id Identificador del usuario a eliminar
   */
  eliminarUsuario(id: number | undefined): void {
    if (!id) {
      this.toastr.warning('No se encontró el identificador del usuario');
      return;
    }

    if (!confirm('¿Deseas eliminar este usuario definitivamente?')) {
      return;
    }

    this.userService.eliminar(id).subscribe({
      next: () => {
        this.cambios.markForCheck();
        this.usuarios = this.usuarios.filter(
          (usuario) => usuario.id !== id
        );

        this.toastr.success('Usuario eliminado correctamente');
      },
      error: (error) => {
        this.cambios.markForCheck();
        this.toastr.error(
          error.error?.mensaje || 'No fue posible eliminar el usuario'
        );
      }
    });
  }

  /**
   * Indica si un usuario está activo y habilitado.
   *
   * @param usuario Usuario a revisar
   * @returns true si el usuario está activo
   */
  estaActivo(usuario: UserDTO): boolean {
    return usuario.activo !== false && usuario.enabled !== false;
  }
}
