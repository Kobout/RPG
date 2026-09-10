package view;

import modelo.ConfiguracoesJogo;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class Som {

    private Som() {
    }

    // Toca um efeito sonoro (arquivo .wav em resources/sons/). Não trava a interface:
    // qualquer erro (arquivo faltando, áudio não suportado no sistema) só é ignorado.
    public static void tocar(String arquivo) {
        if (isSilenciado()) {
            return;
        }
        try {
            InputStream bruto = Som.class.getResourceAsStream("/sons/" + arquivo);
            if (bruto == null) {
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(bruto));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.addLineListener(evento -> {
                if (evento.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            System.out.println("Não foi possível tocar o som \"" + arquivo + "\": " + e.getMessage());
        }
    }

    public static void setSilenciado(boolean valor) {
        ConfiguracoesJogo.setSomSilenciado(valor);
    }

    public static boolean isSilenciado() {
        return ConfiguracoesJogo.isSomSilenciado();
    }
}