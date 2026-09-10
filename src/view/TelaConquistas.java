package view;

import modelo.Conquistas;
import modelo.Dificuldade;
import modelo.Personagem;
import modelo.ProgressoDificuldade;
import modelo.RegistroDeChefoes;
import modelo.RepositorioHerois;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import java.util.List;

public class TelaConquistas extends JFrame {

    private static final int RECOMPENSA_OURO = 100;

    public TelaConquistas() {
        setTitle("Conquistas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);

        JPanel painel = new JPanel(new GridLayout(0, 1, 5, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int chefoesDerrotados = RegistroDeChefoes.exportar().size();

        int maiorNivel = 0;
        List<Personagem> herois = RepositorioHerois.listar();
        for (Personagem p : herois) {
            maiorNivel = Math.max(maiorNivel, p.getNivel());
        }

        boolean primeiroSangue = chefoesDerrotados >= 1;
        boolean flagelo = chefoesDerrotados >= 5;
        boolean veterano = maiorNivel >= 20;
        boolean ferreiro = Conquistas.getItensForjados() >= 5;
        boolean duelista = Conquistas.getVitoriasOnline() >= 3;
        boolean ascensao = ProgressoDificuldade.estaDesbloqueada(Dificuldade.DIFICIL);
        boolean lendaAbsoluta = ProgressoDificuldade.estaDesbloqueada(Dificuldade.NEW_GAME_PLUS);

        verificarRecompensa("primeiro_sangue", primeiroSangue);
        verificarRecompensa("flagelo_dos_chefoes", flagelo);
        verificarRecompensa("veterano", veterano);
        verificarRecompensa("ferreiro", ferreiro);
        verificarRecompensa("duelista", duelista);
        verificarRecompensa("ascensao", ascensao);
        verificarRecompensa("lenda_absoluta", lendaAbsoluta);

        painel.add(criarLinha("Primeiro Sangue", "Derrote seu primeiro chefão", primeiroSangue));
        painel.add(criarLinha("Flagelo dos Chefões", "Derrote todos os 5 chefões pelo menos uma vez", flagelo));
        painel.add(criarLinha("Veterano", "Alcance o nível 20 com algum herói", veterano));
        painel.add(criarLinha("Ferreiro", "Forje 5 itens na Loja", ferreiro));
        painel.add(criarLinha("Duelista", "Vença 3 batalhas online", duelista));
        painel.add(criarLinha("Ascensão", "Desbloqueie a dificuldade Difícil", ascensao));
        painel.add(criarLinha("Lenda Absoluta", "Desbloqueie o New Game+", lendaAbsoluta));

        add(new JScrollPane(painel));
    }

    // Na primeira vez que uma conquista é concluída, dá ouro pra todos os heróis
    private void verificarRecompensa(String id, boolean concluida) {
        if (concluida && !Conquistas.jaRecompensada(id)) {
            Conquistas.marcarRecompensada(id);
            for (Personagem p : RepositorioHerois.listar()) {
                p.adicionarOuro(RECOMPENSA_OURO);
            }
            RepositorioHerois.salvar();
            Som.tocar("ouro.wav");
            JOptionPane.showMessageDialog(this,
                    "Conquista desbloqueada! Todos os heróis ganharam " + RECOMPENSA_OURO + " de ouro.");
        }
    }

    private JLabel criarLinha(String titulo, String descricao, boolean concluida) {
        String icone = concluida ? "✅" : "🔒";
        return new JLabel(icone + "  " + titulo + " — " + descricao);
    }
}