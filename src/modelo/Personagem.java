package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    protected int ouro = 50;
    protected int monstrosDerrotados = 0;

    // Efeitos de status: veneno/queimadura causam dano por turnos; atordoamento/congelamento fazem perder o turno
    protected int turnosVeneno = 0;
    protected int danoVenenoPorTurno = 0;
    protected int turnosQueimadura = 0;
    protected int danoQueimaduraPorTurno = 0;
    protected int turnosAtordoado = 0;
    protected int turnosCongelado = 0;
    protected boolean ultimoAtaqueBloqueado;
    protected boolean ultimoAtaqueEsquivado;
    protected int penalidadesFuga = 0;

    private static final Random RANDOM_ESQUIVA = new Random();

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

    // Cura uma fração da vida máxima (usado entre batalhas, no lugar da cura completa)
    public void curarParcial(double percentual) {
        int cura = (int) Math.round(getVidaMaxima() * percentual);
        this.vida = Math.min(getVidaMaxima(), this.vida + cura);
    }

    // Elemento da arma atualmente equipada (NENHUM se não houver arma ou ela for comum)
    public Elemento getElementoArma() {
        Item arma = equipamentos.get(Item.TipoItem.ARMA);
        return (arma != null) ? arma.getElementoArma() : Elemento.NENHUM;
    }

    // Percentual de roubo de vida concedido por luvas especiais equipadas (0 se não tiver)
    public double getPercentualRouboDeVida() {
        for (Item item : getItensEquipados()) {
            if (item.getTipo() == Item.TipoItem.LUVAS && item.isRouboDeVida()) {
                return 0.07;
            }
        }
        return 0.0;
    }

    // Usado pelo modo multiplayer para sincronizar a vida com o valor informado pela rede
    public void aplicarVidaExterna(int novaVida) {
        this.vida = Math.max(0, Math.min(novaVida, getVidaMaxima()));
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

    public int getOuro() {
        return ouro;
    }

    public void adicionarOuro(int quantidade) {
        ouro += quantidade;
    }

    // Gasta ouro se houver o suficiente. Retorna false (sem gastar nada) se não houver.
    public boolean gastarOuro(int quantidade) {
        if (ouro < quantidade) {
            return false;
        }
        ouro -= quantidade;
        return true;
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
        if (temConjuntoCompleto()) {
            total += Configuracao.getInt("conjunto.bonusAtaque", 10);
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
        if (temConjuntoCompleto()) {
            total += Configuracao.getInt("conjunto.bonusVida", 25);
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
        if (temConjuntoCompleto()) {
            total += Configuracao.getInt("conjunto.bonusDefesa", 10);
        }
        return total;
    }

    // Nome do conjunto ativo (2+ peças equipadas da mesma família), ou null se nenhum
    public String getConjuntoAtivo() {
        int minimo = Configuracao.getInt("conjunto.pecasNecessarias", 2);
        Map<String, Integer> contagem = new HashMap<>();
        for (Item item : getItensEquipados()) {
            if (item.getConjunto() != null) {
                contagem.merge(item.getConjunto(), 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> entrada : contagem.entrySet()) {
            if (entrada.getValue() >= minimo) {
                return entrada.getKey();
            }
        }
        return null;
    }

    private boolean temConjuntoCompleto() {
        return getConjuntoAtivo() != null;
    }

    // Chance de esquivar por completo de um ataque (vinda de botas especiais equipadas)
    public double getChanceEsquiva() {
        for (Item item : getItensEquipados()) {
            if (item.getTipo() == Item.TipoItem.BOTAS && item.isEsquivaExtra()) {
                return Configuracao.getDouble("botas.chanceEsquiva", 0.10);
            }
        }
        return 0.0;
    }

    public boolean isUltimoAtaqueEsquivado() {
        return ultimoAtaqueEsquivado;
    }

    // Registra uma fuga: a próxima vitória não vai conceder subida de nível
    public void registrarFuga() {
        penalidadesFuga++;
    }

    public int getPenalidadesFuga() {
        return penalidadesFuga;
    }

    public void consumirPenalidadeFuga() {
        if (penalidadesFuga > 0) {
            penalidadesFuga--;
        }
    }

    // Todos os itens atualmente equipados (usado pra brilho visual, ícones e bônus elementais)
    public List<Item> getItensEquipados() {
        List<Item> lista = new ArrayList<>(equipamentos.values());
        for (Item anel : aneis) {
            if (anel != null) {
                lista.add(anel);
            }
        }
        return lista;
    }

    // Multiplicador de dano recebido de um elemento, considerando resistência/fraqueza
    // dos itens equipados. Sobrescrito em Monstro para incluir a fraqueza fixa de chefões.
    public double getModificadorDano(Elemento elemento) {
        if (elemento == null || elemento == Elemento.NENHUM) {
            return 1.0;
        }
        double modificador = 1.0;
        for (Item item : getItensEquipados()) {
            if (item.getResistenciaElemento() == elemento) {
                modificador -= 0.25;
            }
            if (item.getFraquezaElemento() == elemento) {
                modificador += 0.25;
            }
        }
        return Math.max(0.25, modificador);
    }

    // Chance de bloquear um ataque por completo (dano zero). Só o Guerreiro tem essa passiva;
    // as demais classes usam o valor padrão (nunca bloqueia).
    protected boolean tentarBloquear() {
        return false;
    }

    public boolean isUltimoAtaqueBloqueado() {
        return ultimoAtaqueBloqueado;
    }

    // Cada classe filha implementa sua própria forma de atacar (herança + polimorfismo)
    public abstract String atacar(Personagem alvo);

    public void aplicarVeneno(int turnos, int danoPorTurno) {
        this.turnosVeneno = turnos;
        this.danoVenenoPorTurno = danoPorTurno;
    }

    public void aplicarQueimadura(int turnos, int danoPorTurno) {
        this.turnosQueimadura = turnos;
        this.danoQueimaduraPorTurno = danoPorTurno;
    }

    public void aplicarAtordoamento(int turnos) {
        this.turnosAtordoado = turnos;
    }

    public void aplicarCongelamento(int turnos) {
        this.turnosCongelado = turnos;
    }

    public boolean estaAtordoado() {
        return turnosAtordoado > 0;
    }

    public boolean estaCongelado() {
        return turnosCongelado > 0;
    }

    public boolean isEnvenenado() {
        return turnosVeneno > 0;
    }

    public boolean isQueimando() {
        return turnosQueimadura > 0;
    }

    // Processa veneno, queimadura, atordoamento e congelamento no início do turno.
    // Retorna uma mensagem de log, ou null se nada aconteceu.
    public String processarStatusInicioDeTurno() {
        StringBuilder msg = new StringBuilder();

        if (turnosVeneno > 0) {
            int danoAjustado = Math.max(1, (int) Math.round(danoVenenoPorTurno * getModificadorDano(Elemento.VENENO)));
            vida = Math.max(0, vida - danoAjustado);
            turnosVeneno--;
            msg.append(nome).append(" sofre ").append(danoAjustado).append(" de dano de veneno!");
        }

        if (turnosQueimadura > 0) {
            int danoAjustado = Math.max(1, (int) Math.round(danoQueimaduraPorTurno * getModificadorDano(Elemento.FOGO)));
            vida = Math.max(0, vida - danoAjustado);
            turnosQueimadura--;
            if (msg.length() > 0) {
                msg.append(" ");
            }
            msg.append(nome).append(" sofre ").append(danoAjustado).append(" de dano de queimadura!");
        }

        if (turnosAtordoado > 0) {
            turnosAtordoado--;
            if (msg.length() > 0) {
                msg.append(" ");
            }
            msg.append(nome).append(" está atordoado e perde o turno!");
        }

        if (turnosCongelado > 0) {
            turnosCongelado--;
            if (msg.length() > 0) {
                msg.append(" ");
            }
            msg.append(nome).append(" está congelado e perde o turno!");
        }

        return msg.length() > 0 ? msg.toString() : null;
    }

    // Calcula e aplica o dano no alvo a partir de um "poder de ataque" bruto.
    // A defesa do alvo reduz o dano de forma percentual (nunca zera o dano por completo,
    // mesmo quando a defesa cresce muito mais rápido que o ataque de quem bate).
    // Antes disso, o alvo tem uma chance de bloquear o golpe (passiva do Guerreiro).
    protected int aplicarDano(Personagem alvo, int poderAtaque) {
        alvo.ultimoAtaqueBloqueado = alvo.tentarBloquear();
        alvo.ultimoAtaqueEsquivado = !alvo.ultimoAtaqueBloqueado
                && alvo.getChanceEsquiva() > 0
                && RANDOM_ESQUIVA.nextDouble() < alvo.getChanceEsquiva();

        if (alvo.ultimoAtaqueBloqueado || alvo.ultimoAtaqueEsquivado) {
            return 0;
        }

        double mitigacao = 100.0 / (100.0 + alvo.getDefesa());
        int danoFinal = Math.max((int) Math.round(poderAtaque * mitigacao), 1);
        alvo.vida -= danoFinal;
        if (alvo.vida < 0) {
            alvo.vida = 0;
        }

        double percentualRoubo = this.getPercentualRouboDeVida();
        if (percentualRoubo > 0) {
            int curaRoubada = (int) Math.round(danoFinal * percentualRoubo);
            this.vida = Math.min(this.getVidaMaxima(), this.vida + curaRoubada);
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