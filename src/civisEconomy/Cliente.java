package civisEconomy;

import CivisCitizen.Persona;

public class Cliente extends Persona {
    private String idCliente; // DNI o pasaporte
    private String email;
    private String telefono;

    public Cliente(String nombre, String apellido, String idCliente, String email, String telefono) {
        super(nombre, apellido);
        this.idCliente = idCliente;
        this.email = email;
        this.telefono = telefono;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}
