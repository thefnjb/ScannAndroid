package com.proyecto.scann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HistorialActivity extends AppCompatActivity implements
        ScanResultAdapter.OnItemDeleteListener,
        ScanResultAdapter.OnItemCopyListener,
        ScanResultAdapter.OnItemClickListener {

    private static final String BASE_URL = "https://67dac53435c87309f52df40a.mockapi.io/";

    private RecyclerView recyclerView;
    private ScanResultAdapter adapter;
    private List<ScanResult> scanResultList;
    private List<ScanResult> originalScanResultList;
    private ApiService apiService;
    private TextView tvNoScansMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historial);

        recyclerView = findViewById(R.id.recyclerViewScanHistory);
        tvNoScansMessage = findViewById(R.id.tv_no_scans_message);
        etSearch = findViewById(R.id.etSearch);

        scanResultList = new ArrayList<>();
        originalScanResultList = new ArrayList<>();
        adapter = new ScanResultAdapter(scanResultList, this, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchScanHistory);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiService = retrofit.create(ApiService.class);
        fetchScanHistory();
    }

    private void fetchScanHistory() {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getScanHistory().enqueue(new Callback<List<ScanResult>>() {
            @Override
            public void onResponse(Call<List<ScanResult>> call, Response<List<ScanResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalScanResultList.clear();
                    originalScanResultList.addAll(response.body());
                    filterList(etSearch.getText().toString());
                } else {
                    mostrarError("Error al cargar historial: " + response.code());
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<ScanResult>> call, Throwable t) {
                mostrarError("Error de conexión: " + t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void filterList(String searchText) {
        List<ScanResult> filteredList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredList.addAll(originalScanResultList);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase(Locale.getDefault());
            for (ScanResult scan : originalScanResultList) {
                if (scan.getData().toLowerCase(Locale.getDefault()).contains(lowerCaseSearchText)) {
                    filteredList.add(scan);
                }
            }
        }

        scanResultList.clear();
        scanResultList.addAll(filteredList);
        adapter.notifyDataSetChanged();

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
                .setPositiveButton("Sí, eliminar", (dialog, which) -> deleteScanResult(position, scanId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteScanResult(final int position, String scanId) {
        Toast.makeText(this, "Eliminando escaneo...", Toast.LENGTH_SHORT).show();
        apiService.deleteScanResult(scanId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
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
                    filterList(etSearch.getText().toString());
                    if (scanResultList.isEmpty()) {
                        tvNoScansMessage.setVisibility(View.VISIBLE);
                        tvNoScansMessage.setText("No hay escaneos guardados aún.");
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    mostrarError("Error al eliminar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mostrarError("Error de conexión al eliminar: " + t.getMessage());
            }
        });
    }

    private void mostrarError(String mensaje) {
        Log.e("HistorialActivity", mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        tvNoScansMessage.setVisibility(View.VISIBLE);
        tvNoScansMessage.setText(mensaje + "\nVerifica tu conexión a internet.");
    }

    // Copiar texto al portapapeles
    @Override
    public void onCopyClick(String scanData) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Escaneo", scanData);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show();
    }

    // Abrir enlaces si es URL válida
    @Override
    public void onItemClick(String scanData) {
        if (Patterns.WEB_URL.matcher(scanData).matches()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanData));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "No es un enlace válido", Toast.LENGTH_SHORT).show();
        }
    }
}
