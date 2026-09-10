package modelo;

import java.util.Random;

public class GerenciadorDeBatalha {

    private static final Random RANDOM = new Random();

    private GerenciadorDeBatalha() {
    }

    // Processa a vitória do herói: sobe de nível, dá ouro, gera item (único na 1ª vez que vence
    // aquele chefão específico) e dá poções extras se for chefão. Retorna o texto de log.
    public static String processarVitoria(Personagem heroi, Monstro monstro) {
        StringBuilder log = new StringBuilder();
        log.append("\n").append(monstro.getNome()).append(" foi derrotado! ")
                .append(heroi.getNome()).append(" venceu a batalha!\n");

        if (heroi.getPenalidadesFuga() > 0) {
            heroi.consumirPenalidadeFuga();
            log.append(heroi.getNome()).append(" não subiu de nível desta vez ")
                    .append("(penalidade por ter fugido antes).\n");
        } else {
            log.append(heroi.subirNivel()).append("\n");
        }
        heroi.registrarVitoriaContraMonstro();

        boolean ehChefao = monstro instanceof Chefao;
        boolean bonusNewGamePlus = ProgressoDificuldade.getAtual() == Dificuldade.NEW_GAME_PLUS;

        int ouroGanho = ehChefao
                ? 50 + RANDOM.nextInt(31) + heroi.getNivel() * 5
                : 10 + RANDOM.nextInt(11) + heroi.getNivel() * 2;
        heroi.adicionarOuro(ouroGanho);
        log.append(heroi.getNome()).append(" encontrou ").append(ouroGanho).append(" de ouro!\n");

        Item item;
        if (ehChefao && RegistroDeChefoes.primeiraVez(monstro.getNome())) {
            item = Item.itemUnicoDoChefao(monstro.getNome());
            RegistroDeChefoes.marcarDerrotado(monstro.getNome());
            log.append(heroi.getNome()).append(" encontrou um item ÚNICO: ").append(item).append("\n");
        } else {
            item = Item.gerarAleatorio(ehChefao || bonusNewGamePlus);
            log.append(heroi.getNome()).append(" encontrou: ").append(item).append("\n");
        }
        heroi.adicionarItem(item);
        log.append("(Use o botão \"Inventário\" para equipar)\n");

        if (ehChefao) {
            heroi.adicionarPocaoVida();
            log.append(heroi.getNome()).append(" ganhou uma Poção de Vida por derrotar o chefão!\n");
            if (heroi instanceof Mago) {
                ((Mago) heroi).adicionarPocaoMana();
                log.append(heroi.getNome()).append(" ganhou uma Poção de Mana por derrotar o chefão!\n");
            }

            String novaDificuldade = ProgressoDificuldade.registrarChefaoDerrotado(monstro.getNome());
            if (novaDificuldade != null) {
                log.append("\n*** Você derrotou todos os chefões nesta dificuldade! ")
                        .append("Dificuldade \"").append(novaDificuldade).append("\" desbloqueada! ***\n");
            }
        }
        return log.toString();
    }

    public static String processarDerrota(Personagem heroi, Monstro monstro) {
        return "\n" + heroi.getNome() + " foi derrotado pelo " + monstro.getNome() + "...\n";
    }

    // Ajusta a mana/fé do mago/clérigo ao final da batalha: cheia se morreu, 30% se sobreviveu/venceu.
    public static void processarFimDeBatalha(Personagem heroi) {
        if (heroi instanceof Mago) {
            Mago mago = (Mago) heroi;
            if (!heroi.estaVivo()) {
                mago.restaurarManaTotal();
            } else {
                mago.restaurarManaAposBatalha();
            }
        } else if (heroi instanceof Clerigo) {
            Clerigo clerigo = (Clerigo) heroi;
            if (!heroi.estaVivo()) {
                clerigo.restaurarFeTotal();
            } else {
                clerigo.restaurarFeAposBatalha();
            }
        }
    }
}