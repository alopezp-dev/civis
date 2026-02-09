package civisEconomy;

public class Transaccion {
    private final CuentaBancaria origen;
    private final CuentaBancaria destino;
    private final Double monto;
    
    public Transaccion(CuentaBancaria origen, CuentaBancaria destino, Double monto) {
        this.origen = origen;
        this.destino = destino;
        this.monto = monto;
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
}