package civisEconomy;

import civisGeo.Direccion;
import civisGeo.Localidad;

public class Sucursal {
    private final String codigoSucursal; // Ej: "1234"
    private EntidadBancaria banco;    // Relación con EntidadBancaria
    private Localidad municipio;      // Relación con Localidad
    private Edificio direccion;      // Relación con el módulo URB (Calle y número)
    private Integer cajeros;   // Servicio disponible 24h

    public Sucursal(EntidadBancaria banco, Localidad municipio, Direccion direccion) {
        this.banco = banco;
        this.municipio = municipio;
        this.direccion = direccion;
    }
}