package rede;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ConexaoBatalha {

    public static final int PORTA_PADRAO = 5555;

    private Socket socket;
    private ServerSocket servidor;
    private ObjectOutputStream saida;
    private ObjectInputStream entrada;
    private volatile boolean conectado;

    public interface Ouvinte {
        void aoConectar();
        void aoReceber(MensagemRede mensagem);
        void aoFalhar(String motivo);
    }

    // Host: fica esperando alguém conectar. Roda numa thread separada para não travar a tela.
    public void hospedar(int porta, Ouvinte ouvinte) {
        new Thread(() -> {
            try {
                servidor = new ServerSocket(porta);
                socket = servidor.accept();
                iniciarFluxos(ouvinte);
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> ouvinte.aoFalhar(e.getMessage()));
            }
        }).start();
    }

    // Cliente: conecta a um host existente. Também roda numa thread separada.
    public void conectar(String ip, int porta, Ouvinte ouvinte) {
        new Thread(() -> {
            try {
                socket = new Socket(ip, porta);
                iniciarFluxos(ouvinte);
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> ouvinte.aoFalhar(e.getMessage()));
            }
        }).start();
    }

    private void iniciarFluxos(Ouvinte ouvinte) throws IOException {
        saida = new ObjectOutputStream(socket.getOutputStream());
        entrada = new ObjectInputStream(socket.getInputStream());
        conectado = true;

        SwingUtilities.invokeLater(ouvinte::aoConectar);

        Thread threadEscuta = new Thread(() -> {
            try {
                while (conectado) {
                    Object obj = entrada.readObject();
                    if (obj instanceof MensagemRede) {
                        MensagemRede msg = (MensagemRede) obj;
                        SwingUtilities.invokeLater(() -> ouvinte.aoReceber(msg));
                    }
                }
            } catch (Exception e) {
                if (conectado) {
                    conectado = false;
                    SwingUtilities.invokeLater(() -> ouvinte.aoFalhar("Conexão perdida com o outro jogador."));
                }
            }
        });
        threadEscuta.setDaemon(true);
        threadEscuta.start();
    }

    public void enviar(MensagemRede mensagem) {
        try {
            if (saida != null) {
                saida.writeObject(mensagem);
                saida.flush();
                saida.reset();
            }
        } catch (IOException e) {
            System.out.println("Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    public boolean isConectado() {
        return conectado;
    }

    public void fechar() {
        conectado = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        try {
            if (servidor != null) servidor.close();
        } catch (IOException ignored) {
        }
    }
}