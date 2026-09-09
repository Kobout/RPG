package modelo;

import java.util.Random;

public class Arqueiro extends Personagem {

    private int alcance;
    private static final double CHANCE_CRITICO = Configuracao.getDouble("arqueiro.chanceCritico", 0.3);
    private static final Random RANDOM = new Random();

    public Arqueiro(String nome, int alcance, int ataque, int vida, int defesa) {
        super(nome, ataque, vida, defesa);
        this.alcance = alcance;
    }

    public int getAlcance() {
        return alcance;
    }

    @Override
    public String atacar(Personagem alvo) {
        boolean critico = RANDOM.nextDouble() < CHANCE_CRITICO;
        int poderAtaque = critico ? this.getAtaque() * 2 : this.getAtaque();
        int danoAplicado = aplicarDano(alvo, poderAtaque);

        String mensagem = nome + " dispara uma flecha em " + alvo.getNome();
        if (critico) {
            mensagem += " - ACERTO CRÍTICO!";
            int danoVeneno = Math.max(1, (int) Math.round(this.getAtaque() * 0.1));
            alvo.aplicarVeneno(2, danoVeneno);
            mensagem += " A flecha estava envenenada!";
        }
        mensagem += " e causa " + danoAplicado + " de dano!";
        return mensagem;
    }
}
