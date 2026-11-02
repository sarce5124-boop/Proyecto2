
package cr.ac.una.alquilervehiculos.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SyncClient implements AutoCloseable {

    private final String host;
    private final int puerto;
    private final Consumer<CambioEvento> alRecibir;

    private Socket socket;
    private PrintWriter salida;
    private volatile boolean activo;

    public SyncClient(String host, int puerto,
                      Consumer<CambioEvento> alRecibir) {
        this.host = host;
        this.puerto = puerto;
        this.alRecibir = alRecibir;
    }

    // Conecta esta instancia con el servidor.
    public synchronized void conectar() throws IOException {
        if (activo) {
            return;
        }

        Socket nuevoSocket = new Socket();
        nuevoSocket.connect(new InetSocketAddress(host, puerto), 3000);

        socket = nuevoSocket;
        salida = new PrintWriter(
                nuevoSocket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
        );
        activo = true;

        Thread hilo = new Thread(
                () -> escuchar(nuevoSocket),
                "sync-escuchar"
        );
        hilo.setDaemon(true);
        hilo.start();
    }

    // Escucha los avisos recibidos del servidor.
    private void escuchar(Socket conexion) {
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(
                        conexion.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String mensaje;

            while (activo && (mensaje = entrada.readLine()) != null) {
                CambioEvento evento =
                        SyncProtocol.interpretarMensaje(mensaje);

                if (evento != null) {
                    alRecibir.accept(evento);
                }
            }

        } catch (IOException e) {
            if (activo) {
                System.err.println(
                        "Conexión de sincronización perdida: "
                                + e.getMessage()
                );
            }
        } finally {
            activo = false;
        }
    }

    // Envía un aviso al servidor.
    public synchronized boolean enviar(CambioEvento evento) {
        if (!activo || salida == null) {
            return false;
        }

        salida.println(SyncProtocol.crearMensaje(evento));
        return !salida.checkError();
    }

    // Indica si el cliente está conectado.
    public boolean estaConectado() {
        return activo && socket != null && !socket.isClosed();
    }

    // Cierra la conexión.
    @Override
    public synchronized void close() {
        activo = false;

        if (salida != null) {
            salida.close();
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
