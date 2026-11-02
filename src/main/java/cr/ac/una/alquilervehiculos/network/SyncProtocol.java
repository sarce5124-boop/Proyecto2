
package cr.ac.una.alquilervehiculos.network;

/**
 * Define y valida los mensajes de sincronización.
 * Formato: CHANGE|ENTIDAD|ACCION|idInstancia
 */
public class SyncProtocol {

    private static final String PREFIJO = "CHANGE";


    public static String crearMensaje(CambioEvento evento) {
        if (evento == null || !esCampoValido(evento.entidad())
                || !esCampoValido(evento.accion())
                || !esCampoValido(evento.idInstancia())) {
            throw new IllegalArgumentException("Evento de sincronización inválido.");
        }

        return PREFIJO + "|" + evento.entidad()
                + "|" + evento.accion()
                + "|" + evento.idInstancia();
    }


    public static CambioEvento interpretarMensaje(String mensaje) {
        if (mensaje == null || mensaje.length() > 200) {
            return null;
        }

        String[] partes = mensaje.split("\\|", -1);

        if (partes.length != 4 || !PREFIJO.equals(partes[0])) {
            return null;
        }

        for (int i = 1; i < partes.length; i++) {
            if (!esCampoValido(partes[i])) {
                return null;
            }
        }

        return new CambioEvento(partes[1], partes[2], partes[3]);
    }


    private static boolean esCampoValido(String campo) {
        return campo != null
                && campo.matches("[a-zA-Z0-9_-]{1,50}");
    }
}
