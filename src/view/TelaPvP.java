package view;

import modelo.Clerigo;
import modelo.Mago;
import modelo.Personagem;
import modelo.RepositorioHerois;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

// Duelo local: dois heróis se enfrentam alternando turnos na mesma máquina.
// É só por diversão - não afeta nível, itens, poções nem o progresso salvo.
// Obs.: o Mago sempre ataca com a arma normal aqui (sem escolha de feitiço),
// para manter esta tela simples.
public class TelaPvP extends JFrame {

    private JComboBox<Personagem> comboJogador1;
    private JComboBox<Personagem> comboJogador2;
    private JButton btnIniciar;
    private JButton btnAtacar;
    private JTextArea areaLog;
    private JProgressBar barraVida1;
    private JProgressBar barraVida2;

    private Personagem jogador1;
    private Personagem jogador2;
    private boolean turnoJogador1;
    private boolean duelEmAndamento;

    public TelaPvP() {
        setTitle("Duelo entre Heróis (PvP local)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                Musica.tocar("menu.wav");
            }
        });
        Musica.tocar("batalha.wav");

        List<Personagem> herois = RepositorioHerois.listar();

        JPanel painelTopo = new JPanel(new GridLayout(3, 2, 5, 5));
        comboJogador1 = new JComboBox<>(herois.toArray(new Personagem[0]));
        comboJogador2 = new JComboBox<>(herois.toArray(new Personagem[0]));
        if (herois.size() > 1) {
            comboJogador2.setSelectedIndex(1);
        }
        painelTopo.add(new JLabel("Jogador 1:"));
        painelTopo.add(comboJogador1);
        painelTopo.add(new JLabel("Jogador 2:"));
        painelTopo.add(comboJogador2);

        btnIniciar = new JButton("Iniciar Duelo");
        btnIniciar.addActionListener(e -> iniciarDuelo());
        painelTopo.add(new JLabel());
        painelTopo.add(btnIniciar);

        add(painelTopo, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        add(new JScrollPane(areaLog), BorderLayout.CENTER);

        JPanel painelInferior = new JPanel(new BorderLayout(5, 5));
        JPanel painelVidas = new JPanel(new GridLayout(1, 2, 10, 0));
        barraVida1 = new JProgressBar(0, 1);
        barraVida1.setStringPainted(true);
        barraVida2 = new JProgressBar(0, 1);
        barraVida2.setStringPainted(true);
        painelVidas.add(barraVida1);
        painelVidas.add(barraVida2);
        painelInferior.add(painelVidas, BorderLayout.NORTH);

        btnAtacar = new JButton("Atacar (jogador da vez)");
        btnAtacar.setEnabled(false);
        btnAtacar.addActionListener(e -> proximoGolpe());
        painelInferior.add(btnAtacar, BorderLayout.SOUTH);

        add(painelInferior, BorderLayout.SOUTH);
    }

    private void iniciarDuelo() {
        jogador1 = (Personagem) comboJogador1.getSelectedItem();
        jogador2 = (Personagem) comboJogador2.getSelectedItem();

        if (jogador1 == null || jogador2 == null || jogador1 == jogador2) {
            JOptionPane.showMessageDialog(this, "Escolha dois heróis diferentes.");
            return;
        }

        curarParaODuelo(jogador1);
        curarParaODuelo(jogador2);

        turnoJogador1 = true;
        duelEmAndamento = true;
        areaLog.setText("Duelo entre " + jogador1.getNome() + " e " + jogador2.getNome() + " começou!\n\n");
        atualizarVidas();

        btnAtacar.setEnabled(true);
        btnIniciar.setEnabled(false);
        comboJogador1.setEnabled(false);
        comboJogador2.setEnabled(false);
    }

    private void proximoGolpe() {
        if (!duelEmAndamento) {
            return;
        }

        Personagem atacante = turnoJogador1 ? jogador1 : jogador2;
        Personagem alvo = turnoJogador1 ? jogador2 : jogador1;

        String resultado = atacante.atacar(alvo);
        areaLog.append(resultado + "\n");
        atualizarVidas();

        if (!alvo.estaVivo()) {
            areaLog.append("\n" + alvo.getNome() + " foi derrotado! " + atacante.getNome() + " venceu o duelo!\n");
            finalizarDuelo();
            return;
        }

        turnoJogador1 = !turnoJogador1;
    }

    private void atualizarVidas() {
        barraVida1.setMaximum(jogador1.getVidaMaxima());
        barraVida1.setValue(jogador1.getVida());
        barraVida1.setString(jogador1.getNome() + " - " + jogador1.getVida() + "/" + jogador1.getVidaMaxima());

        barraVida2.setMaximum(jogador2.getVidaMaxima());
        barraVida2.setValue(jogador2.getVida());
        barraVida2.setString(jogador2.getNome() + " - " + jogador2.getVida() + "/" + jogador2.getVidaMaxima());
    }

    private void finalizarDuelo() {
        duelEmAndamento = false;
        btnAtacar.setEnabled(false);
        btnIniciar.setEnabled(true);
        comboJogador1.setEnabled(true);
        comboJogador2.setEnabled(true);

        // Restaura os dois por completo: o duelo não deixa marcas no progresso salvo
        curarParaODuelo(jogador1);
        curarParaODuelo(jogador2);
    }

    private void curarParaODuelo(Personagem p) {
        p.curarTotalmente();
        if (p instanceof Mago) {
            ((Mago) p).restaurarManaTotal();
        } else if (p instanceof Clerigo) {
            ((Clerigo) p).restaurarFeTotal();
        }
    }
}