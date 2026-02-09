package civisEconomy;

import java.util.*;

public class EntidadBancaria {
    private final String nombre;
    private final String swiftCode;
    private Double reservasLiquidas;
    private final Double encajeLegal; // % mínimo que el banco no puede tocar
    
    // Hilos seguros: varios cajeros o apps pueden acceder a la vez
    private Map<String, Cliente> clientes;
    private Map<String, CuentaBancaria> cuentas;
    private List<Transaccion> libroMayor;

    public EntidadBancaria(String nombre, String swiftCode, double encaje) {
        this.nombre = nombre;
        this.swiftCode = swiftCode;
        this.encajeLegal = 0.0; // Por defecto, el banco no tiene reservas líquidas
        this.clientes = new HashMap<>();
        this.cuentas = new HashMap<>();
        this.libroMayor = new ArrayList<>();
        this.reservasLiquidas = 0.0;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public Double getEncajeLegal() {
        return encajeLegal;
    }

    public Double getReservasLiquidas() {
        return reservasLiquidas;
    }

    // Métodos para gestionar clientes, cuentas y transacciones se agregarían aquí

    public void crearCuentaBancaria(Cliente cliente) {
        // Lógica para crear una cuenta bancaria y asociarla al cliente
        CuentaBancaria nuevaCuenta = new CuentaBancaria();
    }

}