package view;

import modelo.ConfiguracoesJogo;
import modelo.RepositorioHerois;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.GridLayout;

public class TelaConfiguracoes extends JFrame {

    private JButton btnSom;
    private JButton btnMusica;

    public TelaConfiguracoes() {
        setTitle("Configurações");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(360, 220);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        add(new JLabel("Efeitos sonoros:", JLabel.CENTER));
        btnSom = new JButton();
        atualizarTextoSom();
        btnSom.addActionListener(e -> alternarSom());
        add(btnSom);

        add(new JLabel("Trilha sonora:", JLabel.CENTER));
        btnMusica = new JButton();
        atualizarTextoMusica();
        btnMusica.addActionListener(e -> alternarMusica());
        add(btnMusica);
    }

    private void alternarSom() {
        Som.setSilenciado(!Som.isSilenciado());
        atualizarTextoSom();
        RepositorioHerois.salvar();
    }

    private void alternarMusica() {
        ConfiguracoesJogo.setMusicaSilenciada(!ConfiguracoesJogo.isMusicaSilenciada());
        Musica.reaplicarConfiguracao();
        atualizarTextoMusica();
        RepositorioHerois.salvar();
    }

    private void atualizarTextoSom() {
        btnSom.setText(Som.isSilenciado()
                ? "Som: Desligado (clique para ligar)"
                : "Som: Ligado (clique para desligar)");
    }

    private void atualizarTextoMusica() {
        btnMusica.setText(ConfiguracoesJogo.isMusicaSilenciada()
                ? "Música: Desligada (clique para ligar)"
                : "Música: Ligada (clique para desligar)");
    }
}