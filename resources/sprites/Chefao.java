package modelo;

import java.util.Random;

public class Chefao extends Monstro {

    private static final String[] NOMES_CHEFAO = {
        "Dragão Ancião", "Rei Esqueleto", "Golem de Pedra", "Senhor dos Lobos", "Lich Sombrio"
    };
    private static final Random RANDOM = new Random();

    public Chefao(String nome, int ataque, int vida, int defesa) {
        super(nome, ataque, vida, defesa);
    }

    @Override
    public String atacar(Personagem alvo) {
        int danoAplicado = aplicarDano(alvo, this.getAtaque());
        return nome + " (CHEFÃO) golpeia " + alvo.getNome() + " com fúria e causa " + danoAplicado + " de dano!";
    }

    // Escala com o NÍVEL do herói. Mais forte que um monstro comum do mesmo nível,
    // mas calibrado para ser vencível com uma luta bem jogada (sem depender de itens).
    public static Chefao gerarParaNivel(int nivelHeroi) {
        String nome = NOMES_CHEFAO[RANDOM.nextInt(NOMES_CHEFAO.length)];
        double fator = Math.pow(1.1, nivelHeroi - 1);

        int ataqueBase = 15 + RANDOM.nextInt(6);   // 15 a 20
        int vidaBase = 130 + RANDOM.nextInt(41);   // 130 a 170
        int defesaBase = 7 + RANDOM.nextInt(5);    // 7 a 11

        int ataque = Math.max(1, (int) Math.round(ataqueBase * fator));
        int vida = Math.max(50, (int) Math.round(vidaBase * fator));
        int defesa = Math.max(1, (int) Math.round(defesaBase * fator));

        return new Chefao(nome, ataque, vida, defesa);
    }
}
