package modelo;

public class Guerreiro extends Personagem {

    private String arma;

    public Guerreiro(String nome, String arma, int ataque, int vida, int defesa) {
        super(nome, ataque, vida, defesa);
        this.arma = arma;
    }

    public String getArma() {
        return arma;
    }

    @Override
    public String atacar(Personagem alvo) {
        int danoAplicado = aplicarDano(alvo, this.getAtaque());
        return nome + " ataca " + alvo.getNome() + " com " + arma
                + " e causa " + danoAplicado + " de dano!";
    }
}
