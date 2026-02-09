package civisEconomy;

public class Transaccion {
    private final String idTransaccion; // UUID o formato similar para garantizar unicidad
    private final CuentaBancaria origen;
    private final CuentaBancaria destino;
    private final Double monto;
    
    public Transaccion(CuentaBancaria origen, CuentaBancaria destino, Double monto) {
        this.idTransaccion = generarIdUnico();
        this.origen = origen;
        this.destino = destino;
        this.monto = monto;
    }
    
    public String getIdTransaccion() {
        return idTransaccion;
    }

    public CuentaBancaria getOrigen() {
        return origen;
    }
    
    public CuentaBancaria getDestino() {
        return destino;
    }
    
    public Double getMonto() {
        return monto;
    }

    private String generarIdUnico() {
        return java.util.UUID.randomUUID().toString();
    }
}