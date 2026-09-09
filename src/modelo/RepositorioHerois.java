package modelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RepositorioHerois {

    private static final String ARQUIVO_SAVE = "save.dat";
    private static final List<Personagem> herois = new ArrayList<>();

    static {
        carregar();
    }

    private RepositorioHerois() {
        // classe utilitária, não deve ser instanciada
    }

    public static List<Personagem> listar() {
        return herois;
    }

    // Salva o progresso (heróis com seus níveis/itens/poções + chefões já derrotados) em disco
    public static void salvar() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARQUIVO_SAVE))) {
            out.writeObject(new ArrayList<>(herois));
            out.writeObject(RegistroDeChefoes.exportar());
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o progresso: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void carregar() {
        File arquivo = new File(ARQUIVO_SAVE);
        if (!arquivo.exists()) {
            criarHeroisPadrao();
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(arquivo))) {
            List<Personagem> carregados = (List<Personagem>) in.readObject();
            herois.clear();
            herois.addAll(carregados);

            Set<String> chefoesDerrotados = (Set<String>) in.readObject();
            RegistroDeChefoes.importar(chefoesDerrotados);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Não foi possível carregar o save, iniciando um novo progresso: " + e.getMessage());
            criarHeroisPadrao();
        }
    }

    private static void criarHeroisPadrao() {
        herois.clear();
        herois.add(new Guerreiro("Thorin", "Machado de Guerra", 25, 120, 18));
        herois.add(new Mago("Elysia", 60, 30, 70, 8));
        herois.add(new Arqueiro("Kael", 15, 26, 105, 13));
    }
}
