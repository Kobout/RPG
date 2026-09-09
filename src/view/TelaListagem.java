package view;

import modelo.Personagem;
import modelo.RepositorioPersonagens;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

public class TelaListagem extends JFrame {

    public TelaListagem() {
        setTitle("Personagens Cadastrados");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTextArea areaSaida = new JTextArea();
        areaSaida.setEditable(false);

        StringBuilder texto = new StringBuilder("Os personagens cadastrados são:\n\n");
        for (Personagem personagem : RepositorioPersonagens.listar()) {
            texto.append(personagem.toString()).append("\n\n");
        }
        areaSaida.setText(texto.toString());

        add(new JScrollPane(areaSaida), BorderLayout.CENTER);
    }
}