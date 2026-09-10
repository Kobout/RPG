package modelo;

public class ConfiguracoesJogo {

    private static boolean somSilenciado = false;
    private static boolean musicaSilenciada = false;

    private ConfiguracoesJogo() {
    }

    public static boolean isSomSilenciado() {
        return somSilenciado;
    }

    public static void setSomSilenciado(boolean valor) {
        somSilenciado = valor;
    }

    public static boolean isMusicaSilenciada() {
        return musicaSilenciada;
    }

    public static void setMusicaSilenciada(boolean valor) {
        musicaSilenciada = valor;
    }
}