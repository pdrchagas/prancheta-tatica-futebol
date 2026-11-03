import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Codificador e parser JSON mínimo, independente de bibliotecas externas.
 * Permite serializar mapas, listas, strings, números, booleanos e null.
 */
public class JsonSimples {
    public static String escrever(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof String) return aspas((String) valor);
        if (valor instanceof Number || valor instanceof Boolean) return String.valueOf(valor);
        if (valor instanceof Map) {
            StringBuilder b = new StringBuilder("{");
            for (Object item : ((Map<?,?>) valor).entrySet()) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) item;
                if (b.length() > 1) b.append(',');
                b.append(aspas(String.valueOf(e.getKey()))).append(':').append(escrever(e.getValue()));
            }
            return b.append('}').toString();
        }
        if (valor instanceof Iterable) {
            StringBuilder b = new StringBuilder("[");
            for (Object v : (Iterable<?>) valor) {
                if (b.length() > 1) b.append(',');
                b.append(escrever(v));
            }
            return b.append(']').toString();
        }
        throw new IllegalArgumentException("Tipo de JSON inválido: " + valor.getClass());
    }

    private static String aspas(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch(c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 32) b.append(String.format("\\u%04x", (int)c));
                    else b.append(c);
            }
        }
        return b.append('"').toString();
    }

    public static Object ler(String texto) {
        Leitor p = new Leitor(texto);
        Object resultado = p.valor();
        p.espacos();
        if (p.i != texto.length()) throw new IllegalArgumentException("JSON com conteúdo adicional.");
        return resultado;
    }

    private static class Leitor {
        final String s;
        int i;
        Leitor(String s) { this.s = s; }
        void espacos() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        char atual() { if (i >= s.length()) throw new IllegalArgumentException("JSON incompleto."); return s.charAt(i); }
        void esperar(char c) { espacos(); if (atual() != c) throw new IllegalArgumentException("JSON inválido na posição " + i); i++; }
        Object valor() {
            espacos(); char c = atual();
            if (c == '"') return string();
            if (c == '{') return objeto();
            if (c == '[') return vetor();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            if (c == '-' || Character.isDigit(c)) return numero();
            throw new IllegalArgumentException("JSON inválido na posição " + i);
        }
        Object numero() {
            int inicio = i;
            while (i < s.length() && "-+0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            double valor = Double.parseDouble(s.substring(inicio, i));
            if (!Double.isFinite(valor)) throw new IllegalArgumentException("Número JSON inválido.");
            return valor;
        }
        String string() {
            esperar('"'); StringBuilder b = new StringBuilder();
            while (true) {
                char c = atual(); i++;
                if (c == '"') return b.toString();
                if (c == '\\') {
                    char k = atual(); i++;
                    switch(k) {
                        case '"': b.append('"'); break;
                        case '\\': b.append('\\'); break;
                        case '/': b.append('/'); break;
                        case 'n': b.append('\n'); break;
                        case 'r': b.append('\r'); break;
                        case 't': b.append('\t'); break;
                        case 'b': b.append('\b'); break;
                        case 'f': b.append('\f'); break;
                        case 'u':
                            if (i + 4 > s.length()) throw new IllegalArgumentException("Unicode JSON incompleto.");
                            b.append((char) Integer.parseInt(s.substring(i,i+4),16)); i += 4; break;
                        default: throw new IllegalArgumentException("Escape JSON desconhecido.");
                    }
                } else b.append(c);
            }
        }
        Map<String,Object> objeto() {
            esperar('{'); Map<String,Object> m = new LinkedHashMap<String,Object>(); espacos();
            if (atual() == '}') { i++; return m; }
            while (true) {
                String chave = string(); esperar(':'); m.put(chave, valor()); espacos();
                char c = atual(); i++;
                if (c == '}') return m;
                if (c != ',') throw new IllegalArgumentException("Separador JSON inválido.");
                espacos();
            }
        }
        List<Object> vetor() {
            esperar('['); List<Object> l = new ArrayList<Object>(); espacos();
            if (atual() == ']') { i++; return l; }
            while (true) {
                l.add(valor()); espacos(); char c = atual(); i++;
                if (c == ']') return l;
                if (c != ',') throw new IllegalArgumentException("Separador JSON inválido.");
            }
        }
    }
}
