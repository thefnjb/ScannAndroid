package com.proyecto.scann;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.proyecto.scann.Interface.ApiService;
import com.proyecto.scann.Interface.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Registrar extends AppCompatActivity {
    // Declaración de los componentes
    EditText txtNombre, txtCorreo, txtTelefono, txtUsuario, txtClave;
    Button btnRegistrar;
    TextView lblIniciarSesion;
    // Interfaz para la API
    private ApiService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Inicialización de los componentes
        txtNombre = findViewById(R.id.txtnomapellidos);
        txtCorreo = findViewById(R.id.txtemail);
        txtTelefono = findViewById(R.id.txttelefono);
        txtUsuario = findViewById(R.id.txtusuario);
        txtClave = findViewById(R.id.txtclave);
        btnRegistrar = findViewById(R.id.btnregistrar);
        lblIniciarSesion = findViewById(R.id.lbliniciarsesion);

        // Configurar Retrofit con la URL  basede MockAPI
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://67dac53435c87309f52df40a.mockapi.io/") // MockAPI
                .addConverterFactory(GsonConverterFactory.create()) // Convertidor JSON
                .build();
        apiService = retrofit.create(ApiService.class);

        // Acción del botón "Registrar"
        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        // Redirigir a pantalla de login
        lblIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(Registrar.this, loginAty.class);
            startActivity(intent);
        });
    }
    // Metodo para registrar un nuevo usuario
    private void registrarUsuario() {
        // Obtener datos ingresados por el usuario
        String nombre = txtNombre.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String usuario = txtUsuario.getText().toString().trim();
        String clave = txtClave.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || usuario.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el correo ya está registrado en la base de datos
        Call<List<LoginResponse>> call = apiService.getUsers();
        call.enqueue(new Callback<List<LoginResponse>>() {
            @Override
            public void onResponse(Call<List<LoginResponse>> call, Response<List<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LoginResponse> usuarios = response.body();

                    for (LoginResponse user : usuarios) {
                        if (user.getCorreo().equalsIgnoreCase(correo)) {
                            Toast.makeText(Registrar.this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                            return; // Evita que se registre un usuario duplicado
                        }
                    }
                    // Si el correo no está registrado, crear un nuevo usuario
                    crearNuevoUsuario(nombre, correo, telefono, usuario, clave);
                } else {
                    Toast.makeText(Registrar.this, "Error al verificar usuarios", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<LoginResponse>> call, Throwable t) {
                Toast.makeText(Registrar.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Metodo para registrar un nuevo usuario en la base de datos
    private void crearNuevoUsuario(String nombre, String correo, String telefono, String usuario, String clave) {
        LoginResponse nuevoUsuario = new LoginResponse(nombre, correo, telefono, usuario, clave);

        Call<LoginResponse> call = apiService.registrarUsuario(nuevoUsuario);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(Registrar.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Registrar.this, loginAty.class));
                    finish(); // Cierra la actividad actual
                } else {
                    Toast.makeText(Registrar.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(Registrar.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
