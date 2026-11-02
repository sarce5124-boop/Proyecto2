
package cr.ac.una.alquilervehiculos.network;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SyncManager implements AutoCloseable {

    private static final int PUERTO = 5051;
    private static final String HOST = "127.0.0.1";

    private final String idInstancia =
            UUID.randomUUID().toString();

    private final Consumer<CambioEvento> alRecibir;
    private final AtomicBoolean activo = new AtomicBoolean(false);

    private volatile SyncServer servidor;
    private volatile SyncClient cliente;

    public SyncManager(Consumer<CambioEvento> alRecibir) {
        this.alRecibir = alRecibir;
    }

    // Inicia la sincronización en segundo plano.
    public void iniciar() {
        if (!activo.compareAndSet(false, true)) {
            return;
        }

        Thread hilo = new Thread(
                this::mantenerConexion,
                "sync-manager"
        );
        hilo.setDaemon(true);
        hilo.start();
    }

    // Busca el servidor o intenta convertirse en anfitrión.
    private void mantenerConexion() {
        while (activo.get()) {
            try {
                if (cliente == null || !cliente.estaConectado()) {

                    cerrarCliente();

                    try {
                        SyncClient nuevo = new SyncClient(
                                HOST, PUERTO, this::procesarEvento
                        );
                        nuevo.conectar();
                        cliente = nuevo;

                    } catch (IOException e) {
                        iniciarServidorSiEsNecesario();

                        SyncClient nuevo = new SyncClient(
                                HOST, PUERTO, this::procesarEvento
                        );
                        nuevo.conectar();
                        cliente = nuevo;
                    }
                }

                Thread.sleep(2000);

            } catch (IOException e) {
                System.err.println(
                        "Reintentando sincronización: " + e.getMessage()
                );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        cerrarCliente();
    }

    // Intenta abrir el servidor cuando no hay anfitrión.
    private synchronized void iniciarServidorSiEsNecesario() {
        if (servidor != null) {
            return;
        }

        try {
            servidor = new SyncServer(PUERTO);
            servidor.iniciar();
            System.out.println("Servidor de sincronización iniciado.");

        } catch (IOException e) {
            servidor = null;
        }
    }

    // Procesa los cambios provenientes de otras instancias.
    private void procesarEvento(CambioEvento evento) {
        if (!idInstancia.equals(evento.idInstancia())) {
            try {
                alRecibir.accept(evento);
            } catch (RuntimeException e) {
                System.err.println(
                        "Error al procesar aviso: " + e.getMessage()
                );
            }
        }
    }

    // Notifica a las otras instancias después de guardar en MySQL.
    public void notificarCambio(String entidad, String accion) {
        SyncClient actual = cliente;

        if (actual != null && actual.estaConectado()) {
            actual.enviar(new CambioEvento(
                    entidad,
                    accion,
                    idInstancia
            ));
        }
    }

    // Cierra la conexión del cliente.
    private synchronized void cerrarCliente() {
        if (cliente != null) {
            cliente.close();
            cliente = null;
        }
    }

    // Detiene la sincronización.
    @Override
    public void close() {
        activo.set(false);
        cerrarCliente();

        if (servidor != null) {
            servidor.close();
            servidor = null;
        }
    }
}
