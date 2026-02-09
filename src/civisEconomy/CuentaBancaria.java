package civisEconomy;

import java.time.LocalDate;
import java.util.*;
import java.math.BigInteger;

public class CuentaBancaria {

    private final String iban;
    private final Map<String, Cliente> titulares;
    private final LocalDate fechaApertura;

    private Double saldo;
    private Double saldoContable;
    private final String divisa;

    private Boolean cuentaActiva;
    private Double limiteDiarioCajero;
    private List<Transaccion> movimientos;

    public CuentaBancaria(String codigoBanco, String codigoSucursal, String divisa) {
        this.iban = generarIBAN(codigoBanco, codigoSucursal);
        this.titulares = new HashMap<>();
        this.saldo = 0.0;
        this.divisa = divisa;
        this.fechaApertura = LocalDate.now();
        this.cuentaActiva = true;
        this.limiteDiarioCajero = 1000.0; // Ejemplo de límite diario
        this.movimientos = new ArrayList<>();
    }

    private String generarIBAN(String codigoBanco, String codigoSucursal) {
        // 1. Generar un número de cuenta aleatorio de 10 dígitos (el CCC)
        StringBuilder numeroCuenta = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            numeroCuenta.append(random.nextInt(10));
        }
        
        // 2. Formar el Código de Cuenta Corriente (Banco + Sucursal + DC + Cuenta)
        // Para simplificar, usaremos "00" como dígitos de control internos (DC)
        String ccc = codigoBanco + codigoSucursal + "00" + numeroCuenta.toString();
        
        // 3. Calcular los dígitos de control del IBAN (Algoritmo Mod-97)
        // Se toma el código de país 'ES' -> E=14, S=28 y se añade '00' al final
        // La fórmula es: 98 - (ValorNumérico % 97)
        String valorParaCalculo = ccc + "142800"; 
        BigInteger bInt = new java.math.BigInteger(valorParaCalculo);
        int resto = bInt.remainder(java.math.BigInteger.valueOf(97)).intValue();
        int dcIban = 98 - resto;
        
        // Formatear el DC a dos dígitos (ej: 05 en vez de 5)
        String dcIbanStr = String.format("%02d", dcIban);
    
        return "ES" + dcIbanStr + ccc;
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

    public Double getSaldoContable() {
        return this.saldoContable;
    }

    public Double getLimiteDiarioCajero() {
        return this.limiteDiarioCajero;
    }

    public void setLimiteDiarioCajero(Double limite) {
        this.limiteDiarioCajero = limite;
    }

    public String getDivisa() {
        return divisa;
    }

    // Operaciones con la cuenta

    public void agregarTitular(Cliente cliente) {
        titulares.put(cliente.getDni(), cliente);
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
