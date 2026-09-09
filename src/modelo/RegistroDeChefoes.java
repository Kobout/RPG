package modelo;

import java.util.HashSet;
import java.util.Set;

public class RegistroDeChefoes {

    private static final Set<String> derrotadosAntes = new HashSet<>();

    private RegistroDeChefoes() {
    }

    public static boolean primeiraVez(String nomeChefao) {
        return !derrotadosAntes.contains(nomeChefao);
    }

    public static void marcarDerrotado(String nomeChefao) {
        derrotadosAntes.add(nomeChefao);
    }

    public static Set<String> exportar() {
        return new HashSet<>(derrotadosAntes);
    }

    public static void importar(Set<String> dados) {
        derrotadosAntes.clear();
        if (dados != null) {
            derrotadosAntes.addAll(dados);
        }
    }
}
