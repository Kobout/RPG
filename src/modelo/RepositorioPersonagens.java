package modelo;

import java.util.ArrayList;
import java.util.List;

public class RepositorioPersonagens {

    private static final List<Personagem> personagens = new ArrayList<>();

    private RepositorioPersonagens() {
        // classe utilitária, não deve ser instanciada
    }

    public static void adicionar(Personagem personagem) {
        personagens.add(personagem);
    }

    public static List<Personagem> listar() {
        return personagens;
    }
}