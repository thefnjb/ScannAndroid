package com.proyecto.scann.Interface;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService
{
    @GET("Usuari") // MockAPI devuelve la lista de usuarios
    Call<List<LoginResponse>> getUsers();
    @POST("Usuari") // Registrar un nuevo usuario
    Call<LoginResponse> registrarUsuario(@Body LoginResponse usuari);
}
