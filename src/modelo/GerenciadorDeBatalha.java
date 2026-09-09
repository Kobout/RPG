package modelo;

public class GerenciadorDeBatalha {

    private GerenciadorDeBatalha() {
    }

    // Processa a vitória do herói: sobe de nível, gera item (único na 1ª vez que vence
    // aquele chefão específico) e dá poções extras se for chefão. Retorna o texto de log.
    public static String processarVitoria(Personagem heroi, Monstro monstro) {
        StringBuilder log = new StringBuilder();
        log.append("\n").append(monstro.getNome()).append(" foi derrotado! ")
                .append(heroi.getNome()).append(" venceu a batalha!\n");
        log.append(heroi.subirNivel()).append("\n");
        heroi.registrarVitoriaContraMonstro();

        boolean ehChefao = monstro instanceof Chefao;
        Item item;
        if (ehChefao && RegistroDeChefoes.primeiraVez(monstro.getNome())) {
            item = Item.itemUnicoDoChefao(monstro.getNome());
            RegistroDeChefoes.marcarDerrotado(monstro.getNome());
            log.append(heroi.getNome()).append(" encontrou um item ÚNICO: ").append(item).append("\n");
        } else {
            item = Item.gerarAleatorio(ehChefao);
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
        }
        return log.toString();
    }

    public static String processarDerrota(Personagem heroi, Monstro monstro) {
        return "\n" + heroi.getNome() + " foi derrotado pelo " + monstro.getNome() + "...\n";
    }

    // Ajusta a mana do mago ao final da batalha: cheia se ele morreu, 30% se sobreviveu/venceu.
    public static void processarFimDeBatalha(Personagem heroi) {
        if (heroi instanceof Mago) {
            Mago mago = (Mago) heroi;
            if (!heroi.estaVivo()) {
                mago.restaurarManaTotal();
            } else {
                mago.restaurarManaAposBatalha();
            }
        }
    }
}
