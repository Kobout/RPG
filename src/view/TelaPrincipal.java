package view;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("RPG - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(340, 460);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(9, 1, 8, 8));

        JButton btnBatalhar = new JButton("Batalhar");
        JButton btnInventario = new JButton("Inventário");
        JButton btnFicha = new JButton("Ficha do Personagem");
        JButton btnLoja = new JButton("Loja");
        JButton btnPvP = new JButton("Duelo entre Heróis (PvP local)");
        JButton btnMultiplayer = new JButton("Batalha Online (com um amigo)");
        JButton btnDificuldade = new JButton("Dificuldade");
        JButton btnConquistas = new JButton("Conquistas");
        JButton btnConfiguracoes = new JButton("Configurações");

        btnBatalhar.addActionListener(e -> new TelaBatalha().setVisible(true));
        btnInventario.addActionListener(e -> new TelaInventario().setVisible(true));
        btnFicha.addActionListener(e -> new TelaFichaPersonagem().setVisible(true));
        btnLoja.addActionListener(e -> new TelaLoja().setVisible(true));
        btnPvP.addActionListener(e -> new TelaPvP().setVisible(true));
        btnMultiplayer.addActionListener(e -> new TelaMultiplayer().setVisible(true));
        btnDificuldade.addActionListener(e -> new TelaDificuldade().setVisible(true));
        btnConquistas.addActionListener(e -> new TelaConquistas().setVisible(true));
        btnConfiguracoes.addActionListener(e -> new TelaConfiguracoes().setVisible(true));

        add(btnBatalhar);
        add(btnInventario);
        add(btnFicha);
        add(btnLoja);
        add(btnPvP);
        add(btnMultiplayer);
        add(btnDificuldade);
        add(btnConquistas);
        add(btnConfiguracoes);

        Musica.tocar("menu.wav");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}
