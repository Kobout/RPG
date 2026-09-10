package modelo;

import java.util.Random;

public class Guerreiro extends Personagem {

    private String arma;
    private static final double CHANCE_BLOQUEIO = Configuracao.getDouble("guerreiro.chanceBloqueio", 0.15);
    private static final Random RANDOM = new Random();

    public Guerreiro(String nome, String arma, int ataque, int vida, int defesa) {
        super(nome, ataque, vida, defesa);
        this.arma = arma;
    }

    public String getArma() {
        return arma;
    }

    // Passiva: 15% de chance de bloquear qualquer ataque recebido por completo
    @Override
    protected boolean tentarBloquear() {
        return RANDOM.nextDouble() < CHANCE_BLOQUEIO;
    }

    @Override
    public String atacar(Personagem alvo) {
        Elemento elemento = getElementoArma();
        double modificador = alvo.getModificadorDano(elemento);
        int poder = (int) Math.round(this.getAtaque() * modificador);
        int danoAplicado = aplicarDano(alvo, poder);

        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " ataca " + alvo.getNome() + " com " + arma
                    + ", mas " + alvo.getNome() + " BLOQUEOU o golpe!";
        }

        String extra = "";
        if (elemento == Elemento.VENENO) {
            int danoVeneno = Math.max(1, (int) Math.round(this.getAtaque() * 0.1));
            alvo.aplicarVeneno(2, danoVeneno);
            extra = " O golpe estava envenenado!";
        } else if (modificador > 1.0) {
            extra = " (SUPER EFETIVO!)";
        } else if (modificador < 1.0 && elemento != Elemento.NENHUM) {
            extra = " (resistido)";
        }

        return nome + " ataca " + alvo.getNome() + " com " + arma + extra
                + " e causa " + danoAplicado + " de dano!";
    }
}