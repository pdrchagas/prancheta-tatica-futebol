import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * PONTO DE ENTRADA - Projeto Extensionista: Prancheta Tática Interativa 2D.
 * Alunos: João Lucas de Lima Souza, Pedro Chagas Neves de Farias Nascimento,
 * Guilherme Pereira de Rivoredo e Eduardo Achkar Avancini.
 */
public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch(Exception ignored) { /* mantém visual Swing padrão se necessário */ }
        SwingUtilities.invokeLater(()->new TelaPrincipal().setVisible(true));
    }
}
