# Comercializadora Angel

Aplicación Android para gestionar productos e inventario de una bodega, desarrollada en Java con una base local SQLite.

## Funciones

- Inicio de sesión y administración de usuarios y roles.
- Gestión de categorías y proveedores.
- Registro y edición de productos con SKU único, descripción, categoría, stock mínimo y precio por kilo.
- Búsqueda por nombre o SKU y alertas de stock bajo.
- Deshabilitación de productos sin borrar su historial.
- Entradas y salidas con cantidad, motivo, usuario y proveedor opcional para entradas.
- Actualización del stock y registro del movimiento dentro de una misma transacción.
- Migración de la base de datos a la versión 6.

## Ejecución

Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutar el módulo `app`.

- Android 11 o superior.
- SDK de compilación 37.
- Java 25 para Gradle.
- Usuario de demostración: `admin`.
- Contraseña de demostración: `admin123`.

Las cantidades se ingresan en kilos, con punto o coma y hasta tres decimales. SQLite almacena gramos enteros: 1,250 kg equivale a 1250 g. Los precios se expresan en pesos chilenos por kilo.

El stock existente se modifica desde «Entradas y salidas»; la edición del producto modifica su ficha y su mínimo.

## Base de datos

`DBHelper.java` centraliza el esquema, las migraciones y las consultas con `SQLiteOpenHelper`. Las tablas principales son ROL, USUARIO, CATEGORIA, PROVEEDOR, PRODUCTO y MOVIMIENTO.

El [esquema SQL](docs/esquema.sql) permite crear la estructura en una base vacía. Para actualizar una instalación existente se utilizan las migraciones de la aplicación.

La migración v5 a v6 conserva los identificadores y saldos existentes, relaciona los productos con categorías y asigna códigos SKU iniciales con el formato `LEG-000001`.

## Validación

```text
gradlew.bat assembleDebug connectedDebugAndroidTest lintDebug
```

Resultado verificado en Android 11: nueve pruebas instrumentales aprobadas. Lint: cero errores y 92 advertencias.

Las pruebas cubren SKU duplicado, validaciones de cantidades, búsqueda y edición, claves foráneas, movimientos atómicos, stock insuficiente, baja lógica y migraciones que conservan datos.

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Trabajo del equipo

El proyecto continúa los avances del grupo:

- Jaifencio: autenticación, usuarios y roles.
- Catalina: categorías y proveedores.
- Esteban: diseño de las pantallas.
- Leander Rodriguez: productos, inventario e integración de la base SQLite.

Referencias de desarrollo:

- [Ejemplo del profesor](https://github.com/oscarmonardezsaguas/Semana_11_SQLite).
- [Avance de Jaifencio](https://github.com/jaifencio-sudo/BodegaComercializadoraAngel).
- [Avance de Catalina](https://github.com/Catalina0520/SQLite).
- [Diseño de Esteban](https://github.com/Esteban-Gomez12/avance-del-dise-o).

[Alcance y pendientes](docs/REVISION_Y_PENDIENTES.md).
