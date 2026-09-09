package view;

import modelo.Arqueiro;
import modelo.Guerreiro;
import modelo.Mago;
import modelo.Personagem;
import modelo.RepositorioPersonagens;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class TelaCadastro extends JFrame {

    private JTextField txtNome;
    private JTextField txtAtaque;
    private JTextField txtVida;
    private JTextField txtDefesa;
    private JTextField txtEspecifico;
    private JComboBox<String> comboTipo;
    private JLabel lblEspecifico;

    public TelaCadastro() {
        setTitle("Cadastro de Personagem");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(350, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 5, 5));

        add(new JLabel("Tipo:"));
        comboTipo = new JComboBox<>(new String[]{"Guerreiro", "Mago", "Arqueiro"});
        comboTipo.addActionListener(e -> atualizarCampoEspecifico());
        add(comboTipo);

        add(new JLabel("Nome:"));
        txtNome = new JTextField();
        add(txtNome);

        add(new JLabel("Ataque:"));
        txtAtaque = new JTextField();
        add(txtAtaque);

        add(new JLabel("Vida:"));
        txtVida = new JTextField();
        add(txtVida);

        add(new JLabel("Defesa:"));
        txtDefesa = new JTextField();
        add(txtDefesa);

        lblEspecifico = new JLabel("Arma:");
        add(lblEspecifico);
        txtEspecifico = new JTextField();
        add(txtEspecifico);

        JButton btnCadastrar = new JButton("Cadastrar");
        btnCadastrar.addActionListener(e -> cadastrar());
        add(new JLabel());
        add(btnCadastrar);
    }

    private void atualizarCampoEspecifico() {
        String tipo = (String) comboTipo.getSelectedItem();
        switch (tipo) {
            case "Guerreiro":
                lblEspecifico.setText("Arma:");
                break;
            case "Mago":
                lblEspecifico.setText("Mana:");
                break;
            case "Arqueiro":
                lblEspecifico.setText("Alcance:");
                break;
        }
        txtEspecifico.setText("");
    }

    private void cadastrar() {
        try {
            String nome = txtNome.getText().trim();
            int ataque = Integer.parseInt(txtAtaque.getText().trim());
            int vida = Integer.parseInt(txtVida.getText().trim());
            int defesa = Integer.parseInt(txtDefesa.getText().trim());
            String tipo = (String) comboTipo.getSelectedItem();

            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe o nome do personagem.");
                return;
            }

            Personagem novoPersonagem;

            switch (tipo) {
                case "Guerreiro":
                    String arma = txtEspecifico.getText().trim();
                    novoPersonagem = new Guerreiro(nome, arma, ataque, vida, defesa);
                    break;
                case "Mago":
                    int mana = Integer.parseInt(txtEspecifico.getText().trim());
                    novoPersonagem = new Mago(nome, mana, ataque, vida, defesa);
                    break;
                case "Arqueiro":
                    int alcance = Integer.parseInt(txtEspecifico.getText().trim());
                    novoPersonagem = new Arqueiro(nome, alcance, ataque, vida, defesa);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Escolha um tipo de personagem.");
                    return;
            }

            RepositorioPersonagens.adicionar(novoPersonagem);
            JOptionPane.showMessageDialog(this, "Personagem cadastrado com sucesso!");
            limparCampos();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ataque, Vida, Defesa e o campo específico devem ser números.",
                    "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtAtaque.setText("");
        txtVida.setText("");
        txtDefesa.setText("");
        txtEspecifico.setText("");
    }
}