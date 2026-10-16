# Alcance y pendientes

## Productos e inventario

El módulo permite registrar productos con SKU único, nombre, descripción, categoría, saldo inicial, mínimo y precio. La búsqueda responde a cambios en el texto y consulta nombre o SKU. La edición conserva el saldo y la deshabilitación utiliza el campo `activo`, sin eliminar registros históricos.

Las entradas y salidas se registran en MOVIMIENTO con producto, usuario, cantidad en gramos, fecha y motivo. Las entradas admiten un proveedor opcional. Una transacción actualiza el saldo y guarda el movimiento; cualquier error revierte ambas operaciones.

## Relaciones

- USUARIO referencia ROL.
- PRODUCTO referencia CATEGORIA.
- MOVIMIENTO referencia PRODUCTO y USUARIO, y opcionalmente PROVEEDOR.

Las claves foráneas están habilitadas. El SKU es único sin distinguir mayúsculas. Los saldos y mínimos son enteros no negativos expresados en gramos. La tabla `persona` se conserva por compatibilidad con el ejemplo inicial y no participa en las pantallas de bodega.

## Migraciones

La versión 6 conserva los identificadores de productos, los nombres, saldos, mínimos y precios de la versión 5. Crea categorías para los nombres existentes y genera SKU editables con el prefijo `LEG-`. Los kilos se convierten a gramos redondeando al gramo.

El historial contiene los movimientos registrados desde esta versión. Los saldos iniciales o heredados no se convierten en compras o salidas ficticias.

Un esquema de proveedores desconocido cancela la actualización para conservar los datos originales. La compatibilidad con todas las variantes anteriores a v5 requiere ampliar los casos de migración.

## Pendientes de integración

1. Precisar los requisitos del módulo 4, que no figura en el documento de casos de uso disponible.
2. Confirmar con el grupo la unidad base en gramos y su presentación en kilos.
3. Completar la confirmación de cierre de sesión, el mensaje específico para usuarios inactivos y la actualización de roles durante una sesión.
4. Revisar los permisos de categorías, proveedores y edición de productos. La administración de usuarios y la deshabilitación de productos están restringidas a ADMIN en la interfaz.
5. Revisar la validación del dígito verificador del RUT y el almacenamiento de credenciales y sesión.
6. Para mayor volumen, ejecutar consultas fuera del hilo principal y paginar el historial.

## Pruebas

Nueve pruebas instrumentales aprobadas en Android 11, incluidas ocho pruebas de integración de inventario y la prueba preexistente del proyecto. Se comprobaron la migración v5 a v6, la conservación del producto ID 42 y del proveedor ID 71, la integridad referencial y la reversión de una migración v4 con esquema desconocido.

La compilación y `lintDebug` finalizaron sin errores. Lint mantiene 92 advertencias de recursos, textos y presentación.
