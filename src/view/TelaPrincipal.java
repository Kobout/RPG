package view;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("RPG - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(320, 340);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 10, 10));

        JButton btnCadastrar = new JButton("Cadastrar Personagem");
        JButton btnListar = new JButton("Listar Personagens");
        JButton btnBatalhar = new JButton("Batalhar");
        JButton btnInventario = new JButton("Inventário");
        JButton btnLoja = new JButton("Loja");
        JButton btnPvP = new JButton("Duelo entre Heróis (PvP)");

        btnCadastrar.addActionListener(e -> new TelaCadastro().setVisible(true));
        btnListar.addActionListener(e -> new TelaListagem().setVisible(true));
        btnBatalhar.addActionListener(e -> new TelaBatalha().setVisible(true));
        btnInventario.addActionListener(e -> new TelaInventario().setVisible(true));
        btnLoja.addActionListener(e -> new TelaLoja().setVisible(true));
        btnPvP.addActionListener(e -> new TelaPvP().setVisible(true));

        add(btnCadastrar);
        add(btnListar);
        add(btnBatalhar);
        add(btnInventario);
        add(btnLoja);
        add(btnPvP);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}