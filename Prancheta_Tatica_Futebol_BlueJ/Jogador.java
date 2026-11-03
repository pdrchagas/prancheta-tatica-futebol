/**
 * Jogador: atleta titular ou reserva da prancheta tática.
 * As posições dos titulares são medidas em metros de campo (105 x 68).
 * @author João Lucas de Lima Souza, Pedro Chagas Neves de Farias Nascimento,
 *         Guilherme Pereira de Rivoredo e Eduardo Achkar Avancini
 */
public class Jogador {
    public int id;
    public String equipe;
    public String nome;
    public int numero;
    public double x;
    public double y;
    public boolean titular;

    public Jogador(int id, String equipe, String nome, int numero,
                   double x, double y, boolean titular) {
        this.id = id;
        this.equipe = equipe;
        this.nome = nome;
        this.numero = numero;
        this.x = x;
        this.y = y;
        this.titular = titular;
    }

    public String toString() { return numero + " - " + nome; }
}
