package view;

import modelo.Configuracao;
import modelo.Conquistas;
import modelo.Item;
import modelo.Mago;
import modelo.Personagem;
import modelo.RepositorioHerois;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class TelaLoja extends JFrame {

    private static final int PECAS_PARA_FORJAR = 3;

    private JComboBox<Personagem> comboHeroi;
    private JLabel lblOuro;
    private JList<Item> listaItens;
    private DefaultListModel<Item> modeloLista;
    private JButton btnComprarPocaoMana;

    public TelaLoja() {
        setTitle("Loja");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(540, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        List<Personagem> herois = RepositorioHerois.listar();

        JPanel painelTopo = new JPanel(new GridLayout(2, 1, 5, 5));
        comboHeroi = new JComboBox<>(herois.toArray(new Personagem[0]));
        comboHeroi.addActionListener(e -> atualizarLista());
        painelTopo.add(new JLabel("Herói:"));
        painelTopo.add(comboHeroi);
        add(painelTopo, BorderLayout.NORTH);

        JPanel painelCentral = new JPanel(new BorderLayout(5, 5));

        lblOuro = new JLabel("Ouro: -", JLabel.CENTER);
        painelCentral.add(lblOuro, BorderLayout.NORTH);

        modeloLista = new DefaultListModel<>();
        listaItens = new JList<>(modeloLista);
        listaItens.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        painelCentral.add(new JScrollPane(listaItens), BorderLayout.CENTER);

        add(painelCentral, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new GridLayout(2, 2, 5, 5));

        JButton btnVender = new JButton("Vender item selecionado por ouro");
        btnVender.addActionListener(e -> venderSelecionado());
        painelBotoes.add(btnVender);

        JButton btnForjar = new JButton("Forjar 3 iguais em 1 melhor");
        btnForjar.addActionListener(e -> forjarSelecionados());
        painelBotoes.add(btnForjar);

        JButton btnComprarPocaoVida = new JButton("Comprar Poção de Vida");
        btnComprarPocaoVida.addActionListener(e -> comprarPocaoVida());
        painelBotoes.add(btnComprarPocaoVida);

        btnComprarPocaoMana = new JButton("Comprar Poção de Mana");
        btnComprarPocaoMana.addActionListener(e -> comprarPocaoMana());
        painelBotoes.add(btnComprarPocaoMana);

        add(painelBotoes, BorderLayout.SOUTH);

        atualizarLista();
    }

    // Só mostra itens que não estão equipados - não dá pra vender/forjar o que está em uso
    private void atualizarLista() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        modeloLista.clear();
        if (heroi == null) {
            return;
        }
        for (Item item : heroi.getInventario()) {
            if (!heroi.estaEquipado(item)) {
                modeloLista.addElement(item);
            }
        }

        lblOuro.setText("Ouro: " + heroi.getOuro());
        btnComprarPocaoMana.setVisible(heroi instanceof Mago);
    }

    private void venderSelecionado() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        Item item = listaItens.getSelectedValue();

        if (heroi == null || item == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um item para vender (itens equipados não podem ser vendidos).");
            return;
        }

        int valor = valorOuroPorRaridade(item.getRaridade());
        heroi.removerItem(item);
        heroi.adicionarOuro(valor);

        Som.tocar("ouro.wav");
        atualizarLista();
        RepositorioHerois.salvar();

        JOptionPane.showMessageDialog(this, item.getNome() + " vendido por " + valor + " de ouro!");
    }

    private int valorOuroPorRaridade(Item.Raridade raridade) {
        switch (raridade) {
            case NORMAL: return 10;
            case INCOMUM: return 20;
            case RARO: return 40;
            case EPICO: return 80;
            case LENDARIO: return 150;
            default: return 10;
        }
    }

    private void comprarPocaoVida() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        if (heroi == null) {
            return;
        }

        int custo = Configuracao.getInt("loja.custoPocaoVida", 15);
        if (!heroi.gastarOuro(custo)) {
            JOptionPane.showMessageDialog(this, "Ouro insuficiente! Precisa de " + custo + ".");
            return;
        }

        heroi.adicionarPocaoVida();
        Som.tocar("pocao.wav");
        atualizarLista();
        RepositorioHerois.salvar();
        JOptionPane.showMessageDialog(this, "Poção de Vida comprada!");
    }

    private void comprarPocaoMana() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        if (!(heroi instanceof Mago)) {
            return;
        }

        int custo = Configuracao.getInt("loja.custoPocaoMana", 20);
        if (!heroi.gastarOuro(custo)) {
            JOptionPane.showMessageDialog(this, "Ouro insuficiente! Precisa de " + custo + ".");
            return;
        }

        ((Mago) heroi).adicionarPocaoMana();
        Som.tocar("pocao.wav");
        atualizarLista();
        RepositorioHerois.salvar();
        JOptionPane.showMessageDialog(this, "Poção de Mana comprada!");
    }

    // Troca 3 itens do mesmo tipo e raridade por 1 item novo, do mesmo tipo, na raridade seguinte
    private void forjarSelecionados() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        List<Item> selecionados = listaItens.getSelectedValuesList();

        if (heroi == null || selecionados.size() != PECAS_PARA_FORJAR) {
            JOptionPane.showMessageDialog(this,
                    "Selecione exatamente " + PECAS_PARA_FORJAR + " itens (Ctrl+clique) do mesmo tipo e raridade.");
            return;
        }

        Item.TipoItem tipo = selecionados.get(0).getTipo();
        Item.Raridade raridade = selecionados.get(0).getRaridade();
        for (Item item : selecionados) {
            if (item.getTipo() != tipo || item.getRaridade() != raridade) {
                JOptionPane.showMessageDialog(this,
                        "Os 3 itens precisam ser do mesmo tipo e da mesma raridade.");
                return;
            }
        }

        Item.Raridade[] valores = Item.Raridade.values();
        if (raridade.ordinal() >= valores.length - 1) {
            JOptionPane.showMessageDialog(this, "Itens Lendários já são a raridade máxima, não dá pra forjar mais.");
            return;
        }

        Item.Raridade novaRaridade = valores[raridade.ordinal() + 1];
        Item novoItem = Item.gerarComRaridadeETipo(tipo, novaRaridade);

        for (Item item : selecionados) {
            heroi.removerItem(item);
        }
        heroi.adicionarItem(novoItem);

        atualizarLista();
        RepositorioHerois.salvar();
        Conquistas.registrarForja();

        JOptionPane.showMessageDialog(this, "Forja concluída! Você recebeu: " + novoItem);
    }
}