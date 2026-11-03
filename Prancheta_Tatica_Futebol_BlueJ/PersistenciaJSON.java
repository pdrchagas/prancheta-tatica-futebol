import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Salva e recupera a prancheta tática por serialização JSON UTF-8. */
public class PersistenciaJSON {
    private static Map<String,Object> mapa(Object... itens) {
        Map<String,Object> m = new LinkedHashMap<String,Object>();
        for (int i = 0; i < itens.length; i += 2) m.put((String)itens[i], itens[i+1]);
        return m;
    }

    public static String serializar(EstadoTatico e) {
        List<Object> jogadores = new ArrayList<Object>();
        for (Jogador j : e.jogadores) jogadores.add(mapa(
            "id",j.id, "equipe",j.equipe, "nome",j.nome, "numero",j.numero,
            "x",j.x,"y",j.y,"titular",j.titular));
        List<Object> jogadas = new ArrayList<Object>();
        for (Jogada j : e.jogadas) jogadas.add(mapa(
            "tipo",j.tipo,"x1",j.x1,"y1",j.y1,"x2",j.x2,"y2",j.y2,
            "x3",j.x3,"y3",j.y3,"jogadorId",j.jogadorId));
        return JsonSimples.escrever(mapa("versao",1,"equipeAzul",e.equipeAzul,
            "equipeVermelha",e.equipeVermelha,"formacaoAzul",e.formacaoAzul,
            "formacaoVermelho",e.formacaoVermelho,"bolaX",e.bolaX,"bolaY",e.bolaY,
            "proximoId",e.proximoId,"situacao",e.situacao,"jogadores",jogadores,"jogadas",jogadas));
    }

    private static String texto(Map<String,Object> m, String chave) {
        Object v = m.get(chave);
        if (!(v instanceof String)) throw new IllegalArgumentException("Campo inválido: " + chave);
        return (String) v;
    }
    private static double num(Map<String,Object> m, String chave) {
        Object v = m.get(chave);
        if (!(v instanceof Number)) throw new IllegalArgumentException("Campo inválido: " + chave);
        return ((Number) v).doubleValue();
    }
    private static int inteiro(Map<String,Object> m, String chave) {
        return (int) Math.round(num(m,chave));
    }

    @SuppressWarnings("unchecked")
    public static EstadoTatico desserializar(String json) {
        Object raiz = JsonSimples.ler(json);
        if (!(raiz instanceof Map)) throw new IllegalArgumentException("JSON sem objeto raiz.");
        Map<String,Object> m = (Map<String,Object>) raiz;
        EstadoTatico e = new EstadoTatico();
        e.equipeAzul = texto(m,"equipeAzul");
        e.equipeVermelha = texto(m,"equipeVermelha");
        e.formacaoAzul = texto(m,"formacaoAzul");
        e.formacaoVermelho = texto(m,"formacaoVermelho");
        e.bolaX = num(m,"bolaX"); e.bolaY = num(m,"bolaY");
        e.proximoId = inteiro(m,"proximoId");
        // Compatibilidade com arquivos JSON gerados antes dos cenarios de bola parada.
        Object situacao = m.get("situacao");
        e.situacao = situacao instanceof String ? (String) situacao : "JOGO LIVRE";
        if (!(m.get("jogadores") instanceof List) || !(m.get("jogadas") instanceof List))
            throw new IllegalArgumentException("As listas de dados não existem.");
        for (Object o : (List<?>)m.get("jogadores")) {
            Map<String,Object> p = (Map<String,Object>)o;
            Object tit = p.get("titular");
            if (!(tit instanceof Boolean)) throw new IllegalArgumentException("Titular inválido.");
            double x = num(p,"x"), y = num(p,"y");
            if (!Double.isFinite(x) || !Double.isFinite(y) || x<0 || x>105 || y<0 || y>68)
                throw new IllegalArgumentException("Posição de jogador inválida.");
            e.jogadores.add(new Jogador(inteiro(p,"id"),texto(p,"equipe"),
                texto(p,"nome"),inteiro(p,"numero"),x,y,(Boolean)tit));
        }
        for (Object o : (List<?>)m.get("jogadas")) {
            Map<String,Object> p = (Map<String,Object>)o;
            e.jogadas.add(new Jogada(texto(p,"tipo"),num(p,"x1"),num(p,"y1"),
                num(p,"x2"),num(p,"y2"),num(p,"x3"),num(p,"y3"),inteiro(p,"jogadorId")));
        }
        if (e.jogadores.size()>300 || e.jogadas.size()>5000)
            throw new IllegalArgumentException("Arquivo excede os limites do aplicativo.");
        return e;
    }

    public static void salvar(File destino, EstadoTatico e) throws IOException {
        Files.write(destino.toPath(), serializar(e).getBytes(StandardCharsets.UTF_8));
    }
    public static EstadoTatico abrir(File arquivo) throws IOException {
        return desserializar(new String(Files.readAllBytes(arquivo.toPath()),StandardCharsets.UTF_8));
    }
}
