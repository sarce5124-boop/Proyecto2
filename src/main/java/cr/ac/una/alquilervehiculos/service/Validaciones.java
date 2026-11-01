package cr.ac.una.alquilervehiculos.service;

/** Validaciones sencillas para los datos ingresados en los formularios. */
public final class Validaciones {

    private static final double MAXIMO_MONTO = 9_999_999_999.99;

    private Validaciones() {
    }

    public static String textoObligatorio(String valor, String nombre, int maximo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar " + nombre + ".");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(nombre + " no puede superar " + maximo + " caracteres.");
        }
        if (limpio.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Ingrese un valor válido para " + nombre + ".");
        }
        return limpio;
    }

    public static String identificacion(String valor) {
        String limpio = textoObligatorio(valor, "la identificación", 30);
        if (!limpio.matches("[\\p{L}\\p{N}][\\p{L}\\p{N} -]*")) {
            throw new IllegalArgumentException("La identificación solo puede contener letras, números, espacios y guiones.");
        }
        return limpio;
    }

    public static String nombre(String valor) {
        String limpio = textoObligatorio(valor, "el nombre", 120);
        if (!limpio.matches("[\\p{L}][\\p{L} .'-]*")) {
            throw new IllegalArgumentException("El nombre solo puede contener letras, espacios, puntos, apóstrofes y guiones.");
        }
        return limpio;
    }

    public static String telefono(String valor) {
        if (valor == null || valor.trim().isEmpty()) return "";
        String limpio = valor.trim();
        if (limpio.length() > 30 || !limpio.matches("\\+?[0-9() -]+")) {
            throw new IllegalArgumentException("Ingrese un teléfono válido, por ejemplo 8888-8888 o +506 8888-8888.");
        }
        String digitos = limpio.replaceAll("\\D", "");
        if (digitos.length() < 8 || digitos.length() > 15) {
            throw new IllegalArgumentException("El teléfono debe tener entre 8 y 15 dígitos.");
        }
        return limpio;
    }

    public static String correo(String valor) {
        if (valor == null || valor.trim().isEmpty()) return "";
        String limpio = valor.trim();
        if (limpio.length() > 150 || !limpio.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}")) {
            throw new IllegalArgumentException("Ingrese un correo válido, por ejemplo usuario@correo.com.");
        }
        return limpio;
    }

    public static String placa(String valor) {
        String limpio = textoObligatorio(valor, "la placa", 20);
        if (!limpio.matches("[\\p{L}\\p{N}][\\p{L}\\p{N} -]*")) {
            throw new IllegalArgumentException("La placa solo puede contener letras, números, espacios y guiones.");
        }
        return limpio;
    }

    public static String marca(String valor) {
        return textoObligatorio(valor, "la marca", 100);
    }

    public static String tipoMotor(String valor) {
        return textoObligatorio(valor, "el tipo de motor", 80);
    }

    public static String tipoTraccion(String valor) {
        return textoObligatorio(valor, "el tipo de tracción", 80);
    }

    public static void montoValido(double monto, String campo) {
        if (!Double.isFinite(monto) || monto <= 0 || monto > MAXIMO_MONTO) {
            throw new IllegalArgumentException(campo + " debe ser mayor que cero y no superar ₡9.999.999.999,99.");
        }
    }

    public static double convertirMonto(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar " + campo + ".");
        }
        String limpio = texto.trim();
        if (!limpio.matches("[0-9]+(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Ingrese un monto válido para " + campo + " (máximo dos decimales, usando punto).");
        }
        double monto;
        try {
            monto = Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingrese un monto válido para " + campo + ".");
        }
        montoValido(monto, campo);
        return monto;
    }
}
