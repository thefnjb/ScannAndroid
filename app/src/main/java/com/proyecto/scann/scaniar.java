package com.proyecto.scann;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class scaniar extends AppCompatActivity {
    // Declaración de los botones y el campo de texto
    Button btnScan, btnLimpiar, btnCopiar;
    EditText txtResultado;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaniar);

        // Inicialización de los componentes
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
    }
    // Ahora inicializamos los metodos .
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
}
