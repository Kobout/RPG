package view;

import modelo.Arqueiro;
import modelo.Chefao;
import modelo.Clerigo;
import modelo.Configuracao;
import modelo.Elemento;
import modelo.GerenciadorDeBatalha;
import modelo.Guerreiro;
import modelo.Item;
import modelo.LogDeBatalha;
import modelo.Mago;
import modelo.Monstro;
import modelo.Personagem;
import modelo.RepositorioHerois;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelaBatalha extends JFrame {

    private static final int LARGURA_SPRITE = 128;
    private static final int X_HEROI_BASE = 40;
    private static final int X_MONSTRO_BASE = 300;
    private static final int Y_BASE = 15;
    private static final int DISTANCIA_LUNGE = 55;
    private static final int TOTAL_PASSOS_LUNGE = 12;
    private static final int TOTAL_PASSOS_PROJETIL = 14;
    private static final int PAUSA_ENTRE_TURNOS_MS = 500;
    private static final int MONSTROS_ANTES_DO_CHEFAO = 3;
    private static final double CHANCE_FUGA = Configuracao.getDouble("batalha.chanceFuga", 0.7);
    private static final Random RANDOM = new Random();

    private JComboBox<Personagem> comboHeroi;
    private JButton btnIniciar;
    private JButton btnAtacar;
    private JButton btnFugir;
    private JButton btnInventario;
    private JButton btnUsarPocaoVida;
    private JButton btnUsarPocaoMana;
    private JLabel lblPocoes;
    private JComboBox<String> comboFeitico;
    private JLabel lblManaHeroi;
    private JPanel painelFeitico;
    private JComboBox<String> comboAcaoClerigo;
    private JLabel lblFeClerigo;
    private JPanel painelClerigo;
    private JLabel lblSalvo;
    private JTextArea areaLog;
    private JProgressBar barraVidaHeroi;
    private Timer timerPiscaHeroi;
    private Timer timerPiscaMonstro;
    private JProgressBar barraVidaMonstro;
    private JLabel lblStatusHeroi;
    private JLabel lblStatusMonstro;

    private JPanel painelCena;
    private JLabel labelHeroi;
    private JLabel labelMonstro;

    private Personagem heroi;
    private Monstro monstro;
    private boolean turnoHeroi;
    private boolean batalhaEmAndamento;
    private boolean caminhoParaChefao;
    private int monstrosDeMasmorraRestantes;

    public TelaBatalha() {
        setTitle("Batalha");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                Musica.tocar("menu.wav");
            }
        });

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

        // Rodapé: vidas, status, poções, seleção de feitiço (só pro Mago) e botões de ação
        JPanel painelInferior = new JPanel(new BorderLayout(5, 5));

        JPanel painelTopoInferior = new JPanel(new GridLayout(2, 1, 0, 2));
        JPanel painelVidas = new JPanel(new GridLayout(1, 2, 10, 0));
        barraVidaHeroi = new JProgressBar(0, 1);
        barraVidaHeroi.setStringPainted(true);
        barraVidaHeroi.setString("-");
        barraVidaMonstro = new JProgressBar(0, 1);
        barraVidaMonstro.setStringPainted(true);
        barraVidaMonstro.setString("-");
        painelVidas.add(barraVidaHeroi);
        painelVidas.add(barraVidaMonstro);
        painelTopoInferior.add(painelVidas);

        JPanel painelStatus = new JPanel(new GridLayout(1, 2, 10, 0));
        lblStatusHeroi = new JLabel(" ", JLabel.CENTER);
        lblStatusMonstro = new JLabel(" ", JLabel.CENTER);
        painelStatus.add(lblStatusHeroi);
        painelStatus.add(lblStatusMonstro);
        painelTopoInferior.add(painelStatus);

        painelInferior.add(painelTopoInferior, BorderLayout.NORTH);

        JPanel painelMeio = new JPanel(new GridLayout(3, 1, 5, 5));

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

        painelClerigo = new JPanel(new GridLayout(1, 2, 5, 5));
        comboAcaoClerigo = new JComboBox<>(new String[]{"Ataque Sagrado", "Cura Divina", "Purificar"});
        lblFeClerigo = new JLabel(" ", JLabel.CENTER);
        painelClerigo.add(comboAcaoClerigo);
        painelClerigo.add(lblFeClerigo);
        painelClerigo.setVisible(false);
        painelMeio.add(painelClerigo);

        painelInferior.add(painelMeio, BorderLayout.CENTER);

        JPanel painelBotoesAcao = new JPanel(new BorderLayout(5, 5));
        JPanel painelBotoesAtacarFugir = new JPanel(new GridLayout(1, 2, 5, 5));
        btnAtacar = new JButton("Atacar");
        btnAtacar.setEnabled(false);
        btnAtacar.addActionListener(e -> executarTurno());
        btnFugir = new JButton("Fugir");
        btnFugir.setEnabled(false);
        btnFugir.addActionListener(e -> tentarFugir());
        painelBotoesAtacarFugir.add(btnAtacar);
        painelBotoesAtacarFugir.add(btnFugir);
        painelBotoesAcao.add(painelBotoesAtacarFugir, BorderLayout.CENTER);

        lblSalvo = new JLabel(" ", JLabel.CENTER);
        painelBotoesAcao.add(lblSalvo, BorderLayout.SOUTH);

        painelInferior.add(painelBotoesAcao, BorderLayout.SOUTH);

        add(painelInferior, BorderLayout.SOUTH);

        atualizarPocoes();

        // Atalhos: Espaço ou Enter também disparam o ataque, sem precisar clicar no mouse
        getRootPane().registerKeyboardAction(
                e -> { if (btnAtacar.isEnabled()) btnAtacar.doClick(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(
                e -> { if (btnAtacar.isEnabled()) btnAtacar.doClick(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void iniciarBatalha() {
        heroi = (Personagem) comboHeroi.getSelectedItem();

        if (heroi == null) {
            JOptionPane.showMessageDialog(this, "Escolha um herói.");
            return;
        }

        if (timerPiscaHeroi != null) {
            timerPiscaHeroi.stop();
            timerPiscaHeroi = null;
        }
        if (timerPiscaMonstro != null) {
            timerPiscaMonstro.stop();
            timerPiscaMonstro = null;
        }

        heroi.curarParcial(Configuracao.getDouble("batalha.percentualCuraInicio", 0.3));

        caminhoParaChefao = heroi.isEnfrentaChefaoNaProximaBatalha();
        if (caminhoParaChefao) {
            heroi.setEnfrentaChefaoNaProximaBatalha(false);
            monstrosDeMasmorraRestantes = MONSTROS_ANTES_DO_CHEFAO;
            monstro = Monstro.gerarAleatorio(heroi.getNivel());
            areaLog.setText("Você entra numa masmorra perigosa antes de enfrentar o chefão...\n");
            areaLog.append("Um " + monstro.getNome() + " selvagem apareceu! ("
                    + monstrosDeMasmorraRestantes + " inimigo(s) até o chefão)\n");
        } else {
            monstrosDeMasmorraRestantes = 0;
            monstro = Monstro.gerarAleatorio(heroi.getNivel());
            areaLog.setText("Um " + monstro.getNome() + " selvagem apareceu!\n");
        }

        labelHeroi.setIcon(carregarIconeComEquipamento(heroi, spriteDoHeroi(heroi), false));
        labelMonstro.setIcon(carregarIcone(spriteDoMonstro(monstro), true));
        labelHeroi.setBounds(X_HEROI_BASE, Y_BASE, LARGURA_SPRITE, LARGURA_SPRITE);
        labelMonstro.setBounds(X_MONSTRO_BASE, Y_BASE, LARGURA_SPRITE, LARGURA_SPRITE);

        painelFeitico.setVisible(heroi instanceof Mago);
        painelClerigo.setVisible(heroi instanceof Clerigo);

        Musica.tocar(monstro instanceof Chefao ? "chefao.wav" : "batalha.wav");

        turnoHeroi = true;
        batalhaEmAndamento = true;
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
        } else if (heroi instanceof Clerigo) {
            executarTurnoClerigo((Clerigo) heroi);
        } else if (heroi instanceof Arqueiro) {
            desabilitarAcoesHeroi();
            animarProjetil(true, carregarIcone("flecha.png", false), () -> heroi.atacar(monstro));
        } else {
            desabilitarAcoesHeroi();
            animarCorpoACorpo(true, () -> heroi.atacar(monstro));
        }
    }

    private void executarTurnoClerigo(Clerigo clerigo) {
        String escolha = (String) comboAcaoClerigo.getSelectedItem();

        if ("Cura Divina".equals(escolha)) {
            if (!clerigo.temFeParaCuraDivina()) {
                JOptionPane.showMessageDialog(this, "Fé insuficiente para Cura Divina!");
                return;
            }
            executarAcaoSemAnimacao(clerigo::curaDivina);
        } else if ("Purificar".equals(escolha)) {
            if (!clerigo.temFeParaPurificar()) {
                JOptionPane.showMessageDialog(this, "Fé insuficiente para Purificar!");
                return;
            }
            executarAcaoSemAnimacao(clerigo::purificar);
        } else {
            desabilitarAcoesHeroi();
            animarCorpoACorpo(true, () -> clerigo.atacarSagrado(monstro));
        }
    }

    // Para ações que não atingem o monstro (cura/purificação): sem animação de golpe,
    // resolve o efeito na hora e segue direto pro contra-ataque do monstro.
    private void executarAcaoSemAnimacao(Supplier<String> acao) {
        desabilitarAcoesHeroi();
        resolverAtaque(true, acao);
        if (batalhaEmAndamento) {
            continuarSequenciaDeTurno();
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

    private void tentarFugir() {
        if (!batalhaEmAndamento || !turnoHeroi) {
            return;
        }

        desabilitarAcoesHeroi();
        boolean sucesso = RANDOM.nextDouble() < CHANCE_FUGA;

        if (sucesso) {
            areaLog.append(heroi.getNome() + " fugiu da batalha!\n");
            LogDeBatalha.registrar(heroi, monstro.getNome(), "FUGA");
            heroi.registrarFuga();
            caminhoParaChefao = false;
            finalizarBatalha();
        } else {
            areaLog.append(heroi.getNome() + " tentou fugir, mas não conseguiu!\n");
            turnoHeroi = false;
            continuarSequenciaDeTurno();
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
            Som.tocar("pocao.wav");
            areaLog.append(alvo.getNome() + " bebeu uma Poção de Vida! (+30% de vida)\n");
            atualizarVidas();
            turnoHeroi = false;
            continuarSequenciaDeTurno();
        } else {
            alvo.usarPocaoVida();
            Som.tocar("pocao.wav");
            JOptionPane.showMessageDialog(this, alvo.getNome() + " usou uma Poção de Vida!");
            atualizarPocoes();
            RepositorioHerois.salvar();
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
            Som.tocar("pocao.wav");
            areaLog.append(mago.getNome() + " bebeu uma Poção de Mana! (+30% de mana)\n");
            atualizarVidas();
            turnoHeroi = false;
            continuarSequenciaDeTurno();
        } else {
            mago.usarPocaoMana();
            Som.tocar("pocao.wav");
            JOptionPane.showMessageDialog(this, mago.getNome() + " usou uma Poção de Mana!");
            atualizarPocoes();
            RepositorioHerois.salvar();
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

    // Executa o dano de fato e faz o alvo tremer (e piscar vermelho); ao final, decide o que
    // acontece a seguir
    private void aplicarImpacto(boolean heroiAtacou, Supplier<String> acaoAtaque) {
        resolverAtaque(heroiAtacou, acaoAtaque);

        JLabel defensorLabel = heroiAtacou ? labelMonstro : labelHeroi;
        int xDefensor = heroiAtacou ? X_MONSTRO_BASE : X_HEROI_BASE;
        int yDefensor = defensorLabel.getY();

        ImageIcon iconeOriginal = (ImageIcon) defensorLabel.getIcon();
        if (iconeOriginal != null && iconeOriginal.getIconWidth() > 0) {
            defensorLabel.setIcon(aplicarTintaVermelha(iconeOriginal));
        }

        int[] passo = {0};
        Timer shake = new Timer(30, null);
        shake.addActionListener(e -> {
            passo[0]++;
            int deslocamento = (passo[0] % 2 == 0) ? 6 : -6;
            defensorLabel.setLocation(xDefensor + deslocamento, yDefensor);
            if (passo[0] >= 4) {
                defensorLabel.setLocation(xDefensor, yDefensor);
                if (iconeOriginal != null) {
                    defensorLabel.setIcon(iconeOriginal);
                }
                ((Timer) e.getSource()).stop();
                continuarSequenciaDeTurno();
            }
        });
        shake.start();
    }

    // Se a batalha continua: processa veneno/atordoamento de quem for agir, e então
    // deixa o monstro contra-atacar sozinho ou libera os botões pro herói.
    private void continuarSequenciaDeTurno() {
        if (!batalhaEmAndamento) {
            return;
        }

        if (turnoHeroi) {
            boolean vaiPular = heroi.estaAtordoado() || heroi.estaCongelado();
            String statusMsg = heroi.processarStatusInicioDeTurno();
            if (statusMsg != null) {
                areaLog.append(statusMsg + "\n");
                atualizarVidas();
            }

            if (heroi instanceof Mago) {
                String mensagemMana = ((Mago) heroi).regenerarManaPorTurno();
                if (mensagemMana != null) {
                    areaLog.append(mensagemMana + "\n");
                    atualizarVidas();
                }
            }

            if (!heroi.estaVivo()) {
                areaLog.append("\n" + heroi.getNome() + " sucumbiu aos efeitos negativos...\n");
                LogDeBatalha.registrar(heroi, monstro.getNome(), "DERROTA");
                Som.tocar("derrota.wav");
                mostrarGameOver();
                caminhoParaChefao = false;
                finalizarBatalha();
                return;
            }
            if (vaiPular) {
                turnoHeroi = false;
                continuarSequenciaDeTurno();
                return;
            }
            habilitarAcoesHeroi();
        } else {
            String statusMsg = monstro.processarStatusInicioDeTurno();
            if (statusMsg != null) {
                areaLog.append(statusMsg + "\n");
                atualizarVidas();
            }
            if (!monstro.estaVivo()) {
                tratarVitoria();
                return;
            }

            String efeitoRegen = monstro.efeitoInicioTurno();
            if (efeitoRegen != null) {
                areaLog.append(efeitoRegen + "\n");
                atualizarVidas();
            }

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
        tocarSomDeAcao(resultado);
        mostrarNumeroFlutuanteSeHouver(heroiAtacou, resultado);

        if (!alvo.estaVivo()) {
            if (alvo == monstro) {
                tratarVitoria();
            } else {
                areaLog.append(GerenciadorDeBatalha.processarDerrota(heroi, monstro));
                LogDeBatalha.registrar(heroi, monstro.getNome(), "DERROTA");
                Som.tocar("derrota.wav");
                mostrarGameOver();
                caminhoParaChefao = false;
                finalizarBatalha();
            }
            return;
        }

        turnoHeroi = !turnoHeroi;
    }

    // Escolhe o som de acordo com o texto do golpe: crítico/super efetivo, bloqueado/esquivado
    // (sem som de impacto) ou um golpe comum.
    private void tocarSomDeAcao(String resultado) {
        if (resultado.contains("BLOQUEOU") || resultado.contains("ESQUIVOU")) {
            return;
        }
        if (resultado.contains("CRÍTICO") || resultado.contains("SUPER EFETIVO")) {
            Som.tocar("critico.wav");
        } else if (resultado.contains("de dano")) {
            Som.tocar("hit.wav");
        }
    }

    // Lê o texto do golpe e mostra "-42" (vermelho) sobre quem apanhou, "BLOQUEIO"/"ESQUIVA"
    // (cinza) quando não acertou, ou "+35" (verde) sobre quem se curou.
    private void mostrarNumeroFlutuanteSeHouver(boolean heroiAtacou, String resultado) {
        if (resultado.contains("BLOQUEOU")) {
            mostrarNumeroFlutuante(!heroiAtacou, "BLOQUEIO!", new Color(150, 150, 150));
            return;
        }
        if (resultado.contains("ESQUIVOU")) {
            mostrarNumeroFlutuante(!heroiAtacou, "ESQUIVA!", new Color(150, 150, 150));
            return;
        }

        Matcher danoMatch = Pattern.compile("causa (\\d+) de dano").matcher(resultado);
        if (danoMatch.find()) {
            mostrarNumeroFlutuante(!heroiAtacou, "-" + danoMatch.group(1), new Color(210, 40, 40));
            return;
        }

        Matcher curaMatch = Pattern.compile("recupera (\\d+) de vida").matcher(resultado);
        if (curaMatch.find()) {
            mostrarNumeroFlutuante(heroiAtacou, "+" + curaMatch.group(1), new Color(46, 160, 67));
        }
    }

    // Sobe um texto sobre o sprite do herói ou do monstro, desaparecendo aos poucos
    private void mostrarNumeroFlutuante(boolean sobreHeroi, String texto, Color cor) {
        JLabel numero = new JLabel(texto, JLabel.CENTER);
        numero.setForeground(cor);
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 18f));

        int xBase = (sobreHeroi ? X_HEROI_BASE : X_MONSTRO_BASE) + LARGURA_SPRITE / 2 - 40;
        int yBase = Y_BASE - 5;
        numero.setBounds(xBase, yBase, 80, 24);
        painelCena.add(numero);
        painelCena.setComponentZOrder(numero, 0);
        painelCena.repaint();

        int[] passo = {0};
        Timer timer = new Timer(30, null);
        timer.addActionListener(e -> {
            passo[0]++;
            numero.setLocation(xBase, yBase - passo[0]);
            if (passo[0] >= 25) {
                ((Timer) e.getSource()).stop();
                painelCena.remove(numero);
                painelCena.repaint();
            }
        });
        timer.start();
    }

    // Trata a vitória sobre o inimigo atual. Se estivermos numa masmorra (a caminho de um
    // chefão) e ainda faltar gente pra enfrentar, o próximo inimigo aparece na mesma batalha,
    // sem cura completa - senão, a batalha termina normalmente.
    private void tratarVitoria() {
        String resultado = GerenciadorDeBatalha.processarVitoria(heroi, monstro);
        areaLog.append(resultado);
        LogDeBatalha.registrar(heroi, monstro.getNome(), "VITORIA");

        if (!resultado.contains("não subiu de nível")) {
            Som.tocar("levelup.wav");
        }
        if (resultado.contains("ÚNICO") || resultado.contains("EPICO") || resultado.contains("LENDARIO")) {
            Som.tocar("item_raro.wav");
        }
        if (resultado.contains(" de ouro!")) {
            Som.tocar("ouro.wav");
        }

        boolean eraChefao = monstro instanceof Chefao;

        if (caminhoParaChefao && !eraChefao) {
            monstrosDeMasmorraRestantes--;
            monstro = (monstrosDeMasmorraRestantes <= 0)
                    ? Chefao.gerarParaNivel(heroi.getNivel())
                    : Monstro.gerarAleatorio(heroi.getNivel());

            labelMonstro.setIcon(carregarIcone(spriteDoMonstro(monstro), true));
            labelMonstro.setBounds(X_MONSTRO_BASE, Y_BASE, LARGURA_SPRITE, LARGURA_SPRITE);

            if (monstro instanceof Chefao) {
                Musica.tocar("chefao.wav");
            }

            areaLog.append("\n" + (monstro instanceof Chefao
                    ? "Os corredores ficam silenciosos... o CHEFÃO " + monstro.getNome() + " aparece!"
                    : "Mais um inimigo se aproxima: " + monstro.getNome()
                        + " (" + monstrosDeMasmorraRestantes + " até o chefão)") + "\n\n");

            atualizarVidas();
            turnoHeroi = true;
            habilitarAcoesHeroi();
            return;
        }

        caminhoParaChefao = false;
        Som.tocar("vitoria.wav");
        finalizarBatalha();
    }

    private void mostrarGameOver() {
        String mensagem = "GAME OVER\n\n" + heroi.getNome() + " caiu para " + monstro.getNome()
                + " no nível " + heroi.getNivel() + ".\n"
                + "Inimigos derrotados na jornada: " + heroi.getMonstrosDerrotados();
        JOptionPane.showMessageDialog(this, mensagem, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void atualizarVidas() {
        atualizarBarra(barraVidaHeroi, heroi.getNome() + " (Nv " + heroi.getNivel() + ")",
                heroi.getVida(), heroi.getVidaMaxima(), true);
        atualizarBarra(barraVidaMonstro, monstro.getNome(),
                monstro.getVida(), monstro.getVidaMaxima(), false);

        lblStatusHeroi.setText(descreverStatus(heroi));
        lblStatusMonstro.setText(descreverStatus(monstro));

        if (heroi instanceof Mago) {
            Mago mago = (Mago) heroi;
            lblManaHeroi.setText("Mana: " + mago.getMana() + "/" + mago.getManaMaxima());
        }
        if (heroi instanceof Clerigo) {
            Clerigo clerigo = (Clerigo) heroi;
            lblFeClerigo.setText("Fé: " + clerigo.getFe() + "/" + clerigo.getFeMaxima());
        }

        atualizarPocoes();
    }

    private String descreverStatus(Personagem p) {
        StringBuilder sb = new StringBuilder();
        if (p.isEnvenenado()) {
            sb.append("☠ Envenenado ");
        }
        if (p.isQueimando()) {
            sb.append("🔥 Queimando ");
        }
        if (p.estaAtordoado()) {
            sb.append("💫 Atordoado ");
        }
        if (p.estaCongelado()) {
            sb.append("❄ Congelado");
        }
        return sb.length() > 0 ? sb.toString() : " ";
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

    private void atualizarBarra(JProgressBar barra, String nome, int vidaAtual, int vidaMaxima, boolean ehHeroi) {
        barra.setMaximum(vidaMaxima);
        barra.setValue(vidaAtual);
        barra.setString(nome + " - " + vidaAtual + "/" + vidaMaxima);

        double percentual = vidaMaxima > 0 ? (double) vidaAtual / vidaMaxima : 0;
        boolean critica = vidaAtual > 0 && percentual < 0.2;

        Timer timerAtual = ehHeroi ? timerPiscaHeroi : timerPiscaMonstro;

        if (critica) {
            if (timerAtual == null || !timerAtual.isRunning()) {
                boolean[] alternar = {false};
                Timer novoTimer = new Timer(400, e -> {
                    alternar[0] = !alternar[0];
                    barra.setForeground(alternar[0] ? new Color(255, 90, 90) : new Color(140, 20, 20));
                });
                novoTimer.start();
                if (ehHeroi) {
                    timerPiscaHeroi = novoTimer;
                } else {
                    timerPiscaMonstro = novoTimer;
                }
            }
        } else {
            if (timerAtual != null) {
                timerAtual.stop();
                if (ehHeroi) {
                    timerPiscaHeroi = null;
                } else {
                    timerPiscaMonstro = null;
                }
            }
            barra.setForeground(corPorPercentual(vidaAtual, vidaMaxima));
        }
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
        btnFugir.setEnabled(true);
        btnUsarPocaoVida.setEnabled(true);
        btnUsarPocaoMana.setEnabled(true);
    }

    private void desabilitarAcoesHeroi() {
        btnAtacar.setEnabled(false);
        btnFugir.setEnabled(false);
        btnUsarPocaoVida.setEnabled(false);
        btnUsarPocaoMana.setEnabled(false);
    }

    private void finalizarBatalha() {
        batalhaEmAndamento = false;
        desabilitarAcoesHeroi();
        btnUsarPocaoVida.setEnabled(true);
        btnUsarPocaoMana.setEnabled(true);
        btnIniciar.setEnabled(true);
        comboHeroi.setEnabled(true);

        GerenciadorDeBatalha.processarFimDeBatalha(heroi);
        RepositorioHerois.salvar();
        mostrarIndicadorSalvo();

        atualizarPocoes();
    }

    private void mostrarIndicadorSalvo() {
        lblSalvo.setText("Progresso salvo ✓");
        lblSalvo.setForeground(new Color(46, 160, 67));
        Timer timer = new Timer(1800, e -> lblSalvo.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    private String spriteDoHeroi(Personagem heroi) {
        if (heroi instanceof Guerreiro) return "thorin.png";
        if (heroi instanceof Mago) return "elysia.png";
        if (heroi instanceof Arqueiro) return "kael.png";
        if (heroi instanceof Clerigo) return "clerigo.png";
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

    // Carrega uma imagem do classpath (pasta "resources" ao lado de "out" na hora de rodar),
    // em vez de um caminho de arquivo relativo — assim funciona também empacotado num .jar/.exe.
    private ImageIcon carregarIcone(String arquivo, boolean espelhar) {
        try (InputStream in = getClass().getResourceAsStream("/sprites/" + arquivo)) {
            if (in == null) {
                System.out.println("Sprite não encontrado no classpath: " + arquivo);
                return new ImageIcon();
            }
            BufferedImage imagem = ImageIO.read(in);
            if (espelhar) {
                imagem = espelharImagem(imagem);
            }
            return new ImageIcon(imagem);
        } catch (IOException e) {
            System.out.println("Não foi possível carregar o sprite: " + arquivo);
            return new ImageIcon();
        }
    }

    // Monta o sprite do herói com um brilho (épico/lendário conforme o item de maior
    // raridade equipado) e pequenos ícones do que está equipado, ao lado do personagem.
    private ImageIcon carregarIconeComEquipamento(Personagem heroi, String arquivoSprite, boolean espelhar) {
        try (InputStream in = getClass().getResourceAsStream("/sprites/" + arquivoSprite)) {
            if (in == null) {
                return new ImageIcon();
            }
            BufferedImage base = ImageIO.read(in);
            if (espelhar) {
                base = espelharImagem(base);
            }

            List<Item> equipados = heroi.getItensEquipados();
            Item.Raridade maiorRaridade = maiorRaridade(equipados);

            int largura = base.getWidth();
            int altura = base.getHeight();
            BufferedImage tela = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tela.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (maiorRaridade == Item.Raridade.EPICO) {
                desenharBrilho(g2, largura, altura, new Color(160, 90, 220), 0.30f);
            } else if (maiorRaridade == Item.Raridade.LENDARIO) {
                desenharBrilho(g2, largura, altura, new Color(250, 170, 60), 0.55f);
            }

            g2.drawImage(base, 0, 0, null);

            // Ícones pequenos dos itens equipados, alinhados na base do sprite
            int x = 2;
            int y = altura - 18;
            for (Item item : equipados) {
                BufferedImage icone = carregarImagemIcone(nomeArquivoIcone(item.getTipo()));
                if (icone != null) {
                    g2.drawImage(icone, x, y, 16, 16, null);
                    x += 18;
                }
            }

            g2.dispose();
            return new ImageIcon(tela);
        } catch (IOException e) {
            System.out.println("Não foi possível montar o sprite com equipamento: " + e.getMessage());
            return carregarIcone(arquivoSprite, espelhar);
        }
    }

    private Item.Raridade maiorRaridade(List<Item> itens) {
        Item.Raridade maior = null;
        for (Item item : itens) {
            if (maior == null || item.getRaridade().ordinal() > maior.ordinal()) {
                maior = item.getRaridade();
            }
        }
        return maior;
    }

    // Brilho suave atrás do personagem: várias camadas semitransparentes da mesma cor
    private void desenharBrilho(Graphics2D g2, int largura, int altura, Color cor, float intensidadeMaxima) {
        int centroX = largura / 2;
        int centroY = altura / 2;
        int camadas = 10;

        for (int i = camadas; i > 0; i--) {
            float alpha = intensidadeMaxima * (i / (float) camadas) * 0.15f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
            g2.setColor(cor);
            int raio = (int) (Math.min(largura, altura) * 0.5 * (i / (float) camadas));
            g2.fillOval(centroX - raio, centroY - raio, raio * 2, raio * 2);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private BufferedImage carregarImagemIcone(String arquivo) {
        try (InputStream in = getClass().getResourceAsStream("/icones/" + arquivo)) {
            return in != null ? ImageIO.read(in) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private String nomeArquivoIcone(Item.TipoItem tipo) {
        switch (tipo) {
            case CAPACETE: return "capacete.png";
            case ARMADURA: return "armadura.png";
            case BOTAS: return "botas.png";
            case LUVAS: return "luvas.png";
            case ANEL: return "anel.png";
            case AMULETO: return "amuleto.png";
            case ARMA: return "arma.png";
            default: return "arma.png";
        }
    }

    private BufferedImage espelharImagem(BufferedImage imagem) {
        BufferedImage espelhada = new BufferedImage(
                imagem.getWidth(), imagem.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = espelhada.createGraphics();
        g2.drawImage(imagem, imagem.getWidth(), 0, -imagem.getWidth(), imagem.getHeight(), null);
        g2.dispose();
        return espelhada;
    }

    // Cria uma versão avermelhada do ícone atual, só sobre os pixels não-transparentes
    // (silhueta do personagem) - usado como "flash" rápido no momento do impacto
    private ImageIcon aplicarTintaVermelha(ImageIcon original) {
        Image imagem = original.getImage();
        int largura = imagem.getWidth(null);
        int altura = imagem.getHeight(null);
        if (largura <= 0 || altura <= 0) {
            return original;
        }

        BufferedImage tingida = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tingida.createGraphics();
        g2.drawImage(imagem, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.55f));
        g2.setColor(new Color(210, 30, 30));
        g2.fillRect(0, 0, largura, altura);
        g2.dispose();

        return new ImageIcon(tingida);
    }
}