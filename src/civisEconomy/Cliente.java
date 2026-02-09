package civisEconomy;

import civisCitizen.Persona;
import java.util.HashMap;
import java.util.Map;

public class Cliente {
    private Persona usuario;
    private String email;
    private String telefono;
    private Map<String, CuentaBancaria> cuentas; // Cuentas asociadas al cliente, clave: número de cuenta

    public Cliente(Persona usuario, String email, String telefono) {
        this.usuario = usuario;
        this.email = email;
        this.telefono = telefono;
        this.cuentas = new HashMap<>();
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public Persona getUsuario() {
        return usuario;
    }

    public String getDni() {
        return usuario.getDni();
    }

    public Map<String, CuentaBancaria> getCuentas() {
        return cuentas;
    }

    public void agregarCuenta(CuentaBancaria cuenta) {
        cuentas.put(cuenta.getNumeroCuenta(), cuenta);
    }
}
