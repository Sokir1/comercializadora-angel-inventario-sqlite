package com.example.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;
import static org.junit.Assert.*;

public class InventarioDatabaseTest {
    Context ctx; DBHelper db; String name;
    @Before public void setup() {
        ctx=InstrumentationRegistry.getInstrumentation().getTargetContext();
        name="test_inventario_"+System.nanoTime()+".db"; db=new DBHelper(ctx,name);
    }
    @After public void cleanup() { db.close(); ctx.deleteDatabase(name); }
    Producto nuevo(String sku,double stock) {
        Producto p=new Producto(); p.nombre="Prueba"; p.sku=sku; p.descripcion="Descripción";
        p.categoria="Semillas"; p.stock=stock; p.stockMinimo=1; p.precioKg=1000; db.insertarProducto(p); return p;
    }
    interface Action { void run(); }
    void falla(Action a) { try { a.run(); fail("Debía rechazar la operación"); } catch(IllegalArgumentException|android.database.sqlite.SQLiteException expected) {} }
    @Test public void crearBuscarYEditar() {
        Producto p=nuevo("SKU-UNO",2.125); assertEquals(1,db.listarProductos("sku-uno").size());
        p.nombre="Otro"; p.stock=999; db.actualizarProducto(p);
        Producto actual=db.obtenerProducto(p.idProducto); assertEquals("Otro",actual.nombre);
        assertEquals(2.125,actual.stock,0.00001); assertEquals("Descripción",actual.descripcion);
    }
    @Test public void skuUnicoSinDistinguirMayusculas() { nuevo("ABC",0); falla(()->nuevo("abc",1)); }
    @Test public void validaCantidadesYCategoria() {
        falla(()->nuevo("NEG",-1)); falla(()->nuevo("NAN",Double.NaN)); falla(()->nuevo("DEC",0.0001));
        Producto p=nuevo("CAT",0); p.categoria="No existe"; falla(()->db.actualizarProducto(p));
    }
    @Test public void movimientosAtomicosYStockInsuficiente() {
        Producto p=nuevo("MOV",2); long u=db.autenticar("admin","admin123").idUsuario;
        db.registrarMovimiento(p.idProducto,u,null,"ENTRADA",0.125,"Compra");
        db.registrarMovimiento(p.idProducto,u,null,"SALIDA",1,"Venta");
        assertEquals(1.125,db.obtenerProducto(p.idProducto).stock,0.00001);
        falla(()->db.registrarMovimiento(p.idProducto,u,null,"SALIDA",9,"Exceso"));
        assertEquals(2,db.historialMovimientos().size());
        assertEquals(1.125,db.obtenerProducto(p.idProducto).stock,0.00001);
        falla(()->db.registrarMovimiento(p.idProducto,u,999999L,"ENTRADA",1,"Proveedor inválido"));
        assertEquals(1.125,db.obtenerProducto(p.idProducto).stock,0.00001);
    }
    @Test public void bajaLogicaConservaHistorial() {
        Producto p=nuevo("BAJA",2); long u=db.autenticar("admin","admin123").idUsuario;
        db.registrarMovimiento(p.idProducto,u,null,"SALIDA",1,"Venta"); db.eliminarProducto(p.idProducto);
        assertTrue(db.listarProductos("BAJA").isEmpty()); assertFalse(db.obtenerProducto(p.idProducto).activo);
        assertEquals(1,db.historialMovimientos().size());
        falla(()->db.registrarMovimiento(p.idProducto,u,null,"ENTRADA",1,"Inactivo"));
    }
    @Test public void renombrarCategoriaMantieneRelacion() {
        Producto p=nuevo("FK",1); long id=db.obtenerProducto(p.idProducto).idCategoria;
        db.actualizarCategoria(id,"Nombre nuevo",""); assertEquals("Nombre nuevo",db.obtenerProducto(p.idProducto).categoria);
        try(Cursor c=db.getReadableDatabase().rawQuery("PRAGMA foreign_key_check",null)) { assertFalse(c.moveToFirst()); }
    }
    @Test public void migrarV5ConservaIdsStockYProveedor() {
        db.close();
        try(SQLiteDatabase old=ctx.openOrCreateDatabase(name,0,null)) {
            old.execSQL("CREATE TABLE CATEGORIA(id_categoria INTEGER PRIMARY KEY,nombre_categoria TEXT UNIQUE COLLATE NOCASE,descripcion TEXT,activo INTEGER)");
            old.execSQL("CREATE TABLE PRODUCTO(id_producto INTEGER PRIMARY KEY,nombre TEXT,categoria TEXT,stock REAL,stock_minimo REAL,precio_kg INTEGER)");
            old.execSQL("CREATE TABLE ROL(id_rol INTEGER PRIMARY KEY,nombre_rol TEXT,descripcion TEXT)");
            old.execSQL("CREATE TABLE USUARIO(id_usuario INTEGER PRIMARY KEY,username TEXT,password_hash TEXT,nombre_completo TEXT,id_rol INTEGER,activo INTEGER)");
            old.execSQL("CREATE TABLE PROVEEDOR(id_proveedor INTEGER PRIMARY KEY,rut_empresa TEXT,razon_social TEXT,numero_contacto TEXT,direccion TEXT)");
            old.execSQL("INSERT INTO PRODUCTO VALUES(42,'Heredado','Especial',18.5,2.125,1234)");
            old.execSQL("INSERT INTO PROVEEDOR VALUES(71,'12345678-9','Proveedor propio','12345678','Casa')"); old.setVersion(5);
        }
        db=new DBHelper(ctx,name); Producto p=db.obtenerProducto(42);
        assertEquals(18.5,p.stock,0.00001); assertEquals(2.125,p.stockMinimo,0.00001); assertEquals("LEG-000042",p.sku);
        assertEquals(71,db.listarProveedores().get(0).idProveedor);
        try(Cursor c=db.getReadableDatabase().rawQuery("PRAGMA foreign_key_check",null)) { assertFalse(c.moveToFirst()); }
    }
    @Test public void proveedorV4DesconocidoRevierteSinBorrar() {
        db.close();
        try(SQLiteDatabase old=ctx.openOrCreateDatabase(name,0,null)) {
            old.execSQL("CREATE TABLE PROVEEDOR(id INTEGER PRIMARY KEY,otro TEXT)");
            old.execSQL("INSERT INTO PROVEEDOR VALUES(81,'Conservar')"); old.setVersion(4);
        }
        db=new DBHelper(ctx,name);
        try { db.getWritableDatabase(); fail("Debía rechazar esquema desconocido"); }
        catch(IllegalStateException expected) { assertTrue(expected.getMessage().contains("conservar")); }
        db.close();
        try(SQLiteDatabase old=ctx.openOrCreateDatabase(name,0,null);
            Cursor c=old.rawQuery("SELECT otro FROM PROVEEDOR WHERE id=81",null)) {
            assertTrue(c.moveToFirst()); assertEquals("Conservar",c.getString(0)); assertEquals(4,old.getVersion());
        }
    }

}
