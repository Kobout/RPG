package view;

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
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class TelaLoja extends JFrame {

    private JComboBox<Personagem> comboHeroi;
    private JList<Item> listaItens;
    private DefaultListModel<Item> modeloLista;

    public TelaLoja() {
        setTitle("Loja - Trocar Itens por Poções");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        List<Personagem> herois = RepositorioHerois.listar();

        JPanel painelTopo = new JPanel(new GridLayout(2, 1, 5, 5));
        comboHeroi = new JComboBox<>(herois.toArray(new Personagem[0]));
        comboHeroi.addActionListener(e -> atualizarLista());
        painelTopo.add(new JLabel("Herói:"));
        painelTopo.add(comboHeroi);
        add(painelTopo, BorderLayout.NORTH);

        modeloLista = new DefaultListModel<>();
        listaItens = new JList<>(modeloLista);
        add(new JScrollPane(listaItens), BorderLayout.CENTER);

        JButton btnVender = new JButton("Vender item selecionado por poções");
        btnVender.addActionListener(e -> venderSelecionado());
        add(btnVender, BorderLayout.SOUTH);

        atualizarLista();
    }

    // Só mostra itens que não estão equipados - não dá pra vender o que está em uso
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
    }

    private void venderSelecionado() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        Item item = listaItens.getSelectedValue();

        if (heroi == null || item == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um item para vender (itens equipados não podem ser vendidos).");
            return;
        }

        int pocoesVida;
        int pocoesMana;

        switch (item.getRaridade()) {
            case LENDARIO:
                pocoesVida = 2;
                pocoesMana = 1;
                break;
            case EPICO:
            case RARO:
                pocoesVida = 1;
                pocoesMana = 1;
                break;
            default:
                pocoesVida = 1;
                pocoesMana = 0;
        }

        heroi.removerItem(item);
        for (int i = 0; i < pocoesVida; i++) {
            heroi.adicionarPocaoVida();
        }
        if (heroi instanceof Mago) {
            for (int i = 0; i < pocoesMana; i++) {
                ((Mago) heroi).adicionarPocaoMana();
            }
        }

        atualizarLista();
        RepositorioHerois.salvar();

        String msg = item.getNome() + " vendido! Recebeu " + pocoesVida + " poção(ões) de vida";
        if (pocoesMana > 0 && heroi instanceof Mago) {
            msg += " e " + pocoesMana + " de mana";
        }
        JOptionPane.showMessageDialog(this, msg + ".");
    }
}