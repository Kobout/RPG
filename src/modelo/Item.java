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

    private final String nome;
    private final TipoItem tipo;
    private final Raridade raridade;
    private final int bonusAtaque;
    private final int bonusVida;
    private final int bonusDefesa;

    public Item(String nome, TipoItem tipo, Raridade raridade, int bonusAtaque, int bonusVida, int bonusDefesa) {
        this.nome = nome;
        this.tipo = tipo;
        this.raridade = raridade;
        this.bonusAtaque = bonusAtaque;
        this.bonusVida = bonusVida;
        this.bonusDefesa = bonusDefesa;
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

    @Override
    public String toString() {
        return nome + " [" + tipo + " - " + raridade + "] (+" + bonusAtaque + " Atq, +"
                + bonusVida + " Vida, +" + bonusDefesa + " Def)";
    }

    // Sorteia um item novo: tipo, raridade e status seguem uma faixa própria de cada tipo
    public static Item gerarAleatorio(boolean deChefao) {
        Random random = new Random();
        TipoItem tipo = TipoItem.values()[random.nextInt(TipoItem.values().length)];
        Raridade raridade = sortearRaridade(random, deChefao);
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

        return new Item(nome, tipo, raridade, bonusAtaque, bonusVida, bonusDefesa);
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
                return new Item("Presa do Lobo", TipoItem.ARMA, Raridade.EPICO, 20, 10, 5);
            case "Dragão Ancião":
                return new Item("Escama de Dragão", TipoItem.ARMADURA, Raridade.LENDARIO, 10, 40, 20);
            case "Rei Esqueleto":
                return new Item("Coroa Óssea", TipoItem.CAPACETE, Raridade.EPICO, 8, 25, 12);
            case "Golem de Pedra":
                return new Item("Núcleo do Golem", TipoItem.AMULETO, Raridade.EPICO, 5, 35, 15);
            case "Lich Sombrio":
                return new Item("Cajado do Lich", TipoItem.ARMA, Raridade.LENDARIO, 25, 5, 5);
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
