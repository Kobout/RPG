import modelo.Arqueiro;
import modelo.Guerreiro;
import modelo.Mago;

// Testes simples, sem JUnit (o projeto não usa Maven/Gradle, então evitei
// depender de bibliotecas externas). Roda com:
//   javac -d out-test -cp out test/TesteBalanceamento.java
//   java -cp "out;out-test" TesteBalanceamento
public class TesteBalanceamento {

    private static int total = 0;
    private static int falhas = 0;

    public static void main(String[] args) {
        testeSubirNivelAumentaAtributosEm10Porcento();
        testeAplicarDanoNuncaEhMenorQueUm();
        testePocaoDeVidaRestaura30PorCento();
        testeManaRestauraTotalAoMorrer();
        testeArqueiroPossuiChanceDeCritico();

        System.out.println();
        System.out.println(total + " teste(s) rodado(s), " + falhas + " falha(s).");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void testeSubirNivelAumentaAtributosEm10Porcento() {
        total++;
        Guerreiro g = new Guerreiro("Teste", "Espada", 100, 100, 100);
        g.subirNivel();
        verificar("subirNivel aumenta ataque em 10%", g.getAtaque() == 110);
        verificar("subirNivel aumenta vida máxima em 10%", g.getVidaMaxima() == 110);
        verificar("subirNivel aumenta defesa em 10%", g.getDefesa() == 110);
    }

    private static void testeAplicarDanoNuncaEhMenorQueUm() {
        total++;
        Guerreiro atacante = new Guerreiro("Fraco", "Faca", 1, 50, 1);
        Guerreiro alvoForte = new Guerreiro("Tanque", "Escudo", 1, 500, 500);
        int vidaAntes = alvoForte.getVida();
        atacante.atacar(alvoForte);
        int danoCausado = vidaAntes - alvoForte.getVida();
        verificar("dano mínimo de 1 mesmo com defesa muito alta", danoCausado >= 1);
    }

    private static void testePocaoDeVidaRestaura30PorCento() {
        total++;
        Guerreiro g = new Guerreiro("Teste2", "Espada", 50, 100, 20);
        Guerreiro inimigoFraco = new Guerreiro("InimigoTeste", "Nada", 1, 1, 0);
        while (g.getVida() > 50) {
            inimigoFraco.atacar(g);
        }
        int vidaAntes = g.getVida();
        boolean usou = g.usarPocaoVida();
        int esperado = Math.min(g.getVidaMaxima(), vidaAntes + (int) Math.round(g.getVidaMaxima() * 0.3));
        verificar("usarPocaoVida retorna true quando há poções", usou);
        verificar("poção de vida restaura 30% da vida máxima", g.getVida() == esperado);
    }

    private static void testeManaRestauraTotalAoMorrer() {
        total++;
        Mago m = new Mago("MagoTeste", 100, 50, 100, 10);
        Guerreiro alvo = new Guerreiro("Alvo", "Nada", 1, 100000, 1);
        while (m.getMana() >= 15) {
            m.atacarBolaDeFogo(alvo);
        }
        m.restaurarManaTotal();
        verificar("restaurarManaTotal enche a mana", m.getMana() == m.getManaMaxima());
    }

    private static void testeArqueiroPossuiChanceDeCritico() {
        total++;
        Arqueiro a = new Arqueiro("Kael", 15, 50, 100, 10);
        Guerreiro alvo = new Guerreiro("Alvo2", "Nada", 1, 100000, 5);
        boolean encontrouCritico = false;
        for (int i = 0; i < 200 && !encontrouCritico; i++) {
            String resultado = a.atacar(alvo);
            if (resultado.contains("CRÍTICO")) {
                encontrouCritico = true;
            }
        }
        verificar("arqueiro eventualmente acerta um crítico em 200 tentativas", encontrouCritico);
    }

    private static void verificar(String descricao, boolean condicao) {
        if (condicao) {
            System.out.println("[OK] " + descricao);
        } else {
            System.out.println("[FALHOU] " + descricao);
            falhas++;
        }
    }
}