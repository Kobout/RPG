package modelo;

public enum Dificuldade {
    FACIL(0.8, "Fácil"),
    NORMAL(1.0, "Normal"),
    DIFICIL(1.3, "Difícil"),
    NEW_GAME_PLUS(1.6, "New Game+");

    private final double multiplicadorInimigos;
    private final String nomeExibicao;

    Dificuldade(double multiplicadorInimigos, String nomeExibicao) {
        this.multiplicadorInimigos = multiplicadorInimigos;
        this.nomeExibicao = nomeExibicao;
    }

    public double getMultiplicadorInimigos() {
        return multiplicadorInimigos;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }
}