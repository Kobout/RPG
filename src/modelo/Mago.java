package modelo;

public class Mago extends Personagem {

    private int mana;
    private final int manaMaxima;
    private int pocoesMana = 3;

    private static final int CUSTO_BOLA_DE_FOGO = Configuracao.getInt("mago.custoBolaDeFogo", 15);
    private static final int CUSTO_FLECHA_DE_GELO = Configuracao.getInt("mago.custoFlechaDeGelo", 10);
    private static final double BONUS_ELEMENTO_FRACO = Configuracao.getDouble("mago.bonusElementoFraco", 1.5);

    public Mago(String nome, int mana, int ataque, int vida, int defesa) {
        super(nome, ataque, vida, defesa);
        this.mana = mana;
        this.manaMaxima = mana;
    }

    public int getMana() {
        return mana;
    }

    public int getManaMaxima() {
        return manaMaxima;
    }

    public boolean temManaParaBolaDeFogo() {
        return mana >= CUSTO_BOLA_DE_FOGO;
    }

    public boolean temManaParaFlechaDeGelo() {
        return mana >= CUSTO_FLECHA_DE_GELO;
    }

    public String atacarBolaDeFogo(Personagem alvo) {
        mana -= CUSTO_BOLA_DE_FOGO;
        double bonus = temFraqueza(alvo, Elemento.FOGO) ? BONUS_ELEMENTO_FRACO : 1.0;
        int poder = (int) Math.round(this.getAtaque() * 2 * bonus);
        int danoAplicado = aplicarDano(alvo, poder);
        String extra = bonus > 1.0 ? " (SUPER EFETIVO!)" : "";
        return nome + " conjura uma Bola de Fogo em " + alvo.getNome() + extra
                + " (custo: " + CUSTO_BOLA_DE_FOGO + " mana) e causa " + danoAplicado
                + " de dano! Mana restante: " + mana + "/" + manaMaxima;
    }

    public String atacarFlechaDeGelo(Personagem alvo) {
        mana -= CUSTO_FLECHA_DE_GELO;
        double bonus = temFraqueza(alvo, Elemento.GELO) ? BONUS_ELEMENTO_FRACO : 1.0;
        int poder = (int) Math.round(this.getAtaque() * 1.5 * bonus);
        int danoAplicado = aplicarDano(alvo, poder);
        String extra = bonus > 1.0 ? " (SUPER EFETIVO!)" : "";
        return nome + " conjura uma Flecha de Gelo em " + alvo.getNome() + extra
                + " (custo: " + CUSTO_FLECHA_DE_GELO + " mana) e causa " + danoAplicado
                + " de dano! Mana restante: " + mana + "/" + manaMaxima;
    }

    private boolean temFraqueza(Personagem alvo, Elemento elemento) {
        return (alvo instanceof Monstro) && ((Monstro) alvo).getFraqueza() == elemento;
    }

    public String atacarArmaNormal(Personagem alvo) {
        int danoAplicado = aplicarDano(alvo, (int) Math.round(this.getAtaque() * 0.6));
        return nome + " ataca " + alvo.getNome() + " com o cajado, causando " + danoAplicado + " de dano!";
    }

    @Override
    public String atacar(Personagem alvo) {
        return atacarArmaNormal(alvo);
    }

    // Restaura 30% da mana máxima. Chamado ao vencer uma batalha.
    public void restaurarManaAposBatalha() {
        int recuperar = (int) Math.round(manaMaxima * 0.3);
        mana = Math.min(manaMaxima, mana + recuperar);
    }

    // Restaura a mana por completo. Chamado quando o mago morre em batalha.
    public void restaurarManaTotal() {
        mana = manaMaxima;
    }

    public int getPocoesMana() {
        return pocoesMana;
    }

    public void adicionarPocaoMana() {
        pocoesMana++;
    }

    // Usa uma poção de mana, restaurando 30% da mana máxima. Retorna false se não houver poções.
    public boolean usarPocaoMana() {
        if (pocoesMana <= 0) {
            return false;
        }
        pocoesMana--;
        int recuperar = (int) Math.round(manaMaxima * 0.3);
        mana = Math.min(manaMaxima, mana + recuperar);
        return true;
    }
}
