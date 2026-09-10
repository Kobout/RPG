package modelo;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProgressoDificuldade {

    private static final String[] TODOS_CHEFOES = {
        "Dragão Ancião", "Rei Esqueleto", "Golem de Pedra", "Senhor dos Lobos", "Lich Sombrio"
    };

    private static Dificuldade dificuldadeAtual = Dificuldade.FACIL;
    private static Dificuldade maiorDesbloqueada = Dificuldade.FACIL;
    private static final Map<Dificuldade, Set<String>> chefoesPorDificuldade = new EnumMap<>(Dificuldade.class);

    private ProgressoDificuldade() {
    }

    public static Dificuldade getAtual() {
        return dificuldadeAtual;
    }

    public static Dificuldade getMaiorDesbloqueada() {
        return maiorDesbloqueada;
    }

    public static boolean estaDesbloqueada(Dificuldade dificuldade) {
        return dificuldade.ordinal() <= maiorDesbloqueada.ordinal();
    }

    public static void selecionar(Dificuldade dificuldade) {
        if (estaDesbloqueada(dificuldade)) {
            dificuldadeAtual = dificuldade;
        }
    }

    // Quantos dos 5 chefões já foram derrotados NA DIFICULDADE ATUAL
    public static int getChefoesDerrotadosNaDificuldadeAtual() {
        Set<String> conjunto = chefoesPorDificuldade.get(dificuldadeAtual);
        return conjunto == null ? 0 : conjunto.size();
    }

    // Chamado quando um chefão é derrotado. Retorna o nome de exibição da nova dificuldade
    // se ela acabou de ser desbloqueada agora, ou null caso contrário.
    public static String registrarChefaoDerrotado(String nomeChefao) {
        Set<String> conjunto = chefoesPorDificuldade.computeIfAbsent(dificuldadeAtual, d -> new HashSet<>());
        conjunto.add(nomeChefao);

        boolean completou = true;
        for (String nome : TODOS_CHEFOES) {
            if (!conjunto.contains(nome)) {
                completou = false;
                break;
            }
        }

        Dificuldade[] valores = Dificuldade.values();
        if (completou && dificuldadeAtual == maiorDesbloqueada && maiorDesbloqueada.ordinal() < valores.length - 1) {
            maiorDesbloqueada = valores[maiorDesbloqueada.ordinal() + 1];
            return maiorDesbloqueada.getNomeExibicao();
        }
        return null;
    }

    // ---- Persistência ----

    public static EstadoSalvo exportarEstado() {
        EstadoSalvo estado = new EstadoSalvo();
        estado.dificuldadeAtual = dificuldadeAtual.ordinal();
        estado.maiorDesbloqueada = maiorDesbloqueada.ordinal();
        estado.progresso = new HashMap<>();
        for (Map.Entry<Dificuldade, Set<String>> entrada : chefoesPorDificuldade.entrySet()) {
            estado.progresso.put(entrada.getKey().ordinal(), new HashSet<>(entrada.getValue()));
        }
        return estado;
    }

    public static void importarEstado(Object objeto) {
        if (!(objeto instanceof EstadoSalvo)) {
            return;
        }
        EstadoSalvo estado = (EstadoSalvo) objeto;
        Dificuldade[] valores = Dificuldade.values();
        dificuldadeAtual = valores[limitar(estado.dificuldadeAtual, valores.length)];
        maiorDesbloqueada = valores[limitar(estado.maiorDesbloqueada, valores.length)];
        chefoesPorDificuldade.clear();
        if (estado.progresso != null) {
            for (Map.Entry<Integer, Set<String>> entrada : estado.progresso.entrySet()) {
                chefoesPorDificuldade.put(valores[limitar(entrada.getKey(), valores.length)], entrada.getValue());
            }
        }
    }

    private static int limitar(int valor, int tamanho) {
        return Math.max(0, Math.min(valor, tamanho - 1));
    }

    // Estrutura simples e serializável para salvar o progresso de dificuldade no arquivo
    public static class EstadoSalvo implements Serializable {
        private static final long serialVersionUID = 1L;
        int dificuldadeAtual;
        int maiorDesbloqueada;
        Map<Integer, Set<String>> progresso;
    }
}