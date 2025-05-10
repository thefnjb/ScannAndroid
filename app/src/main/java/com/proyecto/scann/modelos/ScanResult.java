package com.proyecto.scann.modelos;

import com.google.gson.annotations.SerializedName;

public class ScanResult
{
    @SerializedName("data")
    private String data;

    @SerializedName("fecha")
    private long fecha; // Usamos long para el timestamp

    @SerializedName("id")
    private String id; // El ID lo genera MockAPI

    // Constructor vacío (necesario para algunas librerías como Retrofit y Gson)
    public ScanResult() {
    }

    // Constructor para crear un objeto antes de enviarlo a la API (sin ID)
    public ScanResult(String data, long fecha) {
        this.data = data;
        this.fecha = fecha;
        // El ID no lo incluimos aquí porque lo asigna la API
    }

    // Getters (Métodos para obtener el valor de las variables)
    public String getData() {
        return data;
    }

    public long getFecha() {
        return fecha;
    }

    public String getId() {
        return id;
    }

    // Setters (Métodos para establecer el valor de las variables - útiles si necesitas modificarlos)
    public void setData(String data) {
        this.data = data;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public void setId(String id) {
        this.id = id;
    }
}
