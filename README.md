# Comercializadora Ángel · Inventario Android

Aplicación Android escrita en Java para gestionar productos, categorías, proveedores, usuarios y movimientos de bodega con una base SQLite local. Proyecto de portafolio con stock en kilogramos y persistencia interna en gramos enteros.

**Java · Android SDK · SQLite · Material Components**

[Perfil del desarrollador](https://github.com/Sokir1) · [Pruebas instrumentadas](app/src/androidTest/java/com/example/sqlite/InventarioDatabaseTest.java) · [Esquema SQL](docs/esquema.sql)

## Qué permite hacer

- Iniciar sesión con usuarios activos y guardar la sesión local con su rol.
- Crear y editar productos con SKU único sin distinguir mayúsculas, descripción, categoría, precio y stock mínimo.
- Consultar inventario y productos con stock bajo; buscar por nombre o SKU.
- Registrar entradas y salidas con motivo, responsable y proveedor opcional para entradas.
- Rechazar movimientos con stock insuficiente y conservar el historial al dar de baja un producto.
- Administrar categorías, proveedores y usuarios desde las pantallas correspondientes.

## Decisiones técnicas

| Decisión | Motivo |
| --- | --- |
| Cantidades enteras en gramos | Representar tres decimales de kilogramos sin acumular errores de coma flotante en el stock persistido |
| Movimiento y actualización de stock en una transacción | Revertir la operación completa si falla una validación o una referencia |
| Claves foráneas y restricciones SQL | Mantener relaciones y rechazar cantidades o referencias inválidas |
| Bajas lógicas de productos | Conservar los movimientos históricos |
| Migración a esquema v6 | Normalizar categorías y productos preservando identificadores y existencias heredadas |

La implementación está concentrada en [DBHelper.java](app/src/main/java/com/example/sqlite/DBHelper.java). Las Activities consumen ese acceso a datos; [SessionManager](app/src/main/java/com/example/sqlite/SessionManager.java) mantiene la sesión en SharedPreferences. No hay un backend remoto ni sincronización con el proyecto Emporio.

## Abrir y ejecutar

1. Clona este repositorio y abre su carpeta raíz en Android Studio.
2. Sincroniza Gradle utilizando el wrapper incluido. La configuración actual declara **Android Gradle Plugin 9.3.3**, **compileSdk/targetSdk 37**, **minSdk 30** y **JVM de Gradle 25**. El código Java compila con compatibilidad de lenguaje 11. Usa una versión de Android Studio compatible con esos requisitos y deja instalar los SDK necesarios.
3. Crea un emulador o conecta un dispositivo con Android API 30 o superior y selecciona el módulo `app`.
4. Ejecuta la aplicación. SQLite crea y prepara su base en el almacenamiento privado de la app; no requiere instalar un servidor SQL ni importar manualmente `docs/esquema.sql`.

La instalación nueva incluye la cuenta de demostración **`admin` / `admin123`**, definida en el código de inicialización. Úsala solo para explorar la demo con datos ficticios. La autenticación local actual y estas credenciales de ejemplo deben revisarse antes de usar información real.

Para generar una APK de depuración desde PowerShell, con el SDK y la JVM configurados:

```powershell
./gradlew.bat assembleDebug
```

Salida habitual: `app/build/outputs/apk/debug/app-debug.apk`. En Linux/macOS utiliza `sh gradlew assembleDebug`.

## Verificar inventario y migraciones

Con un emulador iniciado o un dispositivo de pruebas conectado:

```powershell
./gradlew.bat connectedDebugAndroidTest
```

En Linux/macOS utiliza `sh gradlew connectedDebugAndroidTest`. Las pruebas de [InventarioDatabaseTest](app/src/androidTest/java/com/example/sqlite/InventarioDatabaseTest.java) crean bases temporales con nombres propios y las eliminan al terminar. Verifican:

- Creación, búsqueda y edición sin alterar el stock desde la edición de un producto.
- Unicidad de SKU, cantidades, precisión y categorías válidas.
- Movimientos atómicos y rechazo de stock insuficiente o proveedor inválido.
- Baja lógica y conservación del historial.
- Renombrado de categorías e integridad referencial.
- Migración desde v5 conservando identificadores, stock y proveedores.
- Rechazo de un esquema de proveedor desconocido sin borrar los datos originales.

Los archivos de pruebas existen en el repositorio. Esta revisión de documentación no ha ejecutado la compilación ni las pruebas Android; requieren el entorno descrito. No hay un workflow de CI Android configurado actualmente.

## Estructura

| Ruta | Contenido |
| --- | --- |
| `app/src/main/java/com/example/sqlite` | Activities, modelos, sesión y DBHelper |
| `app/src/main/res/layout` | Pantallas y diálogos XML |
| `app/src/androidTest` | Pruebas que requieren Android |
| `app/src/test` | Pruebas JVM; incluye la prueba de ejemplo del proyecto |
| `docs/esquema.sql` | Referencia del esquema de inventario |
| `gradle/libs.versions.toml` | Versiones de plugins y dependencias |

## Alcance y siguientes mejoras

La información permanece en un dispositivo. La autenticación utiliza un hash propio basado en SHA-256; migrar a una función específica para contraseñas con sal y coste configurable es una mejora pendiente. Las operaciones SQLite se realizan desde las pantallas; separar repositorios/servicios y mover el trabajo fuera del hilo principal facilitará el crecimiento de la aplicación.

- [ ] Registrar una ejecución reproducible de compilación y pruebas instrumentadas.
- [ ] Añadir capturas reales con datos ficticios y una demostración de movimientos.
- [ ] Mejorar el almacenamiento de contraseñas y eliminar credenciales fijas para uso real.
- [ ] Incorporar acceso a datos asíncrono y una capa de repositorios.
- [ ] Diseñar respaldo y restauración con pruebas antes de añadir sincronización.

Autor del repositorio: [Sokir1](https://github.com/Sokir1). Esta documentación describe el código disponible y no afirma despliegue comercial ni uso en producción.
