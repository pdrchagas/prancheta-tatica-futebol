import java.util.ArrayList;
import java.util.List;

/**
 * Estado persistente da prancheta, independente das dimensões da janela.
 * Campo lógico 105 x 68 metros; o viewport converte essas posições em pixels.
 */
public class EstadoTatico {
    public List<Jogador> jogadores = new ArrayList<Jogador>();
    public List<Jogada> jogadas = new ArrayList<Jogada>();
    public String equipeAzul = "Equipe Azul";
    public String equipeVermelha = "Equipe Vermelha";
    public String formacaoAzul = "4-3-3";
    public String formacaoVermelho = "4-4-2";
    public String situacao = "JOGO LIVRE";
    public double bolaX = 52.5;
    public double bolaY = 34;
    public int proximoId = 1;

    public static EstadoTatico novo() {
        EstadoTatico e = new EstadoTatico();
        adicionarOnze(e, "AZUL", "4-3-3");
        adicionarOnze(e, "VERMELHO", "4-4-2");
        return e;
    }

    private static void adicionarOnze(EstadoTatico e, String equipe, String formacao) {
        double[][] pos = Formacoes.posicoes(formacao, equipe.equals("AZUL"));
        for (int i = 0; i < 11; i++) {
            int numero = i == 0 ? 1 : i + 1;
            e.jogadores.add(new Jogador(e.proximoId++, equipe,
                i == 0 ? "Goleiro" : "Jogador " + numero, numero,
                pos[i][0], pos[i][1], true));
        }
    }

    public int totalTitulares(String equipe) {
        int n = 0;
        for (Jogador j : jogadores)
            if (j.titular && j.equipe.equals(equipe)) n++;
        return n;
    }

    public Jogador buscar(int id) {
        for (Jogador j : jogadores) if (j.id == id) return j;
        return null;
    }

    public List<Jogador> reservas(String equipe) {
        List<Jogador> l = new ArrayList<Jogador>();
        for (Jogador j : jogadores)
            if (!j.titular && j.equipe.equals(equipe)) l.add(j);
        return l;
    }

    public Jogador adicionarReserva(String equipe, String nome, int numero) {
        Jogador j = new Jogador(proximoId++, equipe, nome, numero, 52.5, 34, false);
        jogadores.add(j);
        return j;
    }

    public Jogador adicionarTitular(String equipe, String nome, int numero) {
        if (totalTitulares(equipe) >= 11)
            throw new IllegalStateException("O time já possui 11 titulares. Faça uma substituição.");
        double[][] pos = Formacoes.posicoes(equipe.equals("AZUL") ? formacaoAzul : formacaoVermelho,
                                          equipe.equals("AZUL"));
        int n = totalTitulares(equipe);
        Jogador j = new Jogador(proximoId++, equipe, nome, numero, pos[n][0], pos[n][1], true);
        jogadores.add(j);
        return j;
    }
}
