package view;

import modelo.Clerigo;
import modelo.Item;
import modelo.Mago;
import modelo.Personagem;
import modelo.RepositorioHerois;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

public class TelaFichaPersonagem extends JFrame {

    private JComboBox<Personagem> comboHeroi;
    private JTextArea areaFicha;

    public TelaFichaPersonagem() {
        setTitle("Ficha do Personagem");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        List<Personagem> herois = RepositorioHerois.listar();
        comboHeroi = new JComboBox<>(herois.toArray(new Personagem[0]));
        comboHeroi.addActionListener(e -> atualizarFicha());
        add(comboHeroi, BorderLayout.NORTH);

        areaFicha = new JTextArea();
        areaFicha.setEditable(false);
        areaFicha.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(areaFicha), BorderLayout.CENTER);

        atualizarFicha();
    }

    private void atualizarFicha() {
        Personagem heroi = (Personagem) comboHeroi.getSelectedItem();
        if (heroi == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(heroi.getNome()).append(" (").append(heroi.getClass().getSimpleName()).append(")\n");
        sb.append("Nível: ").append(heroi.getNivel()).append("\n\n");

        sb.append("--- Atributos ---\n");
        sb.append("Ataque: ").append(heroi.getAtaque()).append("\n");
        sb.append("Defesa: ").append(heroi.getDefesa()).append("\n");
        sb.append("Vida: ").append(heroi.getVida()).append("/").append(heroi.getVidaMaxima()).append("\n");

        if (heroi instanceof Mago) {
            Mago mago = (Mago) heroi;
            sb.append("Mana: ").append(mago.getMana()).append("/").append(mago.getManaMaxima()).append("\n");
            sb.append("Poções de Mana: ").append(mago.getPocoesMana()).append("\n");
        }
        if (heroi instanceof Clerigo) {
            Clerigo clerigo = (Clerigo) heroi;
            sb.append("Fé: ").append(clerigo.getFe()).append("/").append(clerigo.getFeMaxima()).append("\n");
        }

        sb.append("\n--- Recursos ---\n");
        sb.append("Ouro: ").append(heroi.getOuro()).append("\n");
        sb.append("Poções de Vida: ").append(heroi.getPocoesVida()).append("\n");
        sb.append("Monstros derrotados: ").append(heroi.getMonstrosDerrotados()).append("\n");

        sb.append("\n--- Equipamentos ---\n");
        sb.append("Capacete: ").append(descreve(heroi.getEquipado(Item.TipoItem.CAPACETE))).append("\n");
        sb.append("Armadura: ").append(descreve(heroi.getEquipado(Item.TipoItem.ARMADURA))).append("\n");
        sb.append("Botas:    ").append(descreve(heroi.getEquipado(Item.TipoItem.BOTAS))).append("\n");
        sb.append("Luvas:    ").append(descreve(heroi.getEquipado(Item.TipoItem.LUVAS))).append("\n");
        sb.append("Anel 1:   ").append(descreve(heroi.getAnelEquipado(0))).append("\n");
        sb.append("Anel 2:   ").append(descreve(heroi.getAnelEquipado(1))).append("\n");
        sb.append("Amuleto:  ").append(descreve(heroi.getEquipado(Item.TipoItem.AMULETO))).append("\n");
        sb.append("Arma:     ").append(descreve(heroi.getEquipado(Item.TipoItem.ARMA))).append("\n");

        areaFicha.setText(sb.toString());
    }

    private String descreve(Item item) {
        return item != null ? item.toString() : "(vazio)";
    }
}