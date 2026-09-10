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
        Elemento elemento = getElementoArma();
        double modificador = alvo.getModificadorDano(elemento);
        int poderBase = critico ? this.getAtaque() * 2 : this.getAtaque();
        int poderAtaque = (int) Math.round(poderBase * modificador);
        int danoAplicado = aplicarDano(alvo, poderAtaque);

        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " dispara uma flecha em " + alvo.getNome()
                    + ", mas " + alvo.getNome() + " BLOQUEOU o golpe!";
        }

        String mensagem = nome + " dispara uma flecha em " + alvo.getNome();
        if (critico) {
            mensagem += " - ACERTO CRÍTICO!";
            int danoVeneno = Math.max(1, (int) Math.round(this.getAtaque() * 0.1));
            alvo.aplicarVeneno(2, danoVeneno);
            mensagem += " A flecha estava envenenada!";
        } else if (elemento == Elemento.VENENO) {
            int danoVeneno = Math.max(1, (int) Math.round(this.getAtaque() * 0.1));
            alvo.aplicarVeneno(2, danoVeneno);
            mensagem += " (flecha envenenada pela arma)";
        }
        if (modificador > 1.0) {
            mensagem += " (SUPER EFETIVO!)";
        } else if (modificador < 1.0 && elemento != Elemento.NENHUM) {
            mensagem += " (resistido)";
        }
        mensagem += " e causa " + danoAplicado + " de dano!";
        return mensagem;
    }
}