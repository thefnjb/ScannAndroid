package com.proyecto.scann; // <<<< IMPORTANTE: Este es tu paquete actual, no lo cambies.

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent; // Importación necesaria para Intent
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

// <<<< Importaciones para Retrofit y la comunicación con la API (una sola vez y corregidas)
import com.proyecto.scann.Interface.ApiService; // <<<< IMPORTANTE: Ajusta si tu ApiService está en otro paquete.
import com.proyecto.scann.modelos.ScanResult; // <<<< IMPORTANTE: Ajusta si tu ScanResult está en otro paquete ('modelos' o 'models').

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class scaniar extends AppCompatActivity {
    // Declaración de los botones y el campo de texto
    Button btnScan, btnLimpiar, btnCopiar;
    Button btnVerHistorial; // <<<< CORRECCIÓN: Nombre del botón corregido a 'btnVerHistorial'
    EditText txtResultado;

    // URL base de tu MockAPI
    private static final String BASE_URL = "https://67dac53435c87309f52df40a.mockapi.io/";
    private ApiService apiService; // Instancia de tu ApiService

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaniar);

        // --- Inicialización de Retrofit y ApiService ---
        // Crear un interceptor de logging para ver las peticiones y respuestas en Logcat
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Puedes usar BASIC, HEADERS, o BODY para más detalle

        // Añadir el interceptor al OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        // Inicializar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client) // Añadir el cliente con el interceptor para ver los logs
                .build();

        // Crear la instancia de ApiService
        apiService = retrofit.create(ApiService.class);
        // --- Fin de la inicialización de Retrofit y ApiService ---

        // Inicialización de los componentes existentes
        btnScan = findViewById(R.id.btnScann);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        btnCopiar = findViewById(R.id.btnCopiar);
        txtResultado = findViewById(R.id.txtDatosEscaneados);

        // Configuración del botón "Limpiar" para borrar el contenido del EditText
        btnLimpiar.setOnClickListener(v -> txtResultado.setText(""));

        // Configuración del botón "Copiar" para copiar el texto al portapapeles
        btnCopiar.setOnClickListener(v -> copiarTexto());

        // Configuración del botón "Escanear" para iniciar el lector de códigos QR
        btnScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(scaniar.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES); // Permitir todos los formatos de código de barras
            integrator.setPrompt("Lector-QR"); // Mensaje que aparece en la cámara
            integrator.setCameraId(0); // Usa la cámara trasera y si quiero la Frontal seria 1.
            integrator.setBeepEnabled(true); // Activar sonido al escanear.
            integrator.setBarcodeImageEnabled(true); // Guardar imagen del código escaneado.
            integrator.initiateScan(); // Iniciar el escaneo.
        });

        // <<<< INICIO: Inicialización del nuevo botón "Ver Historial" y su Listener >>>>
        btnVerHistorial = findViewById(R.id.btnVerHistorial); // Inicialización del botón por su ID
        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(scaniar.this, HistorialActivity.class); // Crea un Intent para abrir Historial.class
            startActivity(intent); // Inicia la Activity Historial
        });
        // <<<< FIN: Inicialización del nuevo botón "Ver Historial" >>>>


        // Esto es parte de tu código original para manejar insets.
        // Mantenlo solo si sabes que lo necesitas y funciona correctamente.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Metodo para manejar el resultado del escaneo.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // Si el escaneo fue cancelado, mostrar un mensaje
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                // Obtener el contenido del código escaneado
                String contenido = result.getContents();
                txtResultado.setText(contenido);

                // Llamada a la función para guardar el escaneo en la API
                saveScanResultToApi(contenido);

                // Si el contenido es un enlace, abrirlo automáticamente en el navegador
                if (contenido.startsWith("http://") || contenido.startsWith("https://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(contenido));
                    startActivity(intent);
                } else {
                    // Si no es un enlace, solo mostrar el resultado en un Toast
                    Toast.makeText(this, "Resultado: " + contenido, Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Metodo para copiar el texto del EditText al portapapeles
    private void copiarTexto() {
        String texto = txtResultado.getText().toString(); // Obtener el texto del campo
        if (!texto.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Texto QR", texto);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Texto Copiado ", Toast.LENGTH_SHORT).show();
        }
    }

    // Nuevo método para guardar el resultado del escaneo en la API
    private void saveScanResultToApi(String scannedData) {
        long currentTimestamp = System.currentTimeMillis() / 1000; // UNIX timestamp en segundos
        ScanResult newScan = new ScanResult(scannedData, currentTimestamp);

        apiService.saveScanResult(newScan).enqueue(new Callback<ScanResult>() {
            @Override
            public void onResponse(Call<ScanResult> call, Response<ScanResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ScanResult savedScan = response.body();
                    Log.d("ScaniarAty", "Escaneo guardado correctamente! ID: " + savedScan.getId() + ", Data: " + savedScan.getData());
                } else {
                    String errorMsg = "Error al guardar escaneo: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("ScaniarAty", "Error al leer errorBody", e);
                    }
                    Log.e("ScaniarAty", errorMsg);
                    Toast.makeText(scaniar.this, "Error al guardar el escaneo: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ScanResult> call, Throwable t) {
                Log.e("ScaniarAty", "Error de conexión al guardar escaneo: " + t.getMessage(), t);
                Toast.makeText(scaniar.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}