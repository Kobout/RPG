package modelo;

import java.util.Random;

public class Monstro extends Personagem {

    private static final String[] NOMES = {
        "Goblin", "Orc", "Slime", "Lobo Selvagem", "Esqueleto", "Troll"
    };
    private static final Random RANDOM = new Random();

    public Monstro(String nome, int ataque, int vida, int defesa) {
        super(nome, ataque, vida, defesa);
    }

    @Override
    public String atacar(Personagem alvo) {
        int danoAplicado = aplicarDano(alvo, this.getAtaque());
        return nome + " ataca " + alvo.getNome() + " e causa " + danoAplicado + " de dano!";
    }

    // Escala com o NÍVEL do herói (não com os atributos atuais/itens), garantindo
    // que equipamentos sejam sempre uma vantagem real e a dificuldade seja previsível.
    public static Monstro gerarAleatorio(int nivelHeroi) {
        String nome = NOMES[RANDOM.nextInt(NOMES.length)];
        double fator = Math.pow(1.1, nivelHeroi - 1);

        int ataqueBase = 10 + RANDOM.nextInt(6);  // 10 a 15
        int vidaBase = 35 + RANDOM.nextInt(16);   // 35 a 50
        int defesaBase = 4 + RANDOM.nextInt(5);   // 4 a 8

        int ataque = Math.max(1, (int) Math.round(ataqueBase * fator));
        int vida = Math.max(10, (int) Math.round(vidaBase * fator));
        int defesa = Math.max(1, (int) Math.round(defesaBase * fator));

        return new Monstro(nome, ataque, vida, defesa);
    }
}
