package modelo;

import java.util.Random;

public class Monstro extends Personagem {

    private static final String[] NOMES = {
        "Goblin", "Orc", "Slime", "Lobo Selvagem", "Esqueleto", "Troll"
    };
    private static final Random RANDOM = new Random();

    protected Elemento fraqueza;
    private final boolean regeneraVida;

    public Monstro(String nome, int ataque, int vida, int defesa) {
        this(nome, ataque, vida, defesa, Elemento.NENHUM, false);
    }

    public Monstro(String nome, int ataque, int vida, int defesa, Elemento fraqueza, boolean regeneraVida) {
        super(nome, ataque, vida, defesa);
        this.fraqueza = fraqueza;
        this.regeneraVida = regeneraVida;
    }

    public Elemento getFraqueza() {
        return fraqueza;
    }

    // Chamado no início do turno do monstro. Trolls regeneram parte da vida perdida.
    public String efeitoInicioTurno() {
        if (regeneraVida && estaVivo() && vida < getVidaMaxima()) {
            double percentual = Configuracao.getDouble("troll.percentualRegeneracao", 0.08);
            int cura = Math.max(1, (int) Math.round(getVidaMaxima() * percentual));
            vida = Math.min(getVidaMaxima(), vida + cura);
            return nome + " regenera " + cura + " de vida!";
        }
        return null;
    }

    @Override
    public String atacar(Personagem alvo) {
        int danoAplicado = aplicarDano(alvo, this.getAtaque());
        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " ataca " + alvo.getNome() + ", mas " + alvo.getNome() + " BLOQUEOU o golpe!";
        }
        if (alvo.isUltimoAtaqueEsquivado()) {
            return nome + " ataca " + alvo.getNome() + ", mas " + alvo.getNome() + " ESQUIVOU do golpe!";
        }
        return nome + " ataca " + alvo.getNome() + " e causa " + danoAplicado + " de dano!";
    }

    // A fraqueza fixa do monstro/chefão soma-se a qualquer resistência/fraqueza vinda de itens
    @Override
    public double getModificadorDano(Elemento elemento) {
        if (elemento != Elemento.NENHUM && elemento == fraqueza) {
            return Configuracao.getDouble("mago.bonusElementoFraco", 1.5);
        }
        return super.getModificadorDano(elemento);
    }

    // Escala com o NÍVEL do herói. Cada criatura tem um perfil de atributos diferente,
    // então lutar contra um Esqueleto é uma experiência distinta de lutar com um Troll.
    public static Monstro gerarAleatorio(int nivelHeroi) {
        String nome = NOMES[RANDOM.nextInt(NOMES.length)];
        double fator = Math.pow(1.1, nivelHeroi - 1);

        double multAtaque = 1.0;
        double multVida = 1.0;
        double multDefesa = 1.0;
        boolean regenera = false;

        switch (nome) {
            case "Esqueleto":
                // ossos duros por fora, mas pouca vida por dentro
                multDefesa = 1.6;
                multVida = 0.7;
                break;
            case "Slime":
                // corpo gelatinoso absorve parte do impacto físico
                multDefesa = 1.4;
                multAtaque = 0.8;
                break;
            case "Troll":
                // grande, resistente e se regenera a cada turno
                multVida = 1.5;
                regenera = true;
                break;
            case "Orc":
                // agressivo, mas descuidado com a própria defesa
                multAtaque = 1.3;
                multDefesa = 0.9;
                break;
            default:
                break;
        }

        int ataqueBase = 10 + RANDOM.nextInt(6);
        int vidaBase = 35 + RANDOM.nextInt(16);
        int defesaBase = 4 + RANDOM.nextInt(5);

        double fatorDificuldade = ProgressoDificuldade.getAtual().getMultiplicadorInimigos();
        int ataque = Math.max(1, (int) Math.round(ataqueBase * fator * multAtaque * fatorDificuldade));
        int vida = Math.max(10, (int) Math.round(vidaBase * fator * multVida * fatorDificuldade));
        int defesa = Math.max(1, (int) Math.round(defesaBase * fator * multDefesa * fatorDificuldade));

        return new Monstro(nome, ataque, vida, defesa, Elemento.NENHUM, regenera);
    }
}