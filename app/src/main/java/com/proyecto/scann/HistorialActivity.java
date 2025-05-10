package com.proyecto.scann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable; // <<<< Nueva importación
import android.text.TextWatcher; // <<<< Nueva importación
import android.util.Log;
import android.view.View;
import android.widget.EditText; // <<<< Nueva importación
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.proyecto.scann.Interface.ApiService;
import com.proyecto.scann.modelos.ScanResult;
import com.proyecto.scann.adapters.ScanResultAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // <<<< Nueva importación para Locale en filtro
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HistorialActivity extends AppCompatActivity implements ScanResultAdapter.OnItemDeleteListener {

    private static final String BASE_URL = "https://67dac53435c87309f52df40a.mockapi.io/";

    private RecyclerView recyclerView;
    private ScanResultAdapter adapter;
    private List<ScanResult> scanResultList; // Lista que se muestra y se filtra
    private List<ScanResult> originalScanResultList; // <<<< NUEVO: Copia de la lista original sin filtrar
    private ApiService apiService;
    private TextView tvNoScansMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etSearch; // <<<< NUEVO: Declaración del EditText de búsqueda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historial);

        recyclerView = findViewById(R.id.recyclerViewScanHistory);
        tvNoScansMessage = findViewById(R.id.tv_no_scans_message);
        etSearch = findViewById(R.id.etSearch); // <<<< NUEVO: Inicialización del EditText

        // --- Configuración del RecyclerView ---
        scanResultList = new ArrayList<>();
        originalScanResultList = new ArrayList<>(); // Inicializar la lista original
        adapter = new ScanResultAdapter(scanResultList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Inicialización del SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchScanHistory();
        });

        // <<<< INICIO: Configuración del TextWatcher para la búsqueda >>>>
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No es necesario implementar para esta funcionalidad
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cuando el texto cambia, filtramos la lista
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No es necesario implementar para esta funcionalidad
            }
        });
        // <<<< FIN: Configuración del TextWatcher >>>>


        // --- Inicialización de Retrofit y ApiService ---
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);

        fetchScanHistory(); // Cargar el historial al inicio
    }

    private void fetchScanHistory() {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getScanHistory().enqueue(new Callback<List<ScanResult>>() {
            @Override
            public void onResponse(Call<List<ScanResult>> call, Response<List<ScanResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ScanResult> fetchedScans = response.body();
                    Log.d("HistorialAty", "Historial cargado correctamente. Cantidad: " + fetchedScans.size());

                    originalScanResultList.clear(); // Limpiar la lista original antes de añadir nuevos datos
                    originalScanResultList.addAll(fetchedScans); // Guardar la lista completa

                    // Aplicar el filtro actual (si hay texto en la barra de búsqueda)
                    filterList(etSearch.getText().toString());

                } else {
                    String errorMsg = "Error al cargar historial: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("HistorialAty", "Error al leer errorBody", e);
                    }
                    Log.e("HistorialAty", errorMsg);
                    Toast.makeText(HistorialActivity.this, "Error al cargar historial: " + response.code(), Toast.LENGTH_LONG).show();
                    tvNoScansMessage.setVisibility(View.VISIBLE);
                    tvNoScansMessage.setText("Error al cargar historial: " + response.code() + "\nIntenta de nuevo.");
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<ScanResult>> call, Throwable t) {
                Log.e("HistorialAty", "Error de conexión al cargar historial: " + t.getMessage(), t);
                Toast.makeText(HistorialActivity.this, "Error de conexión al cargar historial: " + t.getMessage(), Toast.LENGTH_LONG).show();
                tvNoScansMessage.setVisibility(View.VISIBLE);
                tvNoScansMessage.setText("Error de conexión: " + t.getMessage() + "\nVerifica tu conexión a internet.");
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // <<<< NUEVO MÉTODO: Para filtrar la lista de escaneos >>>>
    private void filterList(String searchText) {
        List<ScanResult> filteredList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredList.addAll(originalScanResultList); // Si el texto está vacío, mostrar la lista original
        } else {
            String lowerCaseSearchText = searchText.toLowerCase(Locale.getDefault());
            for (ScanResult scan : originalScanResultList) {
                // Filtra por los datos del escaneo o cualquier otro campo relevante
                if (scan.getData().toLowerCase(Locale.getDefault()).contains(lowerCaseSearchText)) {
                    filteredList.add(scan);
                }
            }
        }

        scanResultList.clear();
        scanResultList.addAll(filteredList);
        adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado

        // Actualizar mensaje de "No hay escaneos" si la lista filtrada está vacía
        if (scanResultList.isEmpty()) {
            tvNoScansMessage.setVisibility(View.VISIBLE);
            tvNoScansMessage.setText("No se encontraron resultados para '" + searchText + "'.");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoScansMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDeleteClick(int position, String scanId) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Escaneo")
                .setMessage("¿Estás seguro de que quieres eliminar este escaneo?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    deleteScanResult(position, scanId);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteScanResult(final int position, String scanId) {
        Toast.makeText(this, "Eliminando escaneo...", Toast.LENGTH_SHORT).show();
        Log.d("HistorialAty", "Intentando eliminar escaneo con ID: " + scanId + " en posición: " + position);

        apiService.deleteScanResult(scanId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("HistorialAty", "Escaneo eliminado correctamente del servidor: " + scanId);
                    Toast.makeText(HistorialActivity.this, "Escaneo eliminado.", Toast.LENGTH_SHORT).show();

                    // Eliminar también de la lista original y luego volver a filtrar
                    // Primero, encontrar el elemento original para eliminarlo de originalScanResultList
                    ScanResult removedScan = null;
                    for (ScanResult scan : originalScanResultList) {
                        if (scan.getId().equals(scanId)) {
                            removedScan = scan;
                            break;
                        }
                    }
                    if (removedScan != null) {
                        originalScanResultList.remove(removedScan);
                    }

                    // Volver a filtrar la lista visible para que el elemento desaparezca inmediatamente
                    filterList(etSearch.getText().toString());

                    // Actualizar mensaje si la lista (filtrada o no) queda vacía
                    if (scanResultList.isEmpty()) {
                        tvNoScansMessage.setVisibility(View.VISIBLE);
                        tvNoScansMessage.setText("No hay escaneos guardados aún."); // Restaurar mensaje original si queda vacío
                        recyclerView.setVisibility(View.GONE);
                    }

                } else {
                    String errorMsg = "Error al eliminar escaneo: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("HistorialAty", "Error al leer errorBody al eliminar", e);
                    }
                    Log.e("HistorialAty", errorMsg);
                    Toast.makeText(HistorialActivity.this, "Error al eliminar: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("HistorialAty", "Error de conexión al eliminar escaneo: " + t.getMessage(), t);
                Toast.makeText(HistorialActivity.this, "Error de conexión al eliminar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}