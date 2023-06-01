import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MemoramaCliente2 {
    private static final String IP_SERVIDOR = "127.0.0.1"; // Dirección IP del servidor
    private static final int PUERTO = 1234; // Puerto en el que el servidor escucha las conexiones

    private JFrame ventana;
    private JButton[] botonesCartas;
    private boolean juegoTerminado;
    private PrintWriter writer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MemoramaCliente2().iniciar();
            }
        });
    }

    public void iniciar() {
        ventana = new JFrame("Memorama");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(400, 400);
        ventana.setLayout(new GridLayout(4, 4));

        botonesCartas = new JButton[16];
        for (int i = 0; i < 16; i++) {
            JButton boton = new JButton();
            boton.setFont(new Font("Arial", Font.BOLD, 20));
            boton.setEnabled(false);
            botonesCartas[i] = boton;
            ventana.add(boton);
        }

        ventana.setVisible(true);

        conectarAlServidor();
        manejarEventosBotones();

        new Thread(this::iniciarJuego).start();
    }

    private void conectarAlServidor() {
        try {
            Socket socket = new Socket(IP_SERVIDOR, PUERTO);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            mostrarMensaje("Conectado al servidor.");

            String mensajeBienvenida = reader.readLine();
            mostrarMensaje(mensajeBienvenida);

            String mensajeInicioJuego = reader.readLine();
            mostrarMensaje(mensajeInicioJuego);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manejarEventosBotones() {
        for (int i = 0; i < botonesCartas.length; i++) {
            final int cartaSeleccionada = i;
            botonesCartas[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!juegoTerminado) {
                        writer.println(cartaSeleccionada);
                    }
                }
            });
        }
    }

    private void iniciarJuego() {
        try {
            Socket socket = new Socket(IP_SERVIDOR, PUERTO);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String estadoJuego;

            while (!juegoTerminado && (estadoJuego = reader.readLine()) != null) {
                actualizarEstadoJuego(estadoJuego);

                if (estadoJuego.equals("¡Juego terminado!")) {
                    juegoTerminado = true;
                }
            }

            String estadoFinalJuego = reader.readLine();
            mostrarMensaje(estadoFinalJuego);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void actualizarEstadoJuego(String estadoJuego) {
        String[] cartas = estadoJuego.split(" ");

        for (int i = 0; i < cartas.length; i++) {
            if (cartas[i].equals("*")) {
                botonesCartas[i].setText("");
                botonesCartas[i].setEnabled(true);
            } else {
                botonesCartas[i].setText(cartas[i]);
                botonesCartas[i].setEnabled(false);
            }
        }
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje);
    }
}
