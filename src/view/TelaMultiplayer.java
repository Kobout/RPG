package view;

import modelo.Arqueiro;
import modelo.Clerigo;
import modelo.Conquistas;
import modelo.Guerreiro;
import modelo.Mago;
import modelo.Personagem;
import modelo.RepositorioHerois;
import rede.ConexaoBatalha;
import rede.MensagemRede;

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
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

// Batalha contra outro jogador pela rede (mesmo Wi-Fi ou internet, com a porta liberada
// no roteador de quem hospeda). Cada lado usa o próprio herói local; o adversário é
// representado aqui por um "espelho" com os mesmos atributos, só para os cálculos de dano.
// Simplificação: o Mago sempre ataca com a arma normal neste modo (sem escolha de feitiço),
// e o duelo não afeta nível/itens/progresso salvo, igual ao PvP local.
public class TelaMultiplayer extends JFrame {

    private JComboBox<Personagem> comboHeroi;
    private JTextField campoIp;
    private JButton btnHospedar;
    private JButton btnConectar;
    private JTextField campoStatusConexao;

    private JPanel painelConexao;
    private JPanel painelBatalha;
    private JLabel labelMeuHeroi;
    private JLabel labelOponente;
    private JTextArea areaLog;
    private JProgressBar barraVidaLocal;
    private JProgressBar barraVidaOponente;
    private JButton btnAtacar;
    private JButton btnUsarPocao;

    private ConexaoBatalha conexao;
    private Personagem meuHeroi;
    private Personagem espelhoOponente;
    private boolean minhaVez;
    private boolean partidaEmAndamento;

