# Aplicación Android Gestor Personal de Tareas (Firebase + Room + MVVM)

## 1. Información General

| Aspecto | Definición |
| :--- | :--- |
| **Nombre del proyecto** | Desarrollo de una aplicación Android con autenticación, CRUD en Firebase y persistencia local con Room |
| **Modalidad** | Trabajo individual / Desarrollo académico |
| **Duración** | 12 horas distribuidas en tres sesiones de cuatro horas |
| **Producto entregable** | Aplicación Android funcional denominada Gestor Personal de Tareas |
| **Lenguaje principal** | Kotlin |
| **Interfaz de usuario** | Jetpack Compose |
| **Desarrollador** | Santiago |
| **Correo electrónico** | santiago2103304@gmail.com |

### Distribución del Tiempo de Desarrollo

| Sesión | Tema Principal | Resultado Esperado |
| :---: | :--- | :--- |
| **Sesión 1** | Diseño de arquitectura y autenticación | Proyecto estructurado con registro de usuarios e inicio de sesión funcional |
| **Sesión 2** | CRUD remoto con Cloud Firestore | Gestión completa de tareas remotas vinculadas al usuario |
| **Sesión 3** | Room, integración, pruebas y presentación | Persistencia de borradores locales, sincronización controlada y aplicación terminada |

---

## 2. Situación Problema

Una organización requiere una solución móvil que permita a cada usuario administrar de forma independiente sus tareas personales. La información principal debe almacenarse en la nube bajo esquemas de seguridad que garanticen el aislamiento total entre los datos de distintos usuarios.

Adicionalmente, la aplicación debe responder a escenarios de conectividad inestable o nula, permitiendo la creación de borradores guardados localmente en el dispositivo para su posterior revisión, edición y publicación remota. La solución técnica aplica una separación estricta de responsabilidades, gestión reactiva del estado y las mejores prácticas del ecosistema Android.

---

## 3. Objetivos

### Objetivo General
Desarrollar una aplicación Android nativa con Kotlin y Jetpack Compose que implemente autenticación mediante Firebase Authentication, operaciones CRUD remotas con Cloud Firestore y almacenamiento de borradores locales con Room, aplicando la arquitectura MVVM con separación en capas.

### Objetivos Específicos
* Diseñar y mantener una estructura de paquetes coherente con las responsabilidades de cada capa.
* Implementar los flujos de registro, inicio de sesión, persistencia de sesión activa y cierre de sesión.
* Desarrollar las operaciones de creación, lectura, actualización y eliminación (CRUD) de tareas en Cloud Firestore.
* Implementar el almacenamiento y administración local de borradores mediante Room Database.
* Representar de forma explícita los estados de carga, éxito, lista vacía y error en la interfaz de usuario.
* Configurar e implementar la seguridad de los datos mediante consultas filtradas por `ownerId` y reglas de seguridad en Firestore.
* Documentar, verificar y probar las decisiones técnicas del proyecto.

---

## 4. Producto Esperado y Fuentes de Datos

La solución consiste en un Gestor Personal de Tareas donde las dos fuentes de datos cumplen responsabilidades diferenciadas y complementarias:

| Fuente de Datos | Responsabilidad Principal |
| :--- | :--- |
| **Firebase Authentication** | Registro, inicio, persistencia de sesión y obtención del identificador del usuario actual (`uid`). |
| **Cloud Firestore** | Almacenamiento remoto y sincronizado de las tareas publicadas por cada usuario. |
| **Room Database** | Almacenamiento local persistente de borradores de tareas creados sin necesidad de conexión a Internet. |

*Nota de Alcance:* Room no se utiliza como réplica bidireccional completa de Firestore para evitar complejidad innecesaria en resolución de conflictos, reintentos y duplicados. La publicación de borradores a Firestore sigue un flujo unidireccional y controlado.

---

## 5. Tecnologías Utilizadas

* **Lenguaje:** Kotlin
* **Interfaz de Usuario:** Jetpack Compose + Material Design 3
* **Navegación:** Navigation Compose
* **Gestión de Estado y Asincronía:** ViewModel, StateFlow, Kotlin Coroutines
* **Backend y Autenticación:** Firebase Authentication, Cloud Firestore (administrados mediante Firebase Android BoM)
* **Persistencia Local:** Room Database, Kapt/KSP
* **Inyección de Dependencias:** Hilt / Dagger
* **Control de Versiones:** Git y GitHub

