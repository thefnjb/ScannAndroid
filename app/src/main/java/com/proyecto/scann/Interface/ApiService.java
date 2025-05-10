package com.proyecto.scann.Interface;

import com.proyecto.scann.modelos.ScanResult; // Asegúrate de que esta ruta sea correcta
// import com.proyecto.scann.modelos.LoginResponse; // Descomenta si necesitas esta clase

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE; // <<<< Importación necesaria
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path; // <<<< Importación necesaria
// import retrofit2.http.Field; // No usados en tu ejemplo
// import retrofit2.http.FormUrlEncoded; // No usados en tu ejemplo
// import retrofit2.http.Query; // No usados en tu ejemplo


public interface ApiService {

    // Métodos relacionados con "Usuari" (usuarios) - No modificados
    @GET("Usuari")
    Call<List<LoginResponse>> getUsers();
    @POST("Usuari")
    Call<LoginResponse> registrarUsuario(@Body LoginResponse usuari);

    // Métodos para ScanResult (con endpoint 'scans' como lo tienes actualmente)
    @POST("scans") // Endpoint para crear nuevos escaneos
    Call<ScanResult> saveScanResult(@Body ScanResult scanResult);

    @GET("scans") // Endpoint para obtener todos los escaneos
    Call<List<ScanResult>> getScanHistory();

    @DELETE("scans/{id}")
    Call<Void> deleteScanResult(@Path("id") String id);
}