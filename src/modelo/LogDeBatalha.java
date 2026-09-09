package modelo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogDeBatalha {

    private static final String ARQUIVO = "historico.log";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private LogDeBatalha() {
    }

    public static void registrar(Personagem heroi, String nomeInimigo, String resultado) {
        String linha = LocalDateTime.now().format(FORMATO) + " | " + heroi.getNome()
                + " (Nv " + heroi.getNivel() + ") vs " + nomeInimigo + " -> " + resultado;

        try (PrintWriter out = new PrintWriter(new FileWriter(ARQUIVO, true))) {
            out.println(linha);
        } catch (IOException e) {
            System.out.println("Não foi possível gravar o histórico: " + e.getMessage());
        }
    }
}
