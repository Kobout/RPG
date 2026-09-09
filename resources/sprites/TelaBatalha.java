package view;

import modelo.Arqueiro;
import modelo.Chefao;
import modelo.Guerreiro;
import modelo.Item;
import modelo.Mago;
import modelo.Monstro;
import modelo.Personagem;
import modelo.RepositorioHerois;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class TelaBatalha extends JFrame {

    private static final int LARGURA_SPRITE = 128;
    private static final int X_HEROI_BASE = 40;
    private static final int X_MONSTRO_BASE = 300;
    private static final int Y_BASE = 15;
    private static final int DISTANCIA_LUNGE = 55;
    private static final int TOTAL_PASSOS_LUNGE = 12;
    private static final int TOTAL_PASSOS_PROJETIL = 14;
    private static final int PAUSA_ENTRE_TURNOS_MS = 500;

    private JComboBox<Personagem> comboHeroi;
    private JButton btnIniciar;
    private JButton btnAtacar;
    private JButton btnInventario;
    private JButton btnUsarPocaoVida;
    private JButton btnUsarPocaoMana;
    private JLabel lblPocoes;
    private JComboBox<String> comboFeitico;
    private JLabel lblManaHeroi;
    private JPanel painelFeitico;
    private JTextArea areaLog;
    private JProgressBar barraVidaHeroi;
    private JProgressBar barraVidaMonstro;

    private JPanel painelCena;
    private JLabel labelHeroi;
    private JLabel labelMonstro;

    private Personagem heroi;
    private Monstro monstro;
    private boolean turnoHeroi;
    private boolean batalhaEmAndamento;

    public TelaBatalha() {
        setTitle("Batalha");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        List<Personagem> herois = RepositorioHerois.listar();

        JPanel painelTopo = new JPanel(new GridLayout(3, 2, 5, 5));
        comboHeroi = new JComboBox<>(herois.toArray(new Personagem[0]));
        comboHeroi.addActionListener(e -> {
            if (!batalhaEmAndamento) {
                atualizarPocoes();
            }
        });

        painelTopo.add(new JLabel("Escolha seu herói:"));
        painelTopo.add(comboHeroi);

        btnIniciar = new JButton("Sortear Inimigo e Iniciar Batalha");
        btnIniciar.addActionListener(e -> iniciarBatalha());
        painelTopo.add(new JLabel());
        painelTopo.add(btnIniciar);

        btnInventario = new JButton("Inventário");
        btnInventario.addActionListener(e -> new TelaInventario().setVisible(true));
        painelTopo.add(new JLabel());
        painelTopo.add(btnInventario);

        add(painelTopo, BorderLayout.NORTH);

        // Cena de batalha: painel de posicionamento livre com os dois sprites
        painelCena = new JPanel(null);
        painelCena.setPreferredSize(new Dimension(540, 160));
        painelCena.setBackground(new Color(226, 240, 226));

        labelHeroi = new JLabel();
        labelHeroi.setBounds(X_HEROI_BASE, Y_BASE, LARGURA_SPRITE, LARGURA_SPRITE);
        labelMonstro = new JLabel();
        labelMonstro.setBounds(X_MONSTRO_BASE, Y_BASE, LARGURA_SPRITE, LARGURA_SPRITE);

        painelCena.add(labelHeroi);
        painelCena.add(labelMonstro);

        JPanel painelCentral = new JPanel(new BorderLayout(5, 5));
        painelCentral.add(painelCena, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        painelCentral.add(new JScrollPane(areaLog), BorderLayout.CENTER);

        add(painelCentral, BorderLayout.CENTER);

        // Rodapé: vidas, poções, seleção de feitiço (só pro Mago) e botão de ataque
        JPanel painelInferior = new JPanel(new BorderLayout(5, 5));

        JPanel painelVidas = new JPanel(new GridLayout(1, 2, 10, 0));
        barraVidaHeroi = new JProgressBar(0, 1);
        barraVidaHeroi.setStringPainted(true);
        barraVidaHeroi.setString("-");
        barraVidaMonstro = new JProgressBar(0, 1);
        barraVidaMonstro.setStringPainted(true);
        barraVidaMonstro.setString("-");
        painelVidas.add(barraVidaHeroi);
        painelVidas.add(barraVidaMonstro);
        painelInferior.add(painelVidas, BorderLayout.NORTH);

        JPanel painelMeio = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel painelPocoes = new JPanel(new GridLayout(1, 3, 5, 5));
        lblPocoes = new JLabel("Poções de Vida: -", JLabel.CENTER);
        btnUsarPocaoVida = new JButton("Usar Poção de Vida");
        btnUsarPocaoVida.addActionListener(e -> usarPocaoVida());
        btnUsarPocaoMana = new JButton("Usar Poção de Mana");
        btnUsarPocaoMana.addActionListener(e -> usarPocaoMana());
        painelPocoes.add(lblPocoes);
        painelPocoes.add(btnUsarPocaoVida);
        painelPocoes.add(btnUsarPocaoMana);
        painelMeio.add(painelPocoes);

        painelFeitico = new JPanel(new GridLayout(1, 2, 5, 5));
        comboFeitico = new JComboBox<>(new String[]{"Bola de Fogo", "Flecha de Gelo", "Arma Normal"});
        lblManaHeroi = new JLabel(" ", JLabel.CENTER);
        painelFeitico.add(comboFeitico);
        painelFeitico.add(lblManaHeroi);
        painelFeitico.setVisible(false);
        painelMeio.add(painelFeitico);

        painelInferior.add(painelMeio, BorderLayout.CENTER);

        btnAtacar = new JButton("Atacar");
        btnAtacar.setEnabled(false);
        btnAtacar.addActionListener(e -> executarTurno());
        painelInferior.add(btnAtacar, BorderLayout.SOUTH);

        add(painelInferior, BorderLayout.SOUTH);

        atualizarPocoes();
    }

    private void iniciarBatalha() {
        heroi = (Personagem) comboHeroi.getSelectedItem();

        if (heroi == null) {
            JOptionPane.showMessageDialog(this, "Escolha um herói.");
            return;
        }

        heroi.curarTotalmente();

        boolean ehChefao = heroi.isEnfrentaChefaoNaProximaBatalha();
        if (ehChefao) {
            monstro = Chefao.gerarParaNivel(heroi.getNivel());
            heroi.setEnfrentaChefaoNaProximaBatalha(false);
        } else {
            monstro = Monstro.gerarAleatorio(heroi.getNivel());
        }

        labelHeroi.setIcon(carregarIcone(spriteDoHeroi(heroi), false));
        labelMonstro.setIcon(carregarIcone(spriteDoMonstro(monstro), true));
        labelHeroi.setBounds(X_HEROI_BASE, Y_BASE, LARGURA_SPRITE, LARGURA_SPRITE);
        labelMonstro.setBounds(X_MONSTRO_BASE, Y_BASE, LARGURA_SPRITE, LARGURA_SPRITE);

        painelFeitico.setVisible(heroi instanceof Mago);

        turnoHeroi = true;
        batalhaEmAndamento = true;
        areaLog.setText(ehChefao
                ? "Um CHEFÃO apareceu: " + monstro.getNome() + "!\n"
                : "Um " + monstro.getNome() + " selvagem apareceu!\n");
        areaLog.append(heroi.getNome() + " (Nível " + heroi.getNivel() + ") entra em batalha!\n\n");
        atualizarVidas();

        habilitarAcoesHeroi();
        btnIniciar.setEnabled(false);
        comboHeroi.setEnabled(false);
    }

    // Decide o tipo de animação/ataque de acordo com a classe do herói (sempre é a vez dele aqui)
    private void executarTurno() {
        if (!batalhaEmAndamento || !turnoHeroi) {
            return;
        }

        if (heroi instanceof Mago) {
            executarTurnoMago((Mago) heroi);
        } else if (heroi instanceof Arqueiro) {
            desabilitarAcoesHeroi();
            animarProjetil(true, carregarIcone("flecha.png", false), () -> heroi.atacar(monstro));
        } else {
            desabilitarAcoesHeroi();
            animarCorpoACorpo(true, () -> heroi.atacar(monstro));
        }
    }

    private void executarTurnoMago(Mago mago) {
        String escolha = (String) comboFeitico.getSelectedItem();

        if ("Bola de Fogo".equals(escolha)) {
            if (!mago.temManaParaBolaDeFogo()) {
                JOptionPane.showMessageDialog(this, "Mana insuficiente para Bola de Fogo!");
                return;
            }
            desabilitarAcoesHeroi();
            animarProjetil(true, carregarIcone("fogo.png", false), () -> mago.atacarBolaDeFogo(monstro));
        } else if ("Flecha de Gelo".equals(escolha)) {
            if (!mago.temManaParaFlechaDeGelo()) {
                JOptionPane.showMessageDialog(this, "Mana insuficiente para Flecha de Gelo!");
                return;
            }
            desabilitarAcoesHeroi();
            animarProjetil(true, carregarIcone("gelo.png", false), () -> mago.atacarFlechaDeGelo(monstro));
        } else {
            desabilitarAcoesHeroi();
            animarCorpoACorpo(true, () -> mago.atacarArmaNormal(monstro));
        }
    }

    private void usarPocaoVida() {
        Personagem alvo = batalhaEmAndamento ? heroi : (Personagem) comboHeroi.getSelectedItem();
        if (alvo == null) {
            return;
        }
        if (alvo.getPocoesVida() <= 0) {
            JOptionPane.showMessageDialog(this, "Sem poções de vida.");
            return;
        }

        if (batalhaEmAndamento) {
            if (!turnoHeroi) {
                return;
            }
            desabilitarAcoesHeroi();
            alvo.usarPocaoVida();
            areaLog.append(alvo.getNome() + " bebeu uma Poção de Vida! (+30% de vida)\n");
            atualizarVidas();
            turnoHeroi = false;
            continuarSequenciaDeTurno();
        } else {
            alvo.usarPocaoVida();
            JOptionPane.showMessageDialog(this, alvo.getNome() + " usou uma Poção de Vida!");
            atualizarPocoes();
        }
    }

    private void usarPocaoMana() {
        Personagem selecionado = batalhaEmAndamento ? heroi : (Personagem) comboHeroi.getSelectedItem();
        if (!(selecionado instanceof Mago)) {
            return;
        }
        Mago mago = (Mago) selecionado;
        if (mago.getPocoesMana() <= 0) {
            JOptionPane.showMessageDialog(this, "Sem poções de mana.");
            return;
        }

        if (batalhaEmAndamento) {
            if (!turnoHeroi) {
                return;
            }
            desabilitarAcoesHeroi();
            mago.usarPocaoMana();
            areaLog.append(mago.getNome() + " bebeu uma Poção de Mana! (+30% de mana)\n");
            atualizarVidas();
            turnoHeroi = false;
            continuarSequenciaDeTurno();
        } else {
            mago.usarPocaoMana();
            JOptionPane.showMessageDialog(this, mago.getNome() + " usou uma Poção de Mana!");
            atualizarPocoes();
        }
    }

    // Ataque corpo a corpo: quem ataca avança e recua. O impacto acontece no ponto mais próximo.
    private void animarCorpoACorpo(boolean heroiAtaca, Supplier<String> acaoAtaque) {
        int xAtacante = heroiAtaca ? X_HEROI_BASE : X_MONSTRO_BASE;
        JLabel atacanteLabel = heroiAtaca ? labelHeroi : labelMonstro;
        int direcao = heroiAtaca ? 1 : -1;

        int[] passo = {0};
        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> {
            passo[0]++;
            int metade = TOTAL_PASSOS_LUNGE / 2;
            int deslocamento = passo[0] <= metade
                    ? (DISTANCIA_LUNGE * passo[0]) / metade
                    : (DISTANCIA_LUNGE * (TOTAL_PASSOS_LUNGE - passo[0])) / metade;
            atacanteLabel.setLocation(xAtacante + direcao * deslocamento, atacanteLabel.getY());

            if (passo[0] == metade) {
                aplicarImpacto(heroiAtaca, acaoAtaque);
            }
            if (passo[0] >= TOTAL_PASSOS_LUNGE) {
                atacanteLabel.setLocation(xAtacante, atacanteLabel.getY());
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    // Ataque à distância: quem ataca fica parado, um projétil viaja até o alvo.
    private void animarProjetil(boolean heroiAtaca, ImageIcon iconeProjetil, Supplier<String> acaoAtaque) {
        int xOrigem = heroiAtaca ? X_HEROI_BASE : X_MONSTRO_BASE;
        int xDestino = heroiAtaca ? X_MONSTRO_BASE : X_HEROI_BASE;
        JLabel origemLabel = heroiAtaca ? labelHeroi : labelMonstro;

        int largura = Math.max(iconeProjetil.getIconWidth(), 1);
        int altura = Math.max(iconeProjetil.getIconHeight(), 1);
        boolean vaiDireita = xDestino > xOrigem;

        int yProjetil = origemLabel.getY() + LARGURA_SPRITE / 2 - altura / 2;
        int xInicial = vaiDireita ? xOrigem + LARGURA_SPRITE - 30 : xOrigem + 30 - largura;
        int xFinal = vaiDireita ? xDestino + 30 : xDestino + LARGURA_SPRITE - 30 - largura;

        JLabel projetil = new JLabel(iconeProjetil);
        projetil.setBounds(xInicial, yProjetil, largura, altura);
        painelCena.add(projetil);
        painelCena.setComponentZOrder(projetil, 0);
        painelCena.repaint();

        int[] passo = {0};
        Timer timer = new Timer(18, null);
        timer.addActionListener(e -> {
            passo[0]++;
            int novaX = xInicial + (xFinal - xInicial) * passo[0] / TOTAL_PASSOS_PROJETIL;
            projetil.setLocation(novaX, projetil.getY());

            if (passo[0] >= TOTAL_PASSOS_PROJETIL) {
                ((Timer) e.getSource()).stop();
                painelCena.remove(projetil);
                painelCena.repaint();
                aplicarImpacto(heroiAtaca, acaoAtaque);
            }
        });
        timer.start();
    }

    // Executa o dano de fato e faz o alvo tremer; ao final, decide o que acontece a seguir
    private void aplicarImpacto(boolean heroiAtacou, Supplier<String> acaoAtaque) {
        resolverAtaque(heroiAtacou, acaoAtaque);

        JLabel defensorLabel = heroiAtacou ? labelMonstro : labelHeroi;
        int xDefensor = heroiAtacou ? X_MONSTRO_BASE : X_HEROI_BASE;
        int yDefensor = defensorLabel.getY();

        int[] passo = {0};
        Timer shake = new Timer(30, null);
        shake.addActionListener(e -> {
            passo[0]++;
            int deslocamento = (passo[0] % 2 == 0) ? 6 : -6;
            defensorLabel.setLocation(xDefensor + deslocamento, yDefensor);
            if (passo[0] >= 4) {
                defensorLabel.setLocation(xDefensor, yDefensor);
                ((Timer) e.getSource()).stop();
                continuarSequenciaDeTurno();
            }
        });
        shake.start();
    }

    // Se a batalha continua: sendo a vez do monstro, ele contra-ataca sozinho, sem precisar de clique.
    // Sendo a vez do herói de novo, libera os botões de ação.
    private void continuarSequenciaDeTurno() {
        if (!batalhaEmAndamento) {
            return;
        }

        if (turnoHeroi) {
            habilitarAcoesHeroi();
        } else {
            Timer pausa = new Timer(PAUSA_ENTRE_TURNOS_MS, e -> animarCorpoACorpo(false, () -> monstro.atacar(heroi)));
            pausa.setRepeats(false);
            pausa.start();
        }
    }

    private void resolverAtaque(boolean heroiAtacou, Supplier<String> acaoAtaque) {
        Personagem alvo = heroiAtacou ? monstro : heroi;

        String resultado = acaoAtaque.get();
        areaLog.append(resultado + "\n");
        atualizarVidas();

        if (!alvo.estaVivo()) {
            if (alvo == monstro) {
                areaLog.append("\n" + monstro.getNome() + " foi derrotado! "
                        + heroi.getNome() + " venceu a batalha!\n");
                areaLog.append(heroi.subirNivel() + "\n");

                Item item = Item.gerarAleatorio(monstro instanceof Chefao);
                heroi.adicionarItem(item);
                areaLog.append(heroi.getNome() + " encontrou: " + item + "\n");
                areaLog.append("(Use o botão \"Inventário\" acima para equipar)\n");

                if (monstro instanceof Chefao) {
                    heroi.adicionarPocaoVida();
                    areaLog.append(heroi.getNome() + " ganhou uma Poção de Vida por derrotar o chefão!\n");
                    if (heroi instanceof Mago) {
                        ((Mago) heroi).adicionarPocaoMana();
                        areaLog.append(heroi.getNome() + " ganhou uma Poção de Mana por derrotar o chefão!\n");
                    }
                }
            } else {
                areaLog.append("\n" + heroi.getNome() + " foi derrotado pelo "
                        + monstro.getNome() + "...\n");
            }
            finalizarBatalha();
            return;
        }

        turnoHeroi = !turnoHeroi;
    }

    private void atualizarVidas() {
        atualizarBarra(barraVidaHeroi, heroi.getNome() + " (Nv " + heroi.getNivel() + ")",
                heroi.getVida(), heroi.getVidaMaxima());
        atualizarBarra(barraVidaMonstro, monstro.getNome(),
                monstro.getVida(), monstro.getVidaMaxima());

        if (heroi instanceof Mago) {
            Mago mago = (Mago) heroi;
            lblManaHeroi.setText("Mana: " + mago.getMana() + "/" + mago.getManaMaxima());
        }

        atualizarPocoes();
    }

    private void atualizarPocoes() {
        Personagem alvo = batalhaEmAndamento ? heroi : (Personagem) comboHeroi.getSelectedItem();
        if (alvo == null) {
            return;
        }

        String texto = "Poções de Vida: " + alvo.getPocoesVida();
        boolean ehMago = alvo instanceof Mago;
        if (ehMago) {
            texto += " | Poções de Mana: " + ((Mago) alvo).getPocoesMana();
        }
        lblPocoes.setText(texto);
        btnUsarPocaoMana.setVisible(ehMago);
    }

    private void atualizarBarra(JProgressBar barra, String nome, int vidaAtual, int vidaMaxima) {
        barra.setMaximum(vidaMaxima);
        barra.setValue(vidaAtual);
        barra.setString(nome + " - " + vidaAtual + "/" + vidaMaxima);
        barra.setForeground(corPorPercentual(vidaAtual, vidaMaxima));
    }

    private Color corPorPercentual(int vidaAtual, int vidaMaxima) {
        double percentual = vidaMaxima > 0 ? (double) vidaAtual / vidaMaxima : 0;
        if (percentual >= 0.6) {
            return new Color(46, 160, 67);
        } else if (percentual >= 0.3) {
            return new Color(219, 166, 15);
        } else {
            return new Color(200, 45, 45);
        }
    }

    private void habilitarAcoesHeroi() {
        btnAtacar.setEnabled(true);
        btnUsarPocaoVida.setEnabled(true);
        btnUsarPocaoMana.setEnabled(true);
    }

    private void desabilitarAcoesHeroi() {
        btnAtacar.setEnabled(false);
        btnUsarPocaoVida.setEnabled(false);
        btnUsarPocaoMana.setEnabled(false);
    }

    private void finalizarBatalha() {
        batalhaEmAndamento = false;
        btnAtacar.setEnabled(false);
        btnUsarPocaoVida.setEnabled(true);
        btnUsarPocaoMana.setEnabled(true);
        btnIniciar.setEnabled(true);
        comboHeroi.setEnabled(true);

        if (heroi instanceof Mago) {
            Mago mago = (Mago) heroi;
            if (!heroi.estaVivo()) {
                mago.restaurarManaTotal();
            } else {
                mago.restaurarManaAposBatalha();
            }
        }

        atualizarPocoes();
    }

    private String spriteDoHeroi(Personagem heroi) {
        if (heroi instanceof Guerreiro) return "thorin.png";
        if (heroi instanceof Mago) return "elysia.png";
        if (heroi instanceof Arqueiro) return "kael.png";
        return "thorin.png";
    }

    private String spriteDoMonstro(Monstro monstro) {
        switch (monstro.getNome()) {
            case "Goblin": return "goblin.png";
            case "Orc": return "orc.png";
            case "Slime": return "slime.png";
            case "Lobo Selvagem": return "lobo.png";
            case "Esqueleto": return "esqueleto.png";
            case "Troll": return "troll.png";
            case "Dragão Ancião": return "dragao.png";
            case "Rei Esqueleto": return "rei_esqueleto.png";
            case "Golem de Pedra": return "golem.png";
            case "Senhor dos Lobos": return "senhor_dos_lobos.png";
            case "Lich Sombrio": return "lich.png";
            default: return (monstro instanceof Chefao) ? "chefao.png" : "monstro.png";
        }
    }

    private ImageIcon carregarIcone(String arquivo, boolean espelhar) {
        try {
            BufferedImage imagem = ImageIO.read(new File("assets/sprites/" + arquivo));
            if (espelhar) {
                BufferedImage espelhada = new BufferedImage(
                        imagem.getWidth(), imagem.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = espelhada.createGraphics();
                g2.drawImage(imagem, imagem.getWidth(), 0, -imagem.getWidth(), imagem.getHeight(), null);
                g2.dispose();
                imagem = espelhada;
            }
            return new ImageIcon(imagem);
        } catch (IOException e) {
            System.out.println("Não foi possível carregar o sprite: " + arquivo);
            return new ImageIcon();
        }
    }
}
