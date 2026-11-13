# Control de equipos de laboratorio

Aplicación de escritorio para administrar el catálogo de equipos de un laboratorio. Está desarrollada con JavaFX, Hibernate ORM y PostgreSQL.

## Requisitos

- JDK 21.
- PostgreSQL.
- IntelliJ IDEA.
- Maven, o el Maven Wrapper incluido en el proyecto.

## Base de datos

La aplicación usa la base de datos `bd_laboratorio_orm` y el esquema `catalogos`.

El esquema contiene:

- `catalogos.tipo_equipo`: tipos de equipo.
- `catalogos.equipo`: equipos registrados y su tipo asociado.

Cada equipo pertenece a un tipo mediante la columna `id_tipo`. PostgreSQL valida que el código sea único, que la cantidad no sea negativa y que el estado sea uno de los permitidos.

### Crear y preparar la base

Ejecuta `CREATE DATABASE bd_laboratorio_orm;` una sola vez. Luego conéctate a esa base y ejecuta las instrucciones del script SQL que crean el esquema, las tablas y los datos iniciales.

Si usas `psql` y el script contiene `\connect bd_laboratorio_orm`, puedes ejecutarlo desde la terminal con:

```powershell
psql -U postgres -f bd_laboratorio_orm.sql
```

En pgAdmin, ejecuta la creación de la base por separado. Después abre una consulta conectada a `bd_laboratorio_orm` y ejecuta el resto del script. La instrucción `\connect` es propia de `psql` y no se ejecuta en el editor SQL de pgAdmin.

## Configuración local

La unidad de persistencia se llama `laboratorioPU`. La conexión está configurada para PostgreSQL local:

- Host: `localhost`
- Puerto: `5432`
- Base de datos: `bd_laboratorio_orm`
- Usuario: `postgres`
- Esquema: `catalogos`

La contraseña se lee de la variable de entorno `DB_PASSWORD`. En PowerShell, puedes definirla para la terminal actual así:

```powershell
$env:DB_PASSWORD = "TU_CONTRASENA_LOCAL"
```

No subas contraseñas reales al repositorio. Configura la variable en el entorno local donde vayas a ejecutar la aplicación.

Hibernate usa `hibernate.hbm2ddl.auto=validate`: comprueba que el esquema exista y coincida con las entidades; no crea las tablas. Ejecuta el script SQL antes de iniciar la aplicación.

## Ejecutar la aplicación

Desde la carpeta raíz del proyecto, ejecuta:

```powershell
.\mvnw.cmd clean javafx:run
```

También puedes ejecutar el objetivo `javafx:run` desde la ventana Maven de IntelliJ IDEA.

## Funcionalidades

La pantalla permite:

- Registrar, consultar, actualizar y eliminar equipos.
- Seleccionar un tipo y un estado.
- Buscar equipos por una parte del nombre.
- Filtrar por tipo y combinar ese filtro con la búsqueda por nombre.
- Confirmar la eliminación de un equipo.
- Recibir mensajes de validación y error.

Los estados permitidos son `Disponible`, `En mantenimiento` y `Fuera de servicio`.

## Entidades y relación

Las clases `Equipo` y `TipoEquipo` son entidades JPA.

La relación es de uno a muchos: un tipo puede estar asociado a varios equipos, y cada equipo pertenece a un único tipo. En `Equipo`, la asociación se representa con `@ManyToOne` y la clave foránea `id_tipo`. En `TipoEquipo`, la colección de equipos representa el lado inverso con `@OneToMany`.

## Persistencia y consultas

La capa DAO utiliza `EntityManager` y transacciones para guardar, listar, modificar y eliminar equipos. Las operaciones de escritura inician una transacción, confirman con `commit()` si terminan correctamente y ejecutan `rollback()` si ocurre un error.

Las consultas del inventario y los filtros usan JPQL/HQL con parámetros. La lista de equipos carga también el tipo relacionado para poder mostrar su nombre en la tabla.

## Pruebas

Antes de entregar, ejecutar y documentar las pruebas P1–P8 solicitadas en la guía. Para cada prueba, registrar:

- Datos y pasos utilizados.
- Resultado esperado.
- Resultado obtenido.
- Evidencia mediante captura o consulta verificable.

También demostrar una escritura rechazada por una restricción de integridad y comprobar que el `rollback()` evitó cambios parciales.