---

## 6. Arquitectura del Sistema

La aplicación está construida bajo el patrón **MVVM (Model-View-ViewModel)** aplicando principios de **Clean Architecture** con flujo de dependencias unidireccional hacia el dominio.

```text
┌────────────────────────────────────────────────────────┐
│                   CAPA DE PRESENTACIÓN                 │
│        Jetpack Compose  ◄──►  ViewModels (StateFlow)   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    CAPA DE DOMINIO                     │
│       TaskUseCases  /  Task & Repository Interfaces     │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                     CAPA DE DATOS                      │
│   Room DAO (Local DB)  ◄──►  TaskRepositoryImpl  ◄──► Firestore Remote  │
└────────────────────────────────────────────────────────┘
```

### Matriz de Responsabilidades por Capa

| Capa | Responsabilidad | Prohibición |
| :--- | :--- | :--- |
| **UI** | Mostrar componentes visuales, capturar acciones del usuario y manejar la navegación. | No debe consultar Firebase ni DAOs de Room directamente. |
| **ViewModel** | Coordinar los casos de uso, exponer el estado reactivo (`UiState`) y gestionar la lógica de presentación. | No debe contener referencias a componentes visuales ni credenciales. |
| **Domain** | Definir los modelos de negocio, interfaces de repositorios y casos de uso. | No debe depender de librerías concretas como Firebase o Room. |
| **Data** | Implementar los repositorios, mapeadores de datos y comunicación con DAOs y Firestore. | No debe tomar decisiones de presentación visual. |
| **DI** | Proveer y gestionar el ciclo de vida de las dependencias con Hilt. | No debe contener reglas de negocio. |

### Estructura de Paquetes del Proyecto (`com.example.taller_android`)

```text
com.example.taller_android
├── data
│   ├── TaskDao.kt
│   ├── TaskDatabase.kt
│   ├── TaskEntity.kt
│   └── TaskRepositoryImpl.kt
├── di
│   └── AppModule.kt
├── domain
│   ├── Task.kt
│   ├── TaskRepository.kt
│   └── TaskUseCases.kt
├── ui
│   ├── AppNavigation.kt
│   ├── AuthViewModel.kt
│   ├── LoginScreen.kt
│   ├── RegisterScreen.kt
│   ├── TaskListScreen.kt
│   ├── TaskViewModel.kt
│   └── Theme.kt
├── MainActivity.kt
└── TallerApp.kt
```

---

## 7. Modelo de Datos

