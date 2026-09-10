package view;

import modelo.Dificuldade;
import modelo.ProgressoDificuldade;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridLayout;

public class TelaDificuldade extends JFrame {

    private JLabel lblAtual;

    public TelaDificuldade() {
        setTitle("Escolher Dificuldade");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 5, 5));

        lblAtual = new JLabel(" ", JLabel.CENTER);
        add(lblAtual);

        for (Dificuldade dificuldade : Dificuldade.values()) {
            add(criarBotao(dificuldade));
        }

        atualizarLabelAtual();
    }

    private JButton criarBotao(Dificuldade dificuldade) {
        boolean desbloqueada = ProgressoDificuldade.estaDesbloqueada(dificuldade);
        String texto = dificuldade.getNomeExibicao() + (desbloqueada ? "" : " 🔒 (bloqueada)");

        JButton botao = new JButton(texto);
        botao.setEnabled(desbloqueada);
        botao.addActionListener(e -> {
            ProgressoDificuldade.selecionar(dificuldade);
            atualizarLabelAtual();
            JOptionPane.showMessageDialog(this,
                    "Dificuldade alterada para: " + dificuldade.getNomeExibicao());
        });
        return botao;
    }

    private void atualizarLabelAtual() {
        Dificuldade atual = ProgressoDificuldade.getAtual();
        lblAtual.setText("Dificuldade atual: " + atual.getNomeExibicao());
        lblAtual.setForeground(new Color(30, 90, 160));

        int derrotados = ProgressoDificuldade.getChefoesDerrotadosNaDificuldadeAtual();
        if (atual != Dificuldade.values()[Dificuldade.values().length - 1]) {
            lblAtual.setText(lblAtual.getText() + "  (" + derrotados + "/5 chefões nesta dificuldade)");
        }
    }
}