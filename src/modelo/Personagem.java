package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class Personagem implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String nome;
    protected int ataque;
    protected int vidaMaxima;
    protected int vida;
    protected int defesa;
    protected int nivel;

    // Slots de equipamento: 1 de cada tipo, exceto anel (2 slots à parte)
    protected Map<Item.TipoItem, Item> equipamentos = new EnumMap<>(Item.TipoItem.class);
    protected Item[] aneis = new Item[2];
    protected List<Item> inventario = new ArrayList<>();

    protected boolean enfrentaChefaoNaProximaBatalha;
    protected int pocoesVida = 3;
    protected int monstrosDerrotados = 0;

    // Efeitos de status: veneno causa dano por alguns turnos, atordoamento faz perder o turno
    protected int turnosVeneno = 0;
    protected int danoVenenoPorTurno = 0;
    protected int turnosAtordoado = 0;

    public Personagem(String nome, int ataque, int vida, int defesa) {
        this.nome = nome;
        this.ataque = ataque;
        this.vidaMaxima = vida;
        this.vida = vida;
        this.defesa = defesa;
        this.nivel = 1;
    }

    public String getNome() {
        return nome;
    }

    // Ataque, defesa e vida máxima somam o bônus de todo equipamento usado
    public int getAtaque() {
        return ataque + somarBonusAtaque();
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima + somarBonusVida();
    }

    public int getDefesa() {
        return defesa + somarBonusDefesa();
    }

    public int getNivel() {
        return nivel;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public void curarTotalmente() {
        this.vida = getVidaMaxima();
    }

    public boolean isEnfrentaChefaoNaProximaBatalha() {
        return enfrentaChefaoNaProximaBatalha;
    }

    public void setEnfrentaChefaoNaProximaBatalha(boolean valor) {
        this.enfrentaChefaoNaProximaBatalha = valor;
    }

    public int getPocoesVida() {
        return pocoesVida;
    }

    public void adicionarPocaoVida() {
        pocoesVida++;
    }

    // Usa uma poção de vida, restaurando 30% da vida máxima. Retorna false se não houver poções.
    public boolean usarPocaoVida() {
        if (pocoesVida <= 0) {
            return false;
        }
        pocoesVida--;
        double percentual = Configuracao.getDouble("pocao.percentual", 0.3);
        int cura = (int) Math.round(getVidaMaxima() * percentual);
        vida = Math.min(getVidaMaxima(), vida + cura);
        return true;
    }

    public List<Item> getInventario() {
        return inventario;
    }

    public void adicionarItem(Item item) {
        inventario.add(item);
    }

    public boolean removerItem(Item item) {
        return inventario.remove(item);
    }

    public boolean estaEquipado(Item item) {
        if (equipamentos.containsValue(item)) {
            return true;
        }
        for (Item anel : aneis) {
            if (anel == item) {
                return true;
            }
        }
        return false;
    }

    public int getMonstrosDerrotados() {
        return monstrosDerrotados;
    }

    public void registrarVitoriaContraMonstro() {
        monstrosDerrotados++;
    }

    public boolean isEnvenenado() {
        return turnosVeneno > 0;
    }

    // Equipa um item no slot correspondente ao seu tipo (anéis vão para o slot 0)
    public void equipar(Item item) {
        equipar(item, 0);
    }

    // Equipa um item; slotAnel (0 ou 1) só é usado quando o item é do tipo ANEL
    public void equipar(Item item, int slotAnel) {
        if (item.getTipo() == Item.TipoItem.ANEL) {
            int slot = (slotAnel == 1) ? 1 : 0;
            aneis[slot] = item;
        } else {
            equipamentos.put(item.getTipo(), item);
        }

        if (vida > getVidaMaxima()) {
            vida = getVidaMaxima();
        }
    }

    public Item getEquipado(Item.TipoItem tipo) {
        return equipamentos.get(tipo);
    }

    public Item getAnelEquipado(int slot) {
        if (slot != 0 && slot != 1) {
            return null;
        }
        return aneis[slot];
    }

    private int somarBonusAtaque() {
        int total = 0;
        for (Item item : equipamentos.values()) {
            total += item.getBonusAtaque();
        }
        for (Item anel : aneis) {
            if (anel != null) {
                total += anel.getBonusAtaque();
            }
        }
        return total;
    }

    private int somarBonusVida() {
        int total = 0;
        for (Item item : equipamentos.values()) {
            total += item.getBonusVida();
        }
        for (Item anel : aneis) {
            if (anel != null) {
                total += anel.getBonusVida();
            }
        }
        return total;
    }

    private int somarBonusDefesa() {
        int total = 0;
        for (Item item : equipamentos.values()) {
            total += item.getBonusDefesa();
        }
        for (Item anel : aneis) {
            if (anel != null) {
                total += anel.getBonusDefesa();
            }
        }
        return total;
    }

    // Cada classe filha implementa sua própria forma de atacar (herança + polimorfismo)
    public abstract String atacar(Personagem alvo);

    public void aplicarVeneno(int turnos, int danoPorTurno) {
        this.turnosVeneno = turnos;
        this.danoVenenoPorTurno = danoPorTurno;
    }

    public void aplicarAtordoamento(int turnos) {
        this.turnosAtordoado = turnos;
    }

    public boolean estaAtordoado() {
        return turnosAtordoado > 0;
    }

    // Processa veneno e atordoamento no início do turno deste personagem.
    // Retorna uma mensagem de log, ou null se nada aconteceu.
    public String processarStatusInicioDeTurno() {
        StringBuilder msg = new StringBuilder();

        if (turnosVeneno > 0) {
            vida = Math.max(0, vida - danoVenenoPorTurno);
            turnosVeneno--;
            msg.append(nome).append(" sofre ").append(danoVenenoPorTurno).append(" de dano de veneno!");
        }

        if (turnosAtordoado > 0) {
            turnosAtordoado--;
            if (msg.length() > 0) {
                msg.append(" ");
            }
            msg.append(nome).append(" está atordoado e perde o turno!");
        }

        return msg.length() > 0 ? msg.toString() : null;
    }

    // Calcula e aplica o dano no alvo a partir de um "poder de ataque" bruto.
    // A defesa do alvo reduz o dano de forma percentual (nunca zera o dano por completo,
    // mesmo quando a defesa cresce muito mais rápido que o ataque de quem bate).
    protected int aplicarDano(Personagem alvo, int poderAtaque) {
        double mitigacao = 100.0 / (100.0 + alvo.getDefesa());
        int danoFinal = Math.max((int) Math.round(poderAtaque * mitigacao), 1);
        alvo.vida -= danoFinal;
        if (alvo.vida < 0) {
            alvo.vida = 0;
        }
        return danoFinal;
    }

    // Sobe de nível: +10% em ataque, vida máxima e defesa; cura totalmente.
    // A cada 5 níveis, marca que a próxima batalha será contra um chefão.
    public String subirNivel() {
        nivel++;
        ataque = (int) Math.round(ataque * 1.1);
        vidaMaxima = (int) Math.round(vidaMaxima * 1.1);
        defesa = (int) Math.round(defesa * 1.1);
        vida = getVidaMaxima();

        if (nivel % 5 == 0) {
            enfrentaChefaoNaProximaBatalha = true;
        }

        return nome + " subiu para o nível " + nivel + "! (Ataque: " + getAtaque()
                + ", Vida: " + getVidaMaxima() + ", Defesa: " + getDefesa() + ")";
    }

    @Override
    public String toString() {
        return nome + " (" + getClass().getSimpleName() + ") - Nível: " + nivel
                + ", Vida: " + vida + "/" + getVidaMaxima()
                + ", Ataque: " + getAtaque() + ", Defesa: " + getDefesa();
    }
}
