package civisGeo;

public class TipoVia {

    // Enum or String constants could be used, but database generally uses strings.
    // Ideally map 'CL' -> 'Calle', 'AV' -> 'Avenida', etc.

    public static String getNombreCompleto(String tipoCorto) {
        if (tipoCorto == null)
            return "";
        switch (tipoCorto.toUpperCase()) {
            case "CL":
                return "Calle";
            case "AV":
                return "Avenida";
            case "PZ":
                return "Plaza";
            case "CM":
                return "Camino";
            case "PS":
                return "Paseo";
            case "TR":
                return "Travesía";
            case "RB":
                return "Rambla";
            // Add more mappings based on census data if available
            default:
                return tipoCorto;
        }
    }
}
