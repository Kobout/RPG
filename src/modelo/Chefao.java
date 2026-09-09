package modelo;

import java.util.Random;

public class Chefao extends Monstro {

    private static final String[] NOMES_CHEFAO = {
        "Dragão Ancião", "Rei Esqueleto", "Golem de Pedra", "Senhor dos Lobos", "Lich Sombrio"
    };
    private static final double CHANCE_ATORDOAR = Configuracao.getDouble("chefao.chanceAtordoar", 0.15);
    private static final Random RANDOM = new Random();

    public Chefao(String nome, int ataque, int vida, int defesa, Elemento fraqueza) {
        super(nome, ataque, vida, defesa, fraqueza, false);
    }

    @Override
    public String atacar(Personagem alvo) {
        int danoAplicado = aplicarDano(alvo, this.getAtaque());
        String mensagem = nome + " (CHEFÃO) golpeia " + alvo.getNome()
                + " com fúria e causa " + danoAplicado + " de dano!";

        if (RANDOM.nextDouble() < CHANCE_ATORDOAR) {
            alvo.aplicarAtordoamento(1);
            mensagem += " " + alvo.getNome() + " ficou atordoado com o golpe!";
        }

        return mensagem;
    }

    // Escala com o NÍVEL do herói. Calibrado para ser um pouco desfavorável numa luta seca
    // (sem usar poções) — o jogador precisa gerenciar recursos para vencer com segurança.
    // Cada chefão tem uma fraqueza elemental específica, explorável pelo Mago.
    public static Chefao gerarParaNivel(int nivelHeroi) {
        String nome = NOMES_CHEFAO[RANDOM.nextInt(NOMES_CHEFAO.length)];
        double fator = Math.pow(1.1, nivelHeroi - 1);

        int ataqueBase = 20 + RANDOM.nextInt(7);   // 20 a 26
        int vidaBase = 160 + RANDOM.nextInt(41);   // 160 a 200
        int defesaBase = 10 + RANDOM.nextInt(5);   // 10 a 14

        int ataque = Math.max(1, (int) Math.round(ataqueBase * fator));
        int vida = Math.max(50, (int) Math.round(vidaBase * fator));
        int defesa = Math.max(1, (int) Math.round(defesaBase * fator));

        return new Chefao(nome, ataque, vida, defesa, fraquezaPorNome(nome));
    }

    private static Elemento fraquezaPorNome(String nome) {
        switch (nome) {
            case "Golem de Pedra": return Elemento.GELO;
            case "Dragão Ancião": return Elemento.GELO;
            case "Lich Sombrio": return Elemento.FOGO;
            default: return Elemento.NENHUM;
        }
    }
}
