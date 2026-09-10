package rede;

import java.io.Serializable;

public class MensagemRede implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        INFO_HEROI, ACAO
    }

    public Tipo tipo;

    // Usado quando tipo == INFO_HEROI (apresentação inicial do herói ao entrar na partida)
    public String nome;
    public String classe; // "Guerreiro", "Mago" ou "Arqueiro"
    public int nivel;
    public int ataque;
    public int defesa;
    public int vidaMaxima;
    public int vidaInicial;

    // Usado quando tipo == ACAO (ataque ou poção usada)
    public String textoLog;
    public int vidaRemetenteApos;    // vida de quem enviou a mensagem, depois da ação
    public int vidaDestinatarioApos; // vida de quem VAI RECEBER a mensagem, depois da ação
}