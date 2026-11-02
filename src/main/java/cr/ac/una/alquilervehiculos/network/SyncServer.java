
package cr.ac.una.alquilervehiculos.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SyncServer implements AutoCloseable {

    private final int puerto;
    private final Set<Socket> clientes = ConcurrentHashMap.newKeySet();
    private volatile boolean activo;
    private ServerSocket servidor;

    public SyncServer(int puerto) {
        this.puerto = puerto;
    }

    // Inicia el servidor sin bloquear la interfaz.
    public void iniciar() throws IOException {
        servidor = new ServerSocket(puerto);
        servidor.setReuseAddress(true);
        activo = true;

        Thread hilo = new Thread(this::aceptarClientes, "sync-servidor");
        hilo.setDaemon(true);
        hilo.start();
    }

    // Acepta conexiones de otras instancias.
    private void aceptarClientes() {
        while (activo) {
            try {
                Socket cliente = servidor.accept();
                clientes.add(cliente);

                Thread hilo = new Thread(
                        () -> atenderCliente(cliente),
                        "sync-cliente-conectado"
                );
                hilo.setDaemon(true);
                hilo.start();

            } catch (IOException e) {
                if (activo) {
                    System.err.println("Error en servidor: " + e.getMessage());
                }
            }
        }
    }

    // Recibe, valida y retransmite los avisos.
    private void atenderCliente(Socket cliente) {
        try (cliente;
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(
                             cliente.getInputStream(),
                             StandardCharsets.UTF_8))) {

            String mensaje;

            while ((mensaje = entrada.readLine()) != null) {
                CambioEvento evento =
                        SyncProtocol.interpretarMensaje(mensaje);

                if (evento != null) {
                    enviarATodos(mensaje, cliente);
                }
            }

        } catch (IOException e) {
            if (activo) {
                System.err.println("Cliente desconectado: " + e.getMessage());
            }
        } finally {
            clientes.remove(cliente);
        }
    }

    // Envía el aviso a las demás instancias conectadas.
    private void enviarATodos(String mensaje, Socket origen) {
        for (Socket cliente : clientes) {
            if (cliente != origen && !cliente.isClosed()) {
                try {
                    PrintWriter salida = new PrintWriter(
                            cliente.getOutputStream(), true,
                            StandardCharsets.UTF_8
                    );
                    salida.println(mensaje);
                } catch (IOException e) {
                    clientes.remove(cliente);
                    try {
                        cliente.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    // Cierra el servidor y las conexiones.
    @Override
    public void close() {
        activo = false;

        if (servidor != null) {
            try {
                servidor.close();
            } catch (IOException ignored) {
            }
        }

        for (Socket cliente : clientes) {
            try {
                cliente.close();
            } catch (IOException ignored) {
            }
        }

        clientes.clear();
    }
}
