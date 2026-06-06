package com.uth.apputh;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imgFoto;
    private EditText edtNombre, edtApellido, edtEdad, edtCorreo;
    private ListView listaPersonas;

    private DatabaseHelper bd;

    private byte[] fotoSeleccionada = null;

    private int idSeleccionado = 0;

    private final ActivityResultLauncher<PickVisualMediaRequest> selectorFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        bitmap = escalar(bitmap, 600);
                        fotoSeleccionada = aBytes(bitmap);
                        imgFoto.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bd = new DatabaseHelper(this);

        imgFoto = findViewById(R.id.imgFoto);
        edtNombre = findViewById(R.id.edtNombre);
        edtApellido = findViewById(R.id.edtApellido);
        edtEdad = findViewById(R.id.edtEdad);
        edtCorreo = findViewById(R.id.edtCorreo);
        listaPersonas = findViewById(R.id.listaPersonas);

        Button btnFoto = findViewById(R.id.btnFoto);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnActualizar = findViewById(R.id.btnActualizar);
        Button btnEliminar = findViewById(R.id.btnEliminar);
        Button btnLimpiar = findViewById(R.id.btnLimpiar);

        btnFoto.setOnClickListener(v -> selectorFoto.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        btnGuardar.setOnClickListener(v -> guardar());
        btnActualizar.setOnClickListener(v -> actualizar());
        btnEliminar.setOnClickListener(v -> eliminar());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());

        listaPersonas.setOnItemClickListener((parent, view, position, id) -> {
            Persona persona = (Persona) parent.getItemAtPosition(position);
            cargarEnFormulario(persona);
        });

        listarPersonas();
    }

    private void guardar() {
        if (!formularioValido()) {
            return;
        }
        Persona persona = leerFormulario();
        long id = bd.insertar(persona);
        if (id != -1) {
            Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
            listarPersonas();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizar() {
        if (idSeleccionado == 0) {
            Toast.makeText(this, "Primero selecciona una persona de la lista", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!formularioValido()) {
            return;
        }
        Persona persona = leerFormulario();
        persona.setId(idSeleccionado);
        int filas = bd.actualizar(persona);
        if (filas > 0) {
            Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
            listarPersonas();
        } else {
            Toast.makeText(this, "No se pudo actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            Toast.makeText(this, "Primero selecciona una persona de la lista", Toast.LENGTH_SHORT).show();
            return;
        }
        int filas = bd.eliminar(idSeleccionado);
        if (filas > 0) {
            Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
            listarPersonas();
        } else {
            Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    private void listarPersonas() {
        List<Persona> personas = bd.obtenerTodos();
        PersonaAdapter adapter = new PersonaAdapter(this, personas);
        listaPersonas.setAdapter(adapter);
    }

    private void cargarEnFormulario(Persona persona) {
        idSeleccionado = persona.getId();
        edtNombre.setText(persona.getNombre());
        edtApellido.setText(persona.getApellido());
        edtEdad.setText(String.valueOf(persona.getEdad()));
        edtCorreo.setText(persona.getCorreo());

        fotoSeleccionada = persona.getFoto();
        if (fotoSeleccionada != null && fotoSeleccionada.length > 0) {
            imgFoto.setImageBitmap(BitmapFactory.decodeByteArray(
                    fotoSeleccionada, 0, fotoSeleccionada.length));
        } else {
            imgFoto.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private Persona leerFormulario() {
        Persona persona = new Persona();
        persona.setNombre(edtNombre.getText().toString().trim());
        persona.setApellido(edtApellido.getText().toString().trim());
        persona.setEdad(Integer.parseInt(edtEdad.getText().toString().trim()));
        persona.setCorreo(edtCorreo.getText().toString().trim());
        persona.setFoto(fotoSeleccionada);
        return persona;
    }

    private boolean formularioValido() {
        if (TextUtils.isEmpty(edtNombre.getText().toString().trim())) {
            edtNombre.setError("Ingresa el nombre");
            return false;
        }
        if (TextUtils.isEmpty(edtApellido.getText().toString().trim())) {
            edtApellido.setError("Ingresa el apellido");
            return false;
        }
        if (TextUtils.isEmpty(edtEdad.getText().toString().trim())) {
            edtEdad.setError("Ingresa la edad");
            return false;
        }
        String correo = edtCorreo.getText().toString().trim();
        if (TextUtils.isEmpty(correo) || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreo.setError("Ingresa un correo válido");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        idSeleccionado = 0;
        fotoSeleccionada = null;
        edtNombre.setText("");
        edtApellido.setText("");
        edtEdad.setText("");
        edtCorreo.setText("");
        edtNombre.requestFocus();
        imgFoto.setImageResource(R.mipmap.ic_launcher);
    }

    private Bitmap escalar(Bitmap original, int maxLado) {
        int ancho = original.getWidth();
        int alto = original.getHeight();
        if (ancho <= maxLado && alto <= maxLado) {
            return original;
        }
        float escala = Math.min((float) maxLado / ancho, (float) maxLado / alto);
        int nuevoAncho = Math.round(ancho * escala);
        int nuevoAlto = Math.round(alto * escala);
        return Bitmap.createScaledBitmap(original, nuevoAncho, nuevoAlto, true);
    }

    private byte[] aBytes(Bitmap bitmap) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, salida);
        return salida.toByteArray();
    }
}
