package com.uth.apputh;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PersonaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Persona> personas;

    public PersonaAdapter(Context context, List<Persona> personas) {
        this.context = context;
        this.personas = personas;
    }

    @Override
    public int getCount() {
        return personas.size();
    }

    @Override
    public Object getItem(int position) {
        return personas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return personas.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_persona, parent, false);
        }

        ImageView imgFoto = convertView.findViewById(R.id.imgFotoItem);
        TextView txtNombre = convertView.findViewById(R.id.txtNombreItem);
        TextView txtDetalle = convertView.findViewById(R.id.txtDetalleItem);

        Persona persona = personas.get(position);

        txtNombre.setText(persona.getNombre() + " " + persona.getApellido());
        txtDetalle.setText(persona.getEdad() + " años  •  " + persona.getCorreo());

        byte[] foto = persona.getFoto();
        if (foto != null && foto.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(foto, 0, foto.length);
            imgFoto.setImageBitmap(bitmap);
        } else {
            imgFoto.setImageResource(R.mipmap.ic_launcher);
        }

        return convertView;
    }
}
