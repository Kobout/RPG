package modelo;

public class Clerigo extends Personagem {

    private int fe;
    private final int feMaxima;

    private static final int CUSTO_CURA_DIVINA = Configuracao.getInt("clerigo.custoCuraDivina", 15);
    private static final int CUSTO_PURIFICAR = Configuracao.getInt("clerigo.custoPurificar", 10);

    public Clerigo(String nome, int fe, int ataque, int vida, int defesa) {
        super(nome, ataque, vida, defesa);
        this.fe = fe;
        this.feMaxima = fe;
    }

    public int getFe() {
        return fe;
    }

    public int getFeMaxima() {
        return feMaxima;
    }

    public boolean temFeParaCuraDivina() {
        return fe >= CUSTO_CURA_DIVINA;
    }

    public boolean temFeParaPurificar() {
        return fe >= CUSTO_PURIFICAR;
    }

    // Ataque físico leve com a maça sagrada — sempre disponível, sem custo de fé
    public String atacarSagrado(Personagem alvo) {
        Elemento elemento = getElementoArma();
        double modificador = alvo.getModificadorDano(elemento);
        int poder = (int) Math.round(this.getAtaque() * 0.8 * modificador);
        int danoAplicado = aplicarDano(alvo, poder);

        if (alvo.isUltimoAtaqueBloqueado()) {
            return nome + " golpeia " + alvo.getNome() + " com a maça sagrada, mas "
                    + alvo.getNome() + " BLOQUEOU o golpe!";
        }
        if (alvo.isUltimoAtaqueEsquivado()) {
            return nome + " golpeia " + alvo.getNome() + " com a maça sagrada, mas "
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

        return nome + " golpeia " + alvo.getNome() + " com a maça sagrada e causa "
                + danoAplicado + " de dano!" + extra;
    }

    // Cura uma boa parte da própria vida, custando fé
    public String curaDivina() {
        fe -= CUSTO_CURA_DIVINA;
        int vidaAntes = vida;
        int cura = (int) Math.round(getVidaMaxima() * 0.35);
        vida = Math.min(getVidaMaxima(), vida + cura);
        return nome + " conjura Cura Divina e recupera " + (vida - vidaAntes)
                + " de vida! Fé restante: " + fe + "/" + feMaxima;
    }

    // Remove veneno, queimadura, atordoamento e congelamento de si mesmo
    public String purificar() {
        fe -= CUSTO_PURIFICAR;
        turnosVeneno = 0;
        turnosQueimadura = 0;
        turnosAtordoado = 0;
        turnosCongelado = 0;
        return nome + " se purifica, removendo todos os efeitos negativos! Fé restante: " + fe + "/" + feMaxima;
    }

    @Override
    public String atacar(Personagem alvo) {
        return atacarSagrado(alvo);
    }

    // Restaura 30% da fé máxima. Chamado ao vencer uma batalha.
    public void restaurarFeAposBatalha() {
        int recuperar = (int) Math.round(feMaxima * 0.3);
        fe = Math.min(feMaxima, fe + recuperar);
    }

    // Restaura a fé por completo. Chamado quando o clérigo morre em batalha.
    public void restaurarFeTotal() {
        fe = feMaxima;
    }
}