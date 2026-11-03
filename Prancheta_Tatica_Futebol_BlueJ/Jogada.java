/**
 * Elemento desenhado pelo técnico: passe, chute, deslocamento,
 * linha de marcação ou triângulo representando uma zona de pressão.
 */
public class Jogada {
    public String tipo;
    public double x1, y1, x2, y2, x3, y3;
    public int jogadorId;

    public Jogada(String tipo, double x1, double y1, double x2, double y2,
                  double x3, double y3, int jogadorId) {
        this.tipo = tipo;
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.x3 = x3; this.y3 = y3;
        this.jogadorId = jogadorId;
    }
}
