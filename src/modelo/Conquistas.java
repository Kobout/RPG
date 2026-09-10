package modelo;

import java.util.HashSet;
import java.util.Set;

public class Conquistas {

    private static int itensForjados = 0;
    private static int vitoriasOnline = 0;
    private static final Set<String> recompensadas = new HashSet<>();

    private Conquistas() {
    }

    public static void registrarForja() {
        itensForjados++;
    }

    public static int getItensForjados() {
        return itensForjados;
    }

    public static void registrarVitoriaOnline() {
        vitoriasOnline++;
    }

    public static int getVitoriasOnline() {
        return vitoriasOnline;
    }

    public static boolean jaRecompensada(String id) {
        return recompensadas.contains(id);
    }

    public static void marcarRecompensada(String id) {
        recompensadas.add(id);
    }

    public static int exportarItensForjados() {
        return itensForjados;
    }

    public static int exportarVitoriasOnline() {
        return vitoriasOnline;
    }

    public static Set<String> exportarRecompensadas() {
        return new HashSet<>(recompensadas);
    }

    public static void importar(int itensForjadosSalvos, int vitoriasOnlineSalvas) {
        itensForjados = itensForjadosSalvos;
        vitoriasOnline = vitoriasOnlineSalvas;
    }

    public static void importarRecompensadas(Set<String> dados) {
        recompensadas.clear();
        if (dados != null) {
            recompensadas.addAll(dados);
        }
    }
}