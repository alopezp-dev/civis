package civisEconomy;

import java.time.LocalDate;
import java.util.*;

public class CuentaBancaria {

    private final String iban;
    private final Map<String, Cliente> titulares;
    private final LocalDate fechaApertura;

    private Double saldo;
    private Double saldoContable;
    private final String divisa;

    private Boolean cuentaActiva;
    private Double limmiteDiarioCajero;
    private List<Transaccion> movimientos;

    public CuentaBancaria(String iban, Double saldo, String divisa) {
        this.iban = iban;
        this.titulares = new HashMap<>();
        this.saldo = saldo;
        this.divisa = divisa;
        this.fechaApertura = LocalDate.now();
        this.cuentaActiva = true;
        this.limmiteDiarioCajero = 1000.0; // Ejemplo de límite diario
        this.movimientos = new ArrayList<>();
    }

    public String getNumeroCuenta() {
        return iban;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double nuevoSaldo) {
        this.saldo = nuevoSaldo;
    }

    public Map<String, Cliente> getTitulares() {
        return titulares;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    // Operaciones con la cuenta

    public void agregarTitular(Cliente cliente) {
        titulares.put(cliente.getIdCliente(), cliente);
    }

    public void eliminarTitular(String idCliente) {
        titulares.remove(idCliente);
    }

    public Boolean realizarTransferencia(CuentaBancaria destino, Double monto) {
        if (!cuentaActiva || saldo < monto) {
            return false; // No se puede realizar la transferencia
        }
        this.saldo -= monto;
        destino.setSaldo(destino.getSaldo() + monto);
        // Aquí se podrían agregar detalles de la transacción al libro mayor
        this.movimientos.add(new Transaccion(this, destino, monto));
        return true;
    }
    
}