### Modelo de Dominio (`Task.kt`)
```kotlin
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "Media",
    val isDraft: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

### Entidad Local para Room (`TaskEntity.kt`)
```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: String,
    val isDraft: Boolean,
    val userId: String,
    val createdAt: Long
)
```

El campo `userId` (que corresponde al `uid` entregado por Firebase Authentication) actúa como clave de asociación obligatoria para garantizar la privacidad y el aislamiento de los datos.

---

## 8. Requerimientos Funcionales

* **RF01 - Registro de Usuarios:** Registro mediante correo electrónico, contraseña y confirmación. Incluye validaciones locales de formato de correo, contraseña de mínimo 6 caracteres y coincidencia de campos.
* **RF02 - Inicio de Sesión:** Autenticación de credenciales, indicación visual de carga, prevención de envíos duplicados, manejo de errores y preservación de la sesión activa al reiniciar la aplicación.
* **RF03 - Cierre de Sesión:** Cierre seguro de la sesión del usuario. Previene el acceso a pantallas protegidas mediante la navegación hacia atrás.
* **RF04 - Creación de Tareas Remotas:** Creación y almacenamiento de tareas en Cloud Firestore vinculadas al `userId` activo.
* **RF05 - Consulta de Tareas:** Obtención y visualización exclusiva de las tareas pertenecientes al usuario autenticado.
* **RF06 - Actualización de Tareas:** Edición de título, descripción, prioridad y estado de completado.
* **RF07 - Eliminación de Tareas:** Confirmación antes del borrado y eliminación efectiva en la fuente remota.
* **RF08 - Gestión de Borradores en Room:** Creación, lectura y eliminación local de borradores creados sin conexión.
* **RF09 - Control de Acceso y Aislamiento:** Reglas de seguridad que impiden que un usuario pueda leer, modificar o eliminar tareas de otro usuario.

### Algoritmo de Publicación Segura de Borradores
1. Se envía la tarea a la colección `tasks` de Cloud Firestore.
2. Se espera la confirmación de guardado exitoso desde el servidor remoto.
3. Una vez confirmado el éxito en la nube, se procede a eliminar el borrador de Room.
4. En caso de falla o falta de red, el borrador se conserva en Room notificando el error al usuario.

---

## 9. Casos de Uso

| Grupo | Caso de Uso | Responsabilidad |
| :--- | :--- | :--- |
| **Autenticación** | `RegisterUserUseCase` | Valida credenciales e invoca el registro en Firebase. |
| | `LoginUserUseCase` | Autentica las credenciales del usuario en Firebase. |
| | `LogoutUserUseCase` | Finaliza la sesión actual. |
| | `GetCurrentUserUseCase` | Obtiene el estado del usuario autenticado. |
| **Tareas Remotas** | `CreateTaskUseCase` | Registra una nueva tarea en Cloud Firestore. |
| | `GetTasksUseCase` | Consulta las tareas remotas filtradas por `userId`. |
| | `UpdateTaskUseCase` | Actualiza los atributos de una tarea remota. |
| | `DeleteTaskUseCase` | Remueve una tarea remota en Firestore. |
| **Borradores Locales** | `SaveDraftUseCase` | Almacena un borrador en Room. |
| | `GetDraftsUseCase` | Consulta el listado local de borradores. |
| | `DeleteDraftUseCase` | Elimina un borrador de la base de datos local. |

---

## 10. Estados de la Interfaz

La interfaz de usuario implementa la gestión explícita de estados a través de `StateFlow`.

### Ejemplo de Estado de Pantalla
```kotlin
data class TaskListUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null
)
```

### Estado de Operaciones Asíncronas
```kotlin
sealed interface OperationState {
    data object Idle : OperationState
    data object Loading : OperationState
    data object Success : OperationState
    data class Error(val message: String) : OperationState
}
```

---

## 11. Fases de Desarrollo del Taller

1. **Fase 1 - Análisis y Diseño:** Definición de arquitectura, diagramas de flujo, modelos de datos (`Task`, `TaskEntity`) y estructura inicial de la aplicación.
2. **Fase 2 - Configuración de Firebase:** Registro de la aplicación Android en la consola de Firebase, incorporación del archivo `google-services.json`, inclusión de Firebase Android BoM y habilitación de Authentication y Cloud Firestore.
3. **Fase 3 - Módulo de Autenticación:** Desarrollo de pantallas de Login y Registro en Jetpack Compose, validación de formularios, conservación de sesión e implementación de Logout.
4. **Fase 4 - CRUD con Cloud Firestore:** Implementación del repositorio remoto, creación, consulta filtrada por `userId`, actualización y eliminación de tareas.
5. **Fase 5 - Persistencia Local con Room:** Creación de `TaskDatabase`, `TaskDao` y repositorios para la gestión de borradores locales y su posterior publicación controlada.
6. **Fase 6 - Integración y Pruebas:** Validación de flujos de navegación, pruebas con dos cuentas de usuario independientes, verificación offline y empaquetado del proyecto.

---

## 12. Seguridad y Reglas de Firestore

Las consultas a Cloud Firestore están filtradas obligatoriamente por el `userId` del usuario autenticado. Adicionalmente, la seguridad a nivel de servidor se garantiza mediante las siguientes reglas en Cloud Firestore:

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /tasks/{taskId} {
      allow create: if request.auth != null 
                  && request.resource.data.userId == request.auth.uid;
                  
      allow read, delete: if request.auth != null 
                        && resource.data.userId == request.auth.uid;
                        
      allow update: if request.auth != null 
                  && resource.data.userId == request.auth.uid 
                  && request.resource.data.userId == request.auth.uid;
    }
  }
}
```

