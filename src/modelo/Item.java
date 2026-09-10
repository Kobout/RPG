package modelo;

import java.io.Serializable;
import java.util.Random;

public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TipoItem {
        CAPACETE, ARMADURA, BOTAS, LUVAS, ANEL, AMULETO, ARMA
    }

    public enum Raridade {
        NORMAL(1.0),
        INCOMUM(1.5),
        RARO(2.5),
        EPICO(4.0),
        LENDARIO(6.0);

        private final double multiplicador;

        Raridade(double multiplicador) {
            this.multiplicador = multiplicador;
        }

        public double getMultiplicador() {
            return multiplicador;
        }
    }

    private static final String[] NOMES_CONJUNTO = {"Fúria", "Sombra", "Guardião", "Fênix", "Titã"};

    private final String nome;
    private final TipoItem tipo;
    private final Raridade raridade;
    private final int bonusAtaque;
    private final int bonusVida;
    private final int bonusDefesa;
    private final Elemento resistenciaElemento;
    private final Elemento fraquezaElemento;
    private final Elemento elementoArma;
    private final boolean rouboDeVida;
    private final String conjunto;
    private final boolean esquivaExtra;
    private final boolean regeneracaoMana;

    public Item(String nome, TipoItem tipo, Raridade raridade, int bonusAtaque, int bonusVida, int bonusDefesa) {
        this(nome, tipo, raridade, bonusAtaque, bonusVida, bonusDefesa,
                Elemento.NENHUM, Elemento.NENHUM, Elemento.NENHUM, false, null, false, false);
    }

    public Item(String nome, TipoItem tipo, Raridade raridade, int bonusAtaque, int bonusVida, int bonusDefesa,
                Elemento resistenciaElemento, Elemento fraquezaElemento) {
        this(nome, tipo, raridade, bonusAtaque, bonusVida, bonusDefesa,
                resistenciaElemento, fraquezaElemento, Elemento.NENHUM, false, null, false, false);
    }

    public Item(String nome, TipoItem tipo, Raridade raridade, int bonusAtaque, int bonusVida, int bonusDefesa,
                Elemento resistenciaElemento, Elemento fraquezaElemento, Elemento elementoArma, boolean rouboDeVida) {
        this(nome, tipo, raridade, bonusAtaque, bonusVida, bonusDefesa,
                resistenciaElemento, fraquezaElemento, elementoArma, rouboDeVida, null, false, false);
    }

    public Item(String nome, TipoItem tipo, Raridade raridade, int bonusAtaque, int bonusVida, int bonusDefesa,
                Elemento resistenciaElemento, Elemento fraquezaElemento, Elemento elementoArma, boolean rouboDeVida,
                String conjunto, boolean esquivaExtra, boolean regeneracaoMana) {
        this.nome = nome;
        this.tipo = tipo;
        this.raridade = raridade;
        this.bonusAtaque = bonusAtaque;
        this.bonusVida = bonusVida;
        this.bonusDefesa = bonusDefesa;
        this.resistenciaElemento = resistenciaElemento;
        this.fraquezaElemento = fraquezaElemento;
        this.elementoArma = elementoArma;
        this.rouboDeVida = rouboDeVida;
        this.conjunto = conjunto;
        this.esquivaExtra = esquivaExtra;
        this.regeneracaoMana = regeneracaoMana;
    }

    public String getNome() {
        return nome;
    }

    public TipoItem getTipo() {
        return tipo;
    }

    public Raridade getRaridade() {
        return raridade;
    }

    public int getBonusAtaque() {
        return bonusAtaque;
    }

    public int getBonusVida() {
        return bonusVida;
    }

    public int getBonusDefesa() {
        return bonusDefesa;
    }

    public Elemento getResistenciaElemento() {
        return resistenciaElemento;
    }

    public Elemento getFraquezaElemento() {
        return fraquezaElemento;
    }

    public Elemento getElementoArma() {
        return elementoArma;
    }

    public boolean isRouboDeVida() {
        return rouboDeVida;
    }

    public String getConjunto() {
        return conjunto;
    }

    public boolean isEsquivaExtra() {
        return esquivaExtra;
    }

    public boolean isRegeneracaoMana() {
        return regeneracaoMana;
    }

    @Override
    public String toString() {
        String texto = nome + " [" + tipo + " - " + raridade + "] (+" + bonusAtaque + " Atq, +"
                + bonusVida + " Vida, +" + bonusDefesa + " Def)";
        if (resistenciaElemento != Elemento.NENHUM) {
            texto += " [Resistência: " + resistenciaElemento + "]";
        }
        if (fraquezaElemento != Elemento.NENHUM) {
            texto += " [Fraqueza: " + fraquezaElemento + "]";
        }
        if (elementoArma != Elemento.NENHUM) {
            texto += " [Elemento da Arma: " + elementoArma + "]";
        }
        if (rouboDeVida) {
            texto += " [Roubo de Vida: 7%]";
        }
        if (esquivaExtra) {
            texto += " [Esquiva Extra]";
        }
        if (regeneracaoMana) {
            texto += " [Regenera Mana]";
        }
        if (conjunto != null) {
            texto += " [Conjunto: " + conjunto + "]";
        }
        return texto;
    }

    // Sorteia um item novo, com tipo e raridade aleatórios
    public static Item gerarAleatorio(boolean deChefao) {
        Random random = new Random();
        TipoItem tipo = TipoItem.values()[random.nextInt(TipoItem.values().length)];
        Raridade raridade = sortearRaridade(random, deChefao);
        return construir(tipo, raridade, random);
    }

    // Gera um item com tipo e raridade JÁ DEFINIDOS (usado na forja da Loja)
    public static Item gerarComRaridadeETipo(TipoItem tipo, Raridade raridade) {
        return construir(tipo, raridade, new Random());
    }

    private static Item construir(TipoItem tipo, Raridade raridade, Random random) {
        String nome = nomePorTipo(tipo);

        int baseAtaque;
        int baseVida;
        int baseDefesa;

        switch (tipo) {
            case ARMA:
                baseAtaque = 6; baseVida = 1; baseDefesa = 1;
                break;
            case ARMADURA:
                baseAtaque = 1; baseVida = 10; baseDefesa = 5;
                break;
            case CAPACETE:
                baseAtaque = 1; baseVida = 6; baseDefesa = 4;
                break;
            case BOTAS:
                baseAtaque = 3; baseVida = 5; baseDefesa = 2;
                break;
            case LUVAS:
                baseAtaque = 4; baseVida = 2; baseDefesa = 2;
                break;
            case ANEL:
                baseAtaque = 3; baseVida = 4; baseDefesa = 2;
                break;
            case AMULETO:
                baseAtaque = 2; baseVida = 8; baseDefesa = 2;
                break;
            default:
                baseAtaque = 2; baseVida = 4; baseDefesa = 2;
        }

        int bonusAtaque = (int) Math.round((baseAtaque + random.nextInt(3)) * raridade.getMultiplicador());
        int bonusVida = (int) Math.round((baseVida + random.nextInt(5)) * raridade.getMultiplicador());
        int bonusDefesa = (int) Math.round((baseDefesa + random.nextInt(3)) * raridade.getMultiplicador());

        Elemento resistencia = Elemento.NENHUM;
        Elemento fraqueza = Elemento.NENHUM;
        double sorteioElemento = random.nextDouble();
        if (sorteioElemento < 0.20) {
            resistencia = elementoAleatorio(random);
        } else if (sorteioElemento < 0.30) {
            fraqueza = elementoAleatorio(random);
        }

        Elemento elementoArma = Elemento.NENHUM;
        if (tipo == TipoItem.ARMA && random.nextDouble() < 0.25) {
            elementoArma = elementoAleatorio(random);
        }

        boolean rouboDeVida = tipo == TipoItem.LUVAS && random.nextDouble() < 0.10;
        boolean esquivaExtra = tipo == TipoItem.BOTAS && random.nextDouble() < 0.10;
        boolean regeneracaoMana = tipo == TipoItem.AMULETO && random.nextDouble() < 0.10;

        String conjunto = null;
        if (random.nextDouble() < 0.20) {
            conjunto = NOMES_CONJUNTO[random.nextInt(NOMES_CONJUNTO.length)];
            nome = nome + " do " + conjunto;
        }

        return new Item(nome, tipo, raridade, bonusAtaque, bonusVida, bonusDefesa,
                resistencia, fraqueza, elementoArma, rouboDeVida, conjunto, esquivaExtra, regeneracaoMana);
    }

    private static Elemento elementoAleatorio(Random random) {
        Elemento[] opcoes = {Elemento.FOGO, Elemento.GELO, Elemento.VENENO};
        return opcoes[random.nextInt(opcoes.length)];
    }

    private static Raridade sortearRaridade(Random random, boolean deChefao) {
        double sorteio = random.nextDouble();

        if (deChefao) {
            // Chefões nunca dropam itens abaixo de Raro
            if (sorteio < 0.50) return Raridade.RARO;
            if (sorteio < 0.85) return Raridade.EPICO;
            return Raridade.LENDARIO;
        }

        if (sorteio < 0.50) return Raridade.NORMAL;
        if (sorteio < 0.80) return Raridade.INCOMUM;
        if (sorteio < 0.95) return Raridade.RARO;
        if (sorteio < 0.99) return Raridade.EPICO;
        return Raridade.LENDARIO;
    }

    // Item único e garantido na primeira vez que cada chefão específico é derrotado
    public static Item itemUnicoDoChefao(String nomeChefao) {
        switch (nomeChefao) {
            case "Senhor dos Lobos":
                return new Item("Presa do Lobo", TipoItem.ARMA, Raridade.EPICO, 20, 10, 5,
                        Elemento.NENHUM, Elemento.NENHUM, Elemento.VENENO, false);
            case "Dragão Ancião":
                return new Item("Escama de Dragão", TipoItem.ARMADURA, Raridade.LENDARIO, 10, 40, 20,
                        Elemento.FOGO, Elemento.NENHUM);
            case "Rei Esqueleto":
                return new Item("Coroa Óssea", TipoItem.CAPACETE, Raridade.EPICO, 8, 25, 12);
            case "Golem de Pedra":
                return new Item("Núcleo do Golem", TipoItem.AMULETO, Raridade.EPICO, 5, 35, 15,
                        Elemento.GELO, Elemento.NENHUM);
            case "Lich Sombrio":
                return new Item("Cajado do Lich", TipoItem.ARMA, Raridade.LENDARIO, 25, 5, 5,
                        Elemento.NENHUM, Elemento.NENHUM, Elemento.FOGO, false);
            default:
                return gerarAleatorio(true);
        }
    }

    private static String nomePorTipo(TipoItem tipo) {
        switch (tipo) {
            case CAPACETE: return "Elmo";
            case ARMADURA: return "Armadura";
            case BOTAS: return "Botas";
            case LUVAS: return "Luvas";
            case ANEL: return "Anel";
            case AMULETO: return "Amuleto";
            case ARMA: return "Arma";
            default: return "Item";
        }
    }
}