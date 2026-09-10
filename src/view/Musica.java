package view;

import modelo.ConfiguracoesJogo;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class Musica {

    private static Clip clipAtual;
    private static String faixaAtual;

    private Musica() {
    }

    // Toca uma faixa em loop contínuo. Se a mesma faixa já estiver tocando, não reinicia.
    public static void tocar(String arquivo) {
        if (arquivo.equals(faixaAtual) && clipAtual != null && clipAtual.isRunning()) {
            return;
        }

        parar();
        faixaAtual = arquivo;

        if (ConfiguracoesJogo.isMusicaSilenciada()) {
            return;
        }

        try {
            InputStream bruto = Musica.class.getResourceAsStream("/musicas/" + arquivo);
            if (bruto == null) {
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(bruto));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            ajustarVolume(clip, -12.0f);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            clipAtual = clip;
        } catch (Exception e) {
            System.out.println("Não foi possível tocar a música \"" + arquivo + "\": " + e.getMessage());
        }
    }

    public static void parar() {
        if (clipAtual != null) {
            clipAtual.stop();
            clipAtual.close();
            clipAtual = null;
        }
    }

    // Chamado depois de alternar a configuração de música silenciada: para na hora se
    // acabou de ser desligada, ou retoma a última faixa se foi ligada de novo.
    public static void reaplicarConfiguracao() {
        if (ConfiguracoesJogo.isMusicaSilenciada()) {
            parar();
        } else if (faixaAtual != null) {
            String faixa = faixaAtual;
            faixaAtual = null;
            tocar(faixa);
        }
    }

    private static void ajustarVolume(Clip clip, float decibeis) {
        try {
            FloatControl controle = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            controle.setValue(decibeis);
        } catch (Exception ignorada) {
            // controle de volume não suportado nesse sistema - toca no volume padrão
        }
    }
}