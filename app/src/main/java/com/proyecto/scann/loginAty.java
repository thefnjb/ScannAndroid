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

public class loginAty extends AppCompatActivity {
    // Declaración de los componentes
    EditText Usuario, Clave;
    Button btningresar;
    TextView lblregistrar;
    // Interfaz para la API
    private ApiService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Inicialización de los componentes
        Usuario = findViewById(R.id.txtUsuario);
        Clave = findViewById(R.id.txtClave);
        lblregistrar = findViewById(R.id.lblregistrar);
        btningresar = findViewById(R.id.btningresar);

        // Acción del botón "Registrar" para abrir la actividad de registro
        lblregistrar.setOnClickListener(v -> {
            Intent intent = new Intent(loginAty.this, Registrar.class);
            startActivity(intent);
        });

        // Configurar Retrofit con la URL base de MockAPI
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://67dac53435c87309f52df40a.mockapi.io/") // MockAPI
                .addConverterFactory(GsonConverterFactory.create()) // Convertidor JSON
                .build();
        apiService = retrofit.create(ApiService.class);

        // Acción del botón "Ingresar" para iniciar sesión
        btningresar.setOnClickListener(v -> iniciarSesion());
    }

    // Metodo para iniciar sesión
    private void iniciarSesion() {
        String correoIngresado = Usuario.getText().toString().trim();
        String claveIngresada = Clave.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (correoIngresado.isEmpty() || claveIngresada.isEmpty()) {
            Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        // Llamar a la API para obtener los usuarios registrados
        Call<List<LoginResponse>> call = apiService.getUsers();
        call.enqueue(new Callback<List<LoginResponse>>() {
            @Override
            public void onResponse(Call<List<LoginResponse>> call, Response<List<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LoginResponse> usuarios = response.body();
                    if (usuarios.isEmpty()) {
                        Toast.makeText(loginAty.this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean usuarioEncontrado = false;
                    for (LoginResponse user : usuarios) {
                        // Verificar credenciales (ignorando mayúsculas en el correo)
                        if (user.getCorreo() != null && user.getPasword() != null &&
                                user.getCorreo().equalsIgnoreCase(correoIngresado) &&
                                user.getPasword().equals(claveIngresada)) {
                            usuarioEncontrado = true;
                            Toast.makeText(loginAty.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            // Redirigir a la actividad "scaniar" tras iniciar sesión
                            Intent intent = new Intent(loginAty.this, scaniar.class);
                            startActivity(intent);
                            finish(); // Cierra la actividad actual
                            return;
                        }
                    }
                    // Mostrar mensaje si las credenciales son incorrectas
                    if (!usuarioEncontrado) {
                        Toast.makeText(loginAty.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(loginAty.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<LoginResponse>> call, Throwable t) {
                // Manejar error de conexión con la API
                Toast.makeText(loginAty.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
