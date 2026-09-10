package modelo;

import java.util.Random;

public class Mago extends Personagem {

    private int mana;
    private final int manaMaxima;
    private int pocoesMana = 3;

    private static final int CUSTO_BOLA_DE_FOGO = Configuracao.getInt("mago.custoBolaDeFogo", 15);
    private static final int CUSTO_FLECHA_DE_GELO = Configuracao.getInt("mago.custoFlechaDeGelo", 10);
    private static final double CHANCE_QUEIMADURA = Configuracao.getDouble("mago.chanceQueimadura", 0.3);
    private static final double CHANCE_CONGELAMENTO = Configuracao.getDouble("mago.chanceCongelamento", 0.2);
    private static final Random RANDOM = new Random();

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
        double modificador = alvo.getModificadorDano(Elemento.FOGO);
        int poder = (int) Math.round(this.getAtaque() * 2 * modificador);
        int danoAplicado = aplicarDano(alvo, poder);

        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " conjura uma Bola de Fogo em " + alvo.getNome()
                    + ", mas " + alvo.getNome() + " BLOQUEOU o golpe! Mana restante: " + mana + "/" + manaMaxima;
        }
        if (alvo.isUltimoAtaqueEsquivado()) {
            return nome + " conjura uma Bola de Fogo em " + alvo.getNome()
                    + ", mas " + alvo.getNome() + " ESQUIVOU do golpe! Mana restante: " + mana + "/" + manaMaxima;
        }

        String extra = descreverModificador(modificador);
        if (RANDOM.nextDouble() < CHANCE_QUEIMADURA) {
            int danoQueimadura = Math.max(1, (int) Math.round(this.getAtaque() * 0.15));
            alvo.aplicarQueimadura(2, danoQueimadura);
            extra += " " + alvo.getNome() + " está queimando!";
        }

        return nome + " conjura uma Bola de Fogo em " + alvo.getNome() + extra
                + " (custo: " + CUSTO_BOLA_DE_FOGO + " mana) e causa " + danoAplicado
                + " de dano! Mana restante: " + mana + "/" + manaMaxima;
    }

    public String atacarFlechaDeGelo(Personagem alvo) {
        mana -= CUSTO_FLECHA_DE_GELO;
        double modificador = alvo.getModificadorDano(Elemento.GELO);
        int poder = (int) Math.round(this.getAtaque() * 1.5 * modificador);
        int danoAplicado = aplicarDano(alvo, poder);

        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " conjura uma Flecha de Gelo em " + alvo.getNome()
                    + ", mas " + alvo.getNome() + " BLOQUEOU o golpe! Mana restante: " + mana + "/" + manaMaxima;
        }
        if (alvo.isUltimoAtaqueEsquivado()) {
            return nome + " conjura uma Flecha de Gelo em " + alvo.getNome()
                    + ", mas " + alvo.getNome() + " ESQUIVOU do golpe! Mana restante: " + mana + "/" + manaMaxima;
        }

        String extra = descreverModificador(modificador);
        if (RANDOM.nextDouble() < CHANCE_CONGELAMENTO) {
            alvo.aplicarCongelamento(1);
            extra += " " + alvo.getNome() + " foi CONGELADO!";
        }

        return nome + " conjura uma Flecha de Gelo em " + alvo.getNome() + extra
                + " (custo: " + CUSTO_FLECHA_DE_GELO + " mana) e causa " + danoAplicado
                + " de dano! Mana restante: " + mana + "/" + manaMaxima;
    }

    public String atacarArmaNormal(Personagem alvo) {
        Elemento elemento = getElementoArma();
        double modificador = alvo.getModificadorDano(elemento);
        int poder = (int) Math.round(this.getAtaque() * 0.6 * modificador);
        int danoAplicado = aplicarDano(alvo, poder);

        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " ataca " + alvo.getNome() + " com o cajado, mas "
                    + alvo.getNome() + " BLOQUEOU o golpe!";
        }
        if (alvo.isUltimoAtaqueEsquivado()) {
            return nome + " ataca " + alvo.getNome() + " com o cajado, mas "
                    + alvo.getNome() + " ESQUIVOU do golpe!";
        }

        String extra = "";
        if (elemento == Elemento.VENENO) {
            int danoVeneno = Math.max(1, (int) Math.round(this.getAtaque() * 0.1));
            alvo.aplicarVeneno(2, danoVeneno);
            extra = " (veneno da arma)";
        } else if (modificador > 1.0) {
            extra = " (SUPER EFETIVO!)";
        } else if (modificador < 1.0 && elemento != Elemento.NENHUM) {
            extra = " (resistido)";
        }

        return nome + " ataca " + alvo.getNome() + " com o cajado, causando " + danoAplicado + " de dano!" + extra;
    }

    private String descreverModificador(double modificador) {
        if (modificador > 1.0) {
            return " (SUPER EFETIVO!)";
        } else if (modificador < 1.0) {
            return " (resistido)";
        }
        return "";
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

    // Regenera mana no início do turno do Mago, se ele tiver um amuleto de regeneração equipado.
    // Retorna uma mensagem de log, ou null se nada aconteceu.
    public String regenerarManaPorTurno() {
        if (temAmuletoDeRegeneracao() && mana < manaMaxima) {
            int quantidade = Configuracao.getInt("amuleto.regeneracaoManaPorTurno", 5);
            int antes = mana;
            mana = Math.min(manaMaxima, mana + quantidade);
            if (mana > antes) {
                return nome + " regenera " + (mana - antes) + " de mana pelo amuleto!";
            }
        }
        return null;
    }

    private boolean temAmuletoDeRegeneracao() {
        for (Item item : getItensEquipados()) {
            if (item.getTipo() == Item.TipoItem.AMULETO && item.isRegeneracaoMana()) {
                return true;
            }
        }
        return false;
    }
}