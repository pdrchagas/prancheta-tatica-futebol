import java.awt.Graphics2D;
import java.awt.Point;

/**
 * Operações matemáticas de geometria analítica e rasterização pelo
 * algoritmo de Bresenham, aplicadas a trajetórias táticas.
 */
public class Geometria2D {
    public static double distancia(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    /** Distância mínima do ponto P ao segmento AB, usada na seleção por clique. */
    public static double distanciaSegmento(double px, double py,
        double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double den = dx * dx + dy * dy;
        if (den == 0) return distancia(px, py, ax, ay);
        double t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / den));
        return distancia(px, py, ax + t * dx, ay + t * dy);
    }

    /** Bresenham: traça uma linha rasterizada por pontos, sem bibliotecas externas. */
    public static void bresenham(Graphics2D g, int x0, int y0, int x1, int y1, int tamanho) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int erro = dx + dy;
        int passo = 0;
        while (true) {
            if ((passo / 7) % 2 == 0)
                g.fillOval(x0 - tamanho / 2, y0 - tamanho / 2, tamanho, tamanho);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * erro;
            if (e2 >= dy) { erro += dy; x0 += sx; }
            if (e2 <= dx) { erro += dx; y0 += sy; }
            passo++;
        }
    }
}
