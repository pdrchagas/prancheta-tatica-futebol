import java.util.ArrayList;
import java.util.List;

/**
 * Cenarios prontos de bolas paradas em um campo de 105 x 68 metros.
 * Coordenadas sao espelhadas conforme a equipe atacante.
 * Preserva nome, numero, reservas e identificacao dos atletas.
 * @author João Lucas de Lima Souza, Pedro Chagas Neves de Farias Nascimento,
 *         Guilherme Pereira de Rivoredo e Eduardo Achkar Avancini
 */
public final class BolasParadas {
    public static final String PENALTI = "PÊNALTI";
    public static final String FALTA = "FALTA FRONTAL";
    public static final String ESCANTEIO = "ESCANTEIO";

    private BolasParadas() { }

    /**
     * Aplica um cenario de ataque para AZUL (direita) ou VERMELHO (esquerda).
     * Os primeiros atletas de cada escalação representam os goleiros;
     * o segundo da equipe atacante representa o cobrador.
     */
    public static void aplicar(EstadoTatico estado, String tipo, String atacante) {
        if (!"AZUL".equals(atacante) && !"VERMELHO".equals(atacante))
            throw new IllegalArgumentException("Equipe atacante não reconhecida.");
        if (!PENALTI.equals(tipo) && !FALTA.equals(tipo) && !ESCANTEIO.equals(tipo))
            throw new IllegalArgumentException("Tipo de bola parada não reconhecido.");

        String defensor = "AZUL".equals(atacante) ? "VERMELHO" : "AZUL";
        List<Jogador> ofensivos = titulares(estado, atacante);
        List<Jogador> defensivos = titulares(estado, defensor);
        if (ofensivos.size() != 11 || defensivos.size() != 11)
            throw new IllegalStateException("É necessário ter 11 titulares em cada equipe para aplicar o cenário.");

        double[][] ataque, defesa;
        double bolaX, bolaY;
        if (PENALTI.equals(tipo)) {
            // Cobrador na marca do penalti (94 m); goleiro adversario sobre a linha do gol.
            // Todos os demais atletas de linha ficam fora da grande area (x < 88,5 m)
            // e a mais de 9,15 metros da marca.
            bolaX = 94; bolaY = 34;
            ataque = new double[][] {
                {7.5,34}, {90.8,34}, {78,10}, {75,18}, {72,26}, {71,34},
                {72,42}, {75,50}, {78,58}, {64,23}, {64,45}
            };
            defesa = new double[][] {
                {103.1,34}, {84,12}, {84,21}, {84,30}, {84,40}, {84,49},
                {84,58}, {68,10}, {68,20}, {68,49}, {68,59}
            };
        } else if (FALTA.equals(tipo)) {
            // Falta a 23 m da linha do gol. Barreira proxima a 9,15 m da bola.
            bolaX = 82; bolaY = 34;
            ataque = new double[][] {
                {7.5,34}, {79.4,34}, {69,18}, {69,50}, {78,15}, {78,53},
                {97,19}, {96,49}, {87,21}, {87,50}, {72,39}
            };
            defesa = new double[][] {
                {103.1,34}, {91.5,27}, {91.5,30.4}, {91.5,33.8},
                {91.5,37.2}, {91.5,40.6}, {99,15}, {99,53}, {96,23}, {96,45}, {87,57}
            };
        } else {
            // Escanteio na bandeirinha superior direita. O cobrador esta proximo da bola.
            bolaX = 104.3; bolaY = 0.9;
            ataque = new double[][] {
                {7.5,34}, {101.5,4}, {94,17}, {96,25}, {97,34}, {96,43},
                {94,51}, {86,15}, {86,52}, {89,33}, {78,38}
            };
            defesa = new double[][] {
                {103.1,34}, {100,17}, {101,24}, {101,43}, {100,51}, {92,22},
                {92,29}, {92,40}, {92,47}, {82,25}, {82,48}
            };
        }

        // Espelho horizontal: o VERMELHO ataca o gol da esquerda.
        if ("VERMELHO".equals(atacante)) {
            bolaX = 105 - bolaX;
            if (ESCANTEIO.equals(tipo)) bolaY = 68 - bolaY;
        }
        atribuir(ofensivos, ataque, "VERMELHO".equals(atacante),
                 "VERMELHO".equals(atacante) && ESCANTEIO.equals(tipo));
        atribuir(defensivos, defesa, "VERMELHO".equals(atacante),
                 "VERMELHO".equals(atacante) && ESCANTEIO.equals(tipo));
        estado.bolaX = bolaX;
        estado.bolaY = bolaY;
        estado.situacao = tipo + " • " + atacante + " ATACA";
    }

    private static List<Jogador> titulares(EstadoTatico e, String equipe) {
        List<Jogador> resultado = new ArrayList<Jogador>();
        for (Jogador j : e.jogadores) if (j.titular && equipe.equals(j.equipe)) resultado.add(j);
        return resultado;
    }

    private static void atribuir(List<Jogador> atletas, double[][] posicoes,
                                 boolean espelharX, boolean espelharY) {
        for (int i=0; i<atletas.size(); i++) {
            Jogador j = atletas.get(i);
            j.x = espelharX ? 105 - posicoes[i][0] : posicoes[i][0];
            j.y = espelharY ? 68 - posicoes[i][1] : posicoes[i][1];
        }
    }
}
