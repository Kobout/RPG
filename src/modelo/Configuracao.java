package modelo;

import java.io.InputStream;
import java.util.Properties;

public class Configuracao {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = Configuracao.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.out.println("config.properties não encontrado, usando valores padrão.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler config.properties, usando valores padrão: " + e.getMessage());
        }
    }

    private Configuracao() {
    }

    public static double getDouble(String chave, double padrao) {
        String valor = props.getProperty(chave);
        if (valor == null) {
            return padrao;
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    public static int getInt(String chave, int padrao) {
        String valor = props.getProperty(chave);
        if (valor == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
