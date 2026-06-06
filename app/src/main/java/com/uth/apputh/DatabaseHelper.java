package com.uth.apputh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "personas.db";
    private static final int VERSION_BD = 1;

    public static final String TABLA = "personas";
    public static final String COL_ID = "id";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_APELLIDO = "apellido";
    public static final String COL_EDAD = "edad";
    public static final String COL_CORREO = "correo";
    public static final String COL_FOTO = "foto";

    public DatabaseHelper(@Nullable Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTabla = "CREATE TABLE " + TABLA + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOMBRE + " TEXT, " +
                COL_APELLIDO + " TEXT, " +
                COL_EDAD + " INTEGER, " +
                COL_CORREO + " TEXT, " +
                COL_FOTO + " BLOB)";
        db.execSQL(crearTabla);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    public long insertar(Persona persona) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = valoresDe(persona);
        long id = db.insert(TABLA, null, valores);
        db.close();
        return id;
    }

    public List<Persona> obtenerTodos() {
        List<Persona> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLA, null, null, null, null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Persona persona = new Persona(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_APELLIDO)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_EDAD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CORREO)),
                        cursor.getBlob(cursor.getColumnIndexOrThrow(COL_FOTO))
                );
                lista.add(persona);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public int actualizar(Persona persona) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = valoresDe(persona);
        int filas = db.update(TABLA, valores, COL_ID + " = ?",
                new String[]{String.valueOf(persona.getId())});
        db.close();
        return filas;
    }

    public int eliminar(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int filas = db.delete(TABLA, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return filas;
    }

    private ContentValues valoresDe(Persona persona) {
        ContentValues valores = new ContentValues();
        valores.put(COL_NOMBRE, persona.getNombre());
        valores.put(COL_APELLIDO, persona.getApellido());
        valores.put(COL_EDAD, persona.getEdad());
        valores.put(COL_CORREO, persona.getCorreo());
        valores.put(COL_FOTO, persona.getFoto());
        return valores;
    }
}
