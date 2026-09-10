package modelo;

import java.util.Random;

public class Chefao extends Monstro {

    private static final String[] NOMES_CHEFAO = {
        "Dragão Ancião", "Rei Esqueleto", "Golem de Pedra", "Senhor dos Lobos", "Lich Sombrio"
    };
    private static final double CHANCE_ATORDOAR = Configuracao.getDouble("chefao.chanceAtordoar", 0.15);
    private static final double CHANCE_CARREGAR_GOLPE = Configuracao.getDouble("chefao.chanceCarregarGolpe", 0.2);
    private static final double MULTIPLICADOR_GOLPE_CARREGADO =
            Configuracao.getDouble("chefao.multiplicadorGolpeCarregado", 2.5);
    private static final Random RANDOM = new Random();

    private boolean carregandoGolpe = false;

    public Chefao(String nome, int ataque, int vida, int defesa, Elemento fraqueza) {
        super(nome, ataque, vida, defesa, fraqueza, false);
    }

    @Override
    public String atacar(Personagem alvo) {
        // Turno seguinte ao aviso: descarrega o golpe devastador acumulado
        if (carregandoGolpe) {
            carregandoGolpe = false;
            int poder = (int) Math.round(this.getAtaque() * MULTIPLICADOR_GOLPE_CARREGADO);
            int danoAplicado = aplicarDano(alvo, poder);

            if (alvo.isUltimoAtaqueBloqueado()) {
                return nome + " descarrega um golpe DEVASTADOR em " + alvo.getNome()
                        + ", mas " + alvo.getNome() + " BLOQUEOU o golpe!";
            }
            if (alvo.isUltimoAtaqueEsquivado()) {
                return nome + " descarrega um golpe DEVASTADOR em " + alvo.getNome()
                        + ", mas " + alvo.getNome() + " ESQUIVOU do golpe!";
            }

            String mensagem = nome + " descarrega um golpe DEVASTADOR em " + alvo.getNome()
                    + " e causa " + danoAplicado + " de dano!!!";
            if (RANDOM.nextDouble() < CHANCE_ATORDOAR) {
                alvo.aplicarAtordoamento(1);
                mensagem += " " + alvo.getNome() + " ficou atordoado com o golpe!";
            }
            return mensagem;
        }

        // Chance de começar a carregar um golpe forte em vez de atacar normalmente:
        // dá ao jogador exatamente um turno pra reagir (curar, purificar, etc.)
        if (RANDOM.nextDouble() < CHANCE_CARREGAR_GOLPE) {
            carregandoGolpe = true;
            return nome + " começa a reunir poder para um golpe DEVASTADOR no próximo turno! Prepare-se!";
        }

        int danoAplicado = aplicarDano(alvo, this.getAtaque());

        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " (CHEFÃO) golpeia " + alvo.getNome() + ", mas " + alvo.getNome() + " BLOQUEOU o golpe!";
        }
        if (alvo.isUltimoAtaqueEsquivado()) {
            return nome + " (CHEFÃO) golpeia " + alvo.getNome() + ", mas " + alvo.getNome() + " ESQUIVOU do golpe!";
        }

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

        double fatorDificuldade = ProgressoDificuldade.getAtual().getMultiplicadorInimigos();
        int ataque = Math.max(1, (int) Math.round(ataqueBase * fator * fatorDificuldade));
        int vida = Math.max(50, (int) Math.round(vidaBase * fator * fatorDificuldade));
        int defesa = Math.max(1, (int) Math.round(defesaBase * fator * fatorDificuldade));

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