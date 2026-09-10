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

    // Salva o progresso completo: heróis, chefões derrotados, dificuldade, conquistas e configurações
    public static void salvar() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARQUIVO_SAVE))) {
            out.writeObject(new ArrayList<>(herois));
            out.writeObject(RegistroDeChefoes.exportar());
            out.writeObject(ProgressoDificuldade.exportarEstado());
            out.writeObject(Conquistas.exportarItensForjados());
            out.writeObject(Conquistas.exportarVitoriasOnline());
            out.writeObject(Conquistas.exportarRecompensadas());
            out.writeObject(ConfiguracoesJogo.isSomSilenciado());
            out.writeObject(ConfiguracoesJogo.isMusicaSilenciada());
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

            // Dados adicionados em versões mais novas do save: cada um é lido em seu próprio
            // try, para que um save antigo (sem parte dos dados) ainda carregue o resto certinho.
            try {
                Object progresso = in.readObject();
                ProgressoDificuldade.importarEstado(progresso);
            } catch (Exception ignorada) {
                // save de antes do sistema de dificuldade
            }
            try {
                int itensForjados = (Integer) in.readObject();
                int vitoriasOnline = (Integer) in.readObject();
                Conquistas.importar(itensForjados, vitoriasOnline);
            } catch (Exception ignorada) {
                // save de antes do sistema de conquistas
            }
            try {
                Set<String> recompensadas = (Set<String>) in.readObject();
                Conquistas.importarRecompensadas(recompensadas);
            } catch (Exception ignorada) {
                // save de antes das recompensas de conquista
            }
            try {
                boolean somSilenciado = (Boolean) in.readObject();
                ConfiguracoesJogo.setSomSilenciado(somSilenciado);
            } catch (Exception ignorada) {
                // save de antes da tela de configurações
            }
            try {
                boolean musicaSilenciada = (Boolean) in.readObject();
                ConfiguracoesJogo.setMusicaSilenciada(musicaSilenciada);
            } catch (Exception ignorada) {
                // save de antes da trilha sonora
            }

            garantirClerigoExiste();
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
        herois.add(new Clerigo("Seraphina", 50, 24, 95, 14));
    }

    // Garante que jogadores com um save antigo (de antes do Clérigo existir) também o recebam
    private static void garantirClerigoExiste() {
        for (Personagem p : herois) {
            if (p instanceof Clerigo) {
                return;
            }
        }
        herois.add(new Clerigo("Seraphina", 50, 24, 95, 14));
    }
}