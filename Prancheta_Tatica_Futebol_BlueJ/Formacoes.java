import java.util.ArrayList;
import java.util.List;

/**
 * Gera a posição inicial de um goleiro e dez jogadores de linha.
 * Implementa as formações como linhas entre defesa e ataque.
 */
public class Formacoes {
    public static final String[] OPCOES = {
        "4-3-3", "4-4-2", "4-2-3-1", "3-5-2", "3-4-3", "4-1-4-1",
        "5-3-2", "4-5-1", "4-3-2-1", "3-4-2-1", "4-2-2-2", "4-1-2-1-2"
    };

    /** Calcula posições espelhadas para as duas equipes. */
    public static double[][] posicoes(String formacao, boolean esquerda) {
        String[] s = formacao.split("-");
        int total = 0;
        for (String parte : s) total += Integer.parseInt(parte);
        if (total != 10) throw new IllegalArgumentException("A formação deve somar 10 jogadores de linha.");

        double[][] p = new double[11][2];
        p[0][0] = esquerda ? 7.5 : 97.5;
        p[0][1] = 34;
        int idx = 1;
        for (int linha = 0; linha < s.length; linha++) {
            int quantidade = Integer.parseInt(s[linha]);
            double x = 21 + linha * (60.0 / Math.max(1, s.length - 1));
            if (!esquerda) x = 105 - x;
            for (int k = 0; k < quantidade; k++) {
                p[idx][0] = x;
                p[idx][1] = 68.0 * (k + 1) / (quantidade + 1);
                idx++;
            }
        }
        return p;
    }

    /** Preserva nomes e números ao trocar a formação. */
    public static void aplicar(EstadoTatico estado, String equipe, String formacao) {
        List<Jogador> titulares = new ArrayList<Jogador>();
        for (Jogador j : estado.jogadores)
            if (j.titular && j.equipe.equals(equipe)) titulares.add(j);
        double[][] pos = posicoes(formacao, equipe.equals("AZUL"));
        for (int i = 0; i < titulares.size() && i < 11; i++) {
            titulares.get(i).x = pos[i][0];
            titulares.get(i).y = pos[i][1];
        }
        if (equipe.equals("AZUL")) estado.formacaoAzul = formacao;
        else estado.formacaoVermelho = formacao;
    }
}
