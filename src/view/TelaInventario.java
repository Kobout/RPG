package view;

import modelo.Item;
import modelo.Personagem;
import modelo.RepositorioHerois;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TelaInventario extends JFrame {

    private JComboBox<Personagem> comboHeroi;
    private JTextArea areaEquipamentos;
    private JList<Item> listaItens;
    private DefaultListModel<Item> modeloLista;

    public TelaInventario() {
        setTitle("Inventário");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        List<Personagem> herois = RepositorioHerois.listar();

        JPanel painelTopo = new JPanel(new GridLayout(2, 1, 5, 5));
        comboHeroi = new JComboBox<>(herois.toArray(new Personagem[0]));
        comboHeroi.addActionListener(e -> atualizarTela());
        painelTopo.add(new JLabel("Herói:"));
        painelTopo.add(comboHeroi);
        add(painelTopo, BorderLayout.NORTH);

        JPanel painelCentral = new JPanel(new GridLayout(1, 2, 10, 0));

        areaEquipamentos = new JTextArea();
        areaEquipamentos.setEditable(false);
        JPanel painelEquip = new JPanel(new BorderLayout());
        painelEquip.setBorder(BorderFactory.createTitledBorder("Equipados"));
        painelEquip.add(new JScrollPane(areaEquipamentos), BorderLayout.CENTER);
        painelCentral.add(painelEquip);

        modeloLista = new DefaultListModel<>();
        listaItens = new JList<>(modeloLista);
        listaItens.setCellRenderer(new ItemCellRenderer());
        JPanel painelInventario = new JPanel(new BorderLayout());
        painelInventario.setBorder(BorderFactory.createTitledBorder("Itens Encontrados"));
        painelInventario.add(new JScrollPane(listaItens), BorderLayout.CENTER);
        painelCentral.add(painelInventario);

        add(painelCentral, BorderLayout.CENTER);

        JButton btnEquipar = new JButton("Equipar item selecionado");
        btnEquipar.addActionListener(e -> equiparSelecionado());
        add(btnEquipar, BorderLayout.SOUTH);

        atualizarTela();
    }

    private void atualizarTela() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        if (heroi == null) {
            return;
        }

        modeloLista.clear();
        for (Item item : heroi.getInventario()) {
            modeloLista.addElement(item);
        }

        StringBuilder texto = new StringBuilder();
        texto.append("Capacete: ").append(descreve(heroi.getEquipado(Item.TipoItem.CAPACETE))).append("\n\n");
        texto.append("Armadura: ").append(descreve(heroi.getEquipado(Item.TipoItem.ARMADURA))).append("\n\n");
        texto.append("Botas: ").append(descreve(heroi.getEquipado(Item.TipoItem.BOTAS))).append("\n\n");
        texto.append("Luvas: ").append(descreve(heroi.getEquipado(Item.TipoItem.LUVAS))).append("\n\n");
        texto.append("Anel 1: ").append(descreve(heroi.getAnelEquipado(0))).append("\n\n");
        texto.append("Anel 2: ").append(descreve(heroi.getAnelEquipado(1))).append("\n\n");
        texto.append("Amuleto: ").append(descreve(heroi.getEquipado(Item.TipoItem.AMULETO))).append("\n\n");
        texto.append("Arma: ").append(descreve(heroi.getEquipado(Item.TipoItem.ARMA))).append("\n");

        areaEquipamentos.setText(texto.toString());
    }

    private String descreve(Item item) {
        return item != null ? item.toString() : "(vazio)";
    }

    private void equiparSelecionado() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        Item selecionado = listaItens.getSelectedValue();

        if (heroi == null || selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um herói e um item da lista.");
            return;
        }

        if (selecionado.getTipo() == Item.TipoItem.ANEL) {
            String[] opcoes = {"Anel 1", "Anel 2"};
            int escolha = JOptionPane.showOptionDialog(this,
                    "Em qual slot de anel deseja equipar?",
                    "Escolher slot", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, opcoes, opcoes[0]);
            if (escolha == JOptionPane.CLOSED_OPTION) {
                return;
            }
            heroi.equipar(selecionado, escolha);
        } else {
            heroi.equipar(selecionado);
        }

        atualizarTela();
        RepositorioHerois.salvar();
        JOptionPane.showMessageDialog(this, selecionado.getNome() + " equipado em " + heroi.getNome() + "!");
    }

    // Renderiza cada item da lista com um ícone correspondente ao seu tipo
    private static class ItemCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value instanceof Item) {
                Item item = (Item) value;
                label.setIcon(carregarIconeItem(item));
                label.setText(item.toString());
            }
            return label;
        }

        // Monta o ícone do item com um fundo colorido de acordo com a raridade
        private ImageIcon carregarIconeItem(Item item) {
            try (InputStream in = getClass().getResourceAsStream("/icones/" + nomeArquivoIcone(item.getTipo()))) {
                if (in == null) {
                    System.out.println("Ícone não encontrado no classpath: " + item.getTipo());
                    return null;
                }
                BufferedImage icone = ImageIO.read(in);

                int tamanho = Math.max(icone.getWidth(), icone.getHeight()) + 10;
                BufferedImage composto = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = composto.createGraphics();

                g2.setColor(corPorRaridade(item.getRaridade()));
                g2.fillRect(0, 0, tamanho, tamanho);
                g2.setColor(new Color(90, 90, 90));
                g2.drawRect(0, 0, tamanho - 1, tamanho - 1);

                int x = (tamanho - icone.getWidth()) / 2;
                int y = (tamanho - icone.getHeight()) / 2;
                g2.drawImage(icone, x, y, null);
                g2.dispose();

                return new ImageIcon(composto);
            } catch (IOException e) {
                System.out.println("Não foi possível carregar o ícone: " + item.getTipo());
                return null;
            }
        }

        private Color corPorRaridade(Item.Raridade raridade) {
            switch (raridade) {
                case NORMAL: return Color.WHITE;
                case INCOMUM: return new Color(140, 215, 140);
                case RARO: return new Color(120, 170, 240);
                case EPICO: return new Color(190, 130, 230);
                case LENDARIO: return new Color(250, 170, 60);
                default: return Color.WHITE;
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
    }
}