---

## 13. Matriz de Casos de Prueba

| Código | Caso de Prueba | Procedimiento / Datos de Entrada | Resultado Esperado |
| :---: | :--- | :--- | :--- |
| **P01** | Registrar datos válidos | Formulario con correo válido y contraseña de 6+ caracteres | Cuenta creada e inicio de sesión automático |
| **P02** | Registrar correo existente | Intento de registro con correo ya utilizado | Notificación de error clara sin cierre inesperado de la app |
| **P03** | Credenciales incorrectas | Inicio de sesión con contraseña errónea | Denegación de acceso y mensaje de error |
| **P04** | Persistencia de sesión | Reiniciar la aplicación con sesión iniciada | Apertura directa de la pantalla principal |
| **P05** | Cierre de sesión y botón Atrás | Cerrar sesión y presionar la navegación hacia atrás | La aplicación permanece en la pantalla de Login |
| **P06** | CRUD completo en Firestore | Crear, consultar, editar y borrar tarea remota | Las cuatro operaciones se reflejan correctamente en Firestore |
| **P07** | Aislamiento de datos | Iniciar sesión con un segundo usuario | No se visualizan las tareas del primer usuario |
| **P08** | Guardar borrador local | Guardar borrador en Room y reiniciar la app | El borrador permanece disponible en la base de datos local |
| **P09** | Publicar borrador con red | Publicar un borrador con conexión activa | Tarea creada en Firestore y borrada automáticamente de Room |
| **P10** | Fallo de red al publicar | Intentar publicar un borrador sin conexión | Notificación de error manteniendo el borrador intacto en Room |

---

## 14. Requisitos de Calidad

* Nombres de clases, métodos y variables descriptivos e idóneos en español/inglés.
* Separación estricta de responsabilidades: cero lógica de negocio dentro de composables.
* Comunicación de UI a fuentes de datos intermediada obligatoriamente por ViewModels y Repositorios.
* Uso de inyección de dependencias mediante Hilt para desacoplamiento y facilidades de prueba.
* Exposición exclusiva de estados inmutables desde ViewModels a la interfaz.
* Manejo adecuado de excepciones y errores de red informando al usuario.

---

## 15. Entregables del Proyecto

1. Código fuente completo alojado en el repositorio de GitHub.
2. Archivo `README.md` detallado y estructurado.
3. APK compilado y funcional en modo debug.
4. Diagrama de arquitectura y modelo de datos.
5. Archivo `google-services.json` configurado.

---

## 16. Criterios de Aceptación y Evaluación

* Registro, inicio y cierre de sesión funcionales.
* Persistencia adecuada de la sesión del usuario.
* Operaciones CRUD completadas sobre Cloud Firestore.
* Filtrado estricto de datos por usuario.
* Persistencia local de borradores con Room Database.
* Publicación segura de borradores y manejo de errores.
* Arquitectura MVVM respetada sin accesos directos desde la UI a la BD.
* Visualización explícita de estados (carga, éxito, vacío, error).

---

## 17. Retos Adicionales Implementados

* **Filtros por prioridad y estado:** Organización visual de tareas.
* **Selección visual de prioridad:** Modificadores de prioridad alta, media y baja.
* **Inyección con Hilt:** Módulo `AppModule` para proveer instancias de Firebase, Room y Repositorios.

---

## 18. Autoría y Créditos

* **Desarrollado por:** Santiago
* **Correo electrónico:** santiago2103304@gmail.com
* **Materia / Contexto:** Desarrollo de Software Móvil Android

---

## 19. Fuentes de Consulta

* Documentación oficial de Firebase para Android: https://firebase.google.com/docs/android/setup
* Autenticación con correo y contraseña en Firebase: https://firebase.google.com/docs/auth/android/password-auth
* Guía de administración de datos en Cloud Firestore: https://firebase.google.com/docs/firestore/manage-data/add-data
* Documentación de persistencia con Room: https://developer.android.com/training/data-storage/room
* Guía oficial de arquitectura de aplicaciones Android: https://developer.android.com/topic/architecture