    public TelaMultiplayer() {
        setTitle("Batalha Online");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                Musica.tocar("menu.wav");
            }
        });
        Musica.tocar("batalha.wav");

        montarPainelConexao();
        montarPainelBatalha();

        add(painelConexao, BorderLayout.NORTH);
        add(painelBatalha, BorderLayout.CENTER);
        painelBatalha.setVisible(false);
    }

    private void montarPainelConexao() {
        List<Personagem> herois = RepositorioHerois.listar();

        JPanel painelCampos = new JPanel(new GridLayout(3, 2, 5, 5));

        comboHeroi = new JComboBox<>(herois.toArray(new Personagem[0]));
        painelCampos.add(new JLabel("Seu herói:"));
        painelCampos.add(comboHeroi);

        btnHospedar = new JButton("Hospedar Partida");
        btnHospedar.addActionListener(e -> hospedar());
        painelCampos.add(btnHospedar);
        painelCampos.add(new JLabel("(seu amigo vai precisar do seu IP)"));

        campoIp = new JTextField("127.0.0.1");
        btnConectar = new JButton("Conectar por IP");
        btnConectar.addActionListener(e -> conectar());
        painelCampos.add(btnConectar);
        painelCampos.add(campoIp);

        // Campo de texto (não editável) em vez de rótulo: assim dá pra selecionar e
        // copiar o IP inteiro, sem ficar cortado pela largura da janela.
        campoStatusConexao = new JTextField(" ");
        campoStatusConexao.setEditable(false);

        painelConexao = new JPanel(new BorderLayout(5, 5));
        painelConexao.add(painelCampos, BorderLayout.NORTH);
        painelConexao.add(campoStatusConexao, BorderLayout.SOUTH);
    }

    private void montarPainelBatalha() {
        painelBatalha = new JPanel(new BorderLayout(5, 5));

        JPanel painelCena = new JPanel(new GridLayout(1, 2, 10, 0));
        labelMeuHeroi = new JLabel();
        labelMeuHeroi.setHorizontalAlignment(JLabel.CENTER);
        labelOponente = new JLabel();
        labelOponente.setHorizontalAlignment(JLabel.CENTER);
        painelCena.add(labelMeuHeroi);
        painelCena.add(labelOponente);
        painelBatalha.add(painelCena, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        painelBatalha.add(new JScrollPane(areaLog), BorderLayout.CENTER);

        JPanel painelInferior = new JPanel(new BorderLayout(5, 5));

        JPanel painelVidas = new JPanel(new GridLayout(1, 2, 10, 0));
        barraVidaLocal = new JProgressBar(0, 1);
        barraVidaLocal.setStringPainted(true);
        barraVidaOponente = new JProgressBar(0, 1);
        barraVidaOponente.setStringPainted(true);
        painelVidas.add(barraVidaLocal);
        painelVidas.add(barraVidaOponente);
        painelInferior.add(painelVidas, BorderLayout.NORTH);

        JPanel painelBotoes = new JPanel(new GridLayout(1, 2, 5, 5));
        btnAtacar = new JButton("Atacar");
        btnAtacar.addActionListener(e -> atacar());
        btnUsarPocao = new JButton("Usar Poção de Vida");
        btnUsarPocao.addActionListener(e -> usarPocao());
        painelBotoes.add(btnAtacar);
        painelBotoes.add(btnUsarPocao);
        painelInferior.add(painelBotoes, BorderLayout.SOUTH);

        painelBatalha.add(painelInferior, BorderLayout.SOUTH);
    }

    private void hospedar() {
        meuHeroi = (Personagem) comboHeroi.getSelectedItem();
        if (meuHeroi == null) {
            return;
        }

        try {
            String meuIp = InetAddress.getLocalHost().getHostAddress();
            campoStatusConexao.setText("Aguardando conexão... Seu IP: " + meuIp
                    + " porta " + ConexaoBatalha.PORTA_PADRAO);
        } catch (Exception e) {
            campoStatusConexao.setText("Aguardando conexão na porta " + ConexaoBatalha.PORTA_PADRAO + "...");
        }

        btnHospedar.setEnabled(false);
        btnConectar.setEnabled(false);

        conexao = new ConexaoBatalha();
        minhaVez = true; // quem hospeda começa
        conexao.hospedar(ConexaoBatalha.PORTA_PADRAO, criarOuvinte());
    }

    private void conectar() {
        meuHeroi = (Personagem) comboHeroi.getSelectedItem();
        if (meuHeroi == null) {
            return;
        }

        String ip = campoIp.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o IP de quem está hospedando.");
            return;
        }

        campoStatusConexao.setText("Conectando a " + ip + "...");
        btnHospedar.setEnabled(false);
        btnConectar.setEnabled(false);

        conexao = new ConexaoBatalha();
        minhaVez = false; // quem conecta espera o host começar
        conexao.conectar(ip, ConexaoBatalha.PORTA_PADRAO, criarOuvinte());
    }

    private ConexaoBatalha.Ouvinte criarOuvinte() {
        return new ConexaoBatalha.Ouvinte() {
            @Override
            public void aoConectar() {
                campoStatusConexao.setText("Conectado! Trocando informações...");
                enviarInfoHeroi();
            }

            @Override
            public void aoReceber(MensagemRede msg) {
                if (msg.tipo == MensagemRede.Tipo.INFO_HEROI) {
                    receberInfoHeroi(msg);
                } else {
                    receberAcao(msg);
                }
            }

            @Override
            public void aoFalhar(String motivo) {
                JOptionPane.showMessageDialog(TelaMultiplayer.this,
                        "Não foi possível continuar: " + motivo);
                campoStatusConexao.setText("Desconectado.");
                partidaEmAndamento = false;
                btnHospedar.setEnabled(true);
                btnConectar.setEnabled(true);
                painelBatalha.setVisible(false);
            }
        };
    }

    private void enviarInfoHeroi() {
        MensagemRede msg = new MensagemRede();
        msg.tipo = MensagemRede.Tipo.INFO_HEROI;
        msg.nome = meuHeroi.getNome();
        msg.classe = meuHeroi.getClass().getSimpleName();
        msg.nivel = meuHeroi.getNivel();
        msg.ataque = meuHeroi.getAtaque();
        msg.defesa = meuHeroi.getDefesa();
        msg.vidaMaxima = meuHeroi.getVidaMaxima();
        msg.vidaInicial = meuHeroi.getVidaMaxima();
        conexao.enviar(msg);
    }

    private void receberInfoHeroi(MensagemRede msg) {
        switch (msg.classe) {
            case "Mago":
                espelhoOponente = new Mago(msg.nome, 100, msg.ataque, msg.vidaInicial, msg.defesa);
                break;
            case "Arqueiro":
                espelhoOponente = new Arqueiro(msg.nome, 10, msg.ataque, msg.vidaInicial, msg.defesa);
                break;
            case "Clerigo":
                espelhoOponente = new Clerigo(msg.nome, 50, msg.ataque, msg.vidaInicial, msg.defesa);
                break;
            default:
                espelhoOponente = new Guerreiro(msg.nome, "Arma", msg.ataque, msg.vidaInicial, msg.defesa);
        }

        meuHeroi.curarTotalmente();

        labelMeuHeroi.setIcon(carregarIcone(spriteDoHeroi(meuHeroi), false));
        labelOponente.setIcon(carregarIcone(spriteDoHeroi(espelhoOponente), true));

        painelConexao.setVisible(false);
        painelBatalha.setVisible(true);
        partidaEmAndamento = true;

        areaLog.setText("Batalha online contra " + espelhoOponente.getNome() + " começou!\n\n");
        atualizarVidas();
        atualizarBotoes();
    }

    private void atacar() {
        if (!partidaEmAndamento || !minhaVez) {
            return;
        }

        String resultado = meuHeroi.atacar(espelhoOponente);
        areaLog.append(resultado + "\n");
        atualizarVidas();

        enviarAcao(resultado);
        minhaVez = false;

        if (!espelhoOponente.estaVivo()) {
            areaLog.append("\nVocê venceu a batalha!\n");
            partidaEmAndamento = false;
            Conquistas.registrarVitoriaOnline();
            RepositorioHerois.salvar();
        }

        atualizarBotoes();
    }

    private void usarPocao() {
        if (!partidaEmAndamento || !minhaVez) {
            return;
        }
        if (meuHeroi.getPocoesVida() <= 0) {
            JOptionPane.showMessageDialog(this, "Sem poções de vida.");
            return;
        }

        meuHeroi.usarPocaoVida();
        String resultado = meuHeroi.getNome() + " bebeu uma Poção de Vida! (+30% de vida)";
        areaLog.append(resultado + "\n");
        atualizarVidas();

        enviarAcao(resultado);
        minhaVez = false;
        atualizarBotoes();
    }

    private void enviarAcao(String textoLog) {
        MensagemRede msg = new MensagemRede();
        msg.tipo = MensagemRede.Tipo.ACAO;
        msg.textoLog = textoLog;
        msg.vidaRemetenteApos = meuHeroi.getVida();
        msg.vidaDestinatarioApos = espelhoOponente.getVida();
        conexao.enviar(msg);
    }

    private void receberAcao(MensagemRede msg) {
        areaLog.append(msg.textoLog + "\n");

        // A vida que o adversário diz que EU tenho agora, e a vida que ELE tem agora
        aplicarVidaLocal(msg.vidaDestinatarioApos);
        espelhoOponente.aplicarVidaExterna(msg.vidaRemetenteApos);

        atualizarVidas();
        minhaVez = true;

        if (!meuHeroi.estaVivo()) {
            areaLog.append("\nVocê foi derrotado...\n");
            partidaEmAndamento = false;
        }

        atualizarBotoes();
    }

    private void aplicarVidaLocal(int novaVida) {
        meuHeroi.aplicarVidaExterna(novaVida);
    }

    private void atualizarVidas() {
        barraVidaLocal.setMaximum(meuHeroi.getVidaMaxima());
        barraVidaLocal.setValue(Math.max(0, meuHeroi.getVida()));
        barraVidaLocal.setString(meuHeroi.getNome() + " - " + meuHeroi.getVida() + "/" + meuHeroi.getVidaMaxima());

        barraVidaOponente.setMaximum(espelhoOponente.getVidaMaxima());
        barraVidaOponente.setValue(Math.max(0, espelhoOponente.getVida()));
        barraVidaOponente.setString(espelhoOponente.getNome() + " - "
                + espelhoOponente.getVida() + "/" + espelhoOponente.getVidaMaxima());
    }

    private void atualizarBotoes() {
        boolean podeAgir = partidaEmAndamento && minhaVez;
        btnAtacar.setEnabled(podeAgir);
        btnUsarPocao.setEnabled(podeAgir);
    }

    private String spriteDoHeroi(Personagem heroi) {
        if (heroi instanceof Guerreiro) return "thorin.png";
        if (heroi instanceof Mago) return "elysia.png";
        if (heroi instanceof Arqueiro) return "kael.png";
        if (heroi instanceof Clerigo) return "clerigo.png";
        return "thorin.png";
    }

    private ImageIcon carregarIcone(String arquivo, boolean espelhar) {
        try (InputStream in = getClass().getResourceAsStream("/sprites/" + arquivo)) {
            if (in == null) {
                return new ImageIcon();
            }
            BufferedImage imagem = ImageIO.read(in);
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