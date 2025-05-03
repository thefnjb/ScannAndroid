package com.proyecto.scann.Interface;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("correo")
    private String correo;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("usuario")
    private String usuario;

    @SerializedName("pasword")
    private String pasword;

    public LoginResponse(String nombre, String correo, String telefono, String usuario, String pasword) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.usuario = usuario;
        this.pasword = pasword;
    }

    public String getCorreo() {
        return correo;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPasword() {
        return pasword;
    }
}
