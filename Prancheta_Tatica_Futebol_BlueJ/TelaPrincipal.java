import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Janela principal Swing do simulador de prancheta tática para futebol.
 * Exibe campo, ferramenta de desenho, escalação e banco de reservas.
 */
public class TelaPrincipal extends JFrame {
    private EstadoTatico estado = EstadoTatico.novo();
    private final CampoFutebol campo = new CampoFutebol(this);
    private final JLabel rodape = new JLabel("Pronto para criar uma jogada.");
    private final JLabel indicador = new JLabel("22 titulares  •  0 jogadas");
    private final JTextField nomeAzul = new JTextField("Equipe Azul");
    private final JTextField nomeVermelho = new JTextField("Equipe Vermelha");
    private final JComboBox<String> formAzul = new JComboBox<String>(Formacoes.OPCOES);
    private final JComboBox<String> formVermelho = new JComboBox<String>(Formacoes.OPCOES);
    private final JTextField campoNome = new JTextField();
    private final JSpinner campoNumero = new JSpinner(new SpinnerNumberModel(1,1,99,1));
    private final JLabel atletaSelecionado = new JLabel("Nenhum jogador selecionado");
    private final JComboBox<String> equipeNova = new JComboBox<String>(new String[]{"AZUL","VERMELHO"});
    private final JComboBox<String> equipeBanco = new JComboBox<String>(new String[]{"AZUL","VERMELHO"});
    private final JTextField novoNome = new JTextField("Reserva");
    private final JSpinner novoNumero = new JSpinner(new SpinnerNumberModel(12,1,99,1));
    private final DefaultListModel<Jogador> modeloReservas = new DefaultListModel<Jogador>();
    private final JList<Jogador> listaReservas = new JList<Jogador>(modeloReservas);
    private final java.util.Deque<String> historico = new java.util.ArrayDeque<String>();
    private final ButtonGroup grupoFerramentas = new ButtonGroup();
    private File arquivoAtual;
    private boolean sujo;
    private Timer timer;
    private int etapaAnimacao, quadroAnimacao;
    private List<Jogada> sequenciaAnimacao;

    private static final Color FUNDO = new Color(12,24,38);
    private static final Color CARTAO = new Color(24,41,58);
    private static final Color LETRA = new Color(230,240,248);
    private static final Color AZUL = new Color(65,143,245);
    private static final Color VERMELHO = new Color(249,99,114);
    // UI clara nos botoes: legibilidade consistente inclusive com Look & Feel do Windows.
    private static final Color BOTAO_CLARO = new Color(221,237,252);
    private static final Color BOTAO_HOVER = new Color(183,222,250);
    private static final Color BOTAO_TEXTO = new Color(13,39,63);
    private static final Color BOTAO_SELECIONADO = new Color(36,113,196);
    private static final Color BOTAO_SELECIONADO_HOVER = new Color(25,92,164);

    public TelaPrincipal() {
        super("PRANCHETA PRO — Simulador Tático de Futebol");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1130,730));
        setSize(1520,920);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FUNDO);
        add(criarCabecalho(),BorderLayout.NORTH);
        add(criarAreaCentral(),BorderLayout.CENTER);
        add(criarRodape(),BorderLayout.SOUTH);
        formAzul.setSelectedItem("4-3-3");
        formVermelho.setSelectedItem("4-4-2");
        atualizarReservas(); atualizarContadores();
        atalhos();
    }

    public EstadoTatico estado() {return estado;}
    public void status(String s) {rodape.setText("  " + s);}
    public void registrarAntes() { registrarAntes(PersistenciaJSON.serializar(estado)); }
    public void registrarAntes(String antes) {
        if(historico.size()>=60)historico.removeFirst();
        historico.addLast(antes);
    }
    public void alterado() {sujo=true;atualizarContadores();campo.repaint();atualizarReservas();}

    private JPanel criarCabecalho() {
        JPanel p=new JPanel(new BorderLayout(16,0));
        p.setBackground(FUNDO);p.setBorder(BorderFactory.createEmptyBorder(14,18,12,18));
        JPanel marca=new JPanel(new GridLayout(2,1));marca.setOpaque(false);
        JLabel tit=new JLabel("◉  PRANCHETA PRO");tit.setFont(new Font("SansSerif",Font.BOLD,23));tit.setForeground(Color.WHITE);
        JLabel sub=new JLabel("ESTRATÉGIA  •  FORMAÇÕES  •  JOGADAS ENSAIADAS");
        sub.setFont(new Font("SansSerif",Font.BOLD,10));sub.setForeground(new Color(139,170,197));
        marca.add(tit);marca.add(sub);
        p.add(marca,BorderLayout.WEST);
        JPanel acoes=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,0));acoes.setOpaque(false);
        acoes.add(botaoAcao("Novo",()->novoEsquema()));
        acoes.add(botaoAcao("Abrir JSON",()->abrir()));
        acoes.add(botaoAcao("Salvar JSON",()->salvar()));
        acoes.add(botaoAcao("Exportar PNG",()->exportarImagem()));
        acoes.add(botaoAcao("Desfazer",()->desfazer()));
        acoes.add(botaoAcao("Reproduzir",()->reproduzir()));
        JButton parada=botaoAcao("Bola parada ▾",()->{});
        JPopupMenu menu=new JPopupMenu();
        menu.add(itemMenu("Pênalti — Azul cobra",()->bolaParada(BolasParadas.PENALTI,"AZUL")));
        menu.add(itemMenu("Pênalti — Vermelho cobra",()->bolaParada(BolasParadas.PENALTI,"VERMELHO")));
        menu.addSeparator();
        menu.add(itemMenu("Falta frontal — Azul cobra",()->bolaParada(BolasParadas.FALTA,"AZUL")));
        menu.add(itemMenu("Falta frontal — Vermelho cobra",()->bolaParada(BolasParadas.FALTA,"VERMELHO")));
        menu.addSeparator();
        menu.add(itemMenu("Escanteio direito — Azul cobra",()->bolaParada(BolasParadas.ESCANTEIO,"AZUL")));
        menu.add(itemMenu("Escanteio esquerdo — Vermelho cobra",()->bolaParada(BolasParadas.ESCANTEIO,"VERMELHO")));
        parada.addActionListener(e->menu.show(parada,0,parada.getHeight()));
        acoes.add(parada);
        acoes.add(botaoAcao("Ajuda",()->ajuda()));
        p.add(acoes,BorderLayout.EAST);
        return p;
    }

    private JMenuItem itemMenu(String nome, Runnable acao) {
        JMenuItem item=new JMenuItem(nome);
        item.addActionListener(e->acao.run()); return item;
    }
    private void bolaParada(String tipo, String atacante) {
        try {
            interromper();
            // Valida os titulares antes de salvar o ponto de desfazer.
            if (estado.totalTitulares("AZUL") != 11 || estado.totalTitulares("VERMELHO") != 11) {
                status("Para preparar bolas paradas, as duas equipes precisam ter 11 titulares.");
                return;
            }
            registrarAntes();
            BolasParadas.aplicar(estado,tipo,atacante);
            campo.limparSelecao();
            campo.setFerramenta(CampoFutebol.Ferramenta.SELECIONAR);
            selecionarFerramentaVisual();
            alterado();
            status(tipo+": "+atacante+" cobra. Atletas, goleiros e bola reposicionados; você pode arrastá-los para ajustar.");
        } catch (Exception e) { erro(e); }
    }

    /** Usa pintura Basic para evitar texto invisivel em temas personalizados. */
    private void configurarCores(final AbstractButton b, final boolean alternancia) {
        if (alternancia) b.setUI(new BasicToggleButtonUI());
        else b.setUI(new BasicButtonUI());
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setForeground(BOTAO_TEXTO);
        b.setBackground(BOTAO_CLARO);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { atualizarCor(b, true); }
            public void mouseExited(MouseEvent e) { atualizarCor(b, false); }
        });
        if (alternancia) b.addItemListener(e -> atualizarCor(b, b.getModel().isRollover()));
        atualizarCor(b, false);
    }

    private void atualizarCor(AbstractButton b, boolean sobre) {
        boolean ativo = b instanceof JToggleButton && b.isSelected();
        b.setBackground(ativo ? (sobre ? BOTAO_SELECIONADO_HOVER : BOTAO_SELECIONADO) :
                        (sobre ? BOTAO_HOVER : BOTAO_CLARO));
        b.setForeground(ativo ? Color.WHITE : BOTAO_TEXTO);
        b.repaint();
    }

    private void selecionarFerramentaVisual() {
        java.util.Enumeration<AbstractButton> ops = grupoFerramentas.getElements();
        while (ops.hasMoreElements()) {
            AbstractButton b = ops.nextElement();
            if ("Selecionar / mover".equals(b.getText())) b.setSelected(true);
            atualizarCor(b,false);
        }
    }

    private JButton botaoAcao(String texto,Runnable r) {
        JButton b=new JButton(texto);
        b.setFont(new Font("SansSerif",Font.BOLD,12));
        configurarCores(b,false);
        b.setBorder(BorderFactory.createEmptyBorder(10,11,10,11));
        b.addActionListener(e->r.run());return b;
    }

    private JPanel criarAreaCentral() {
        JPanel area=new JPanel(new BorderLayout(10,0));area.setBackground(FUNDO);
        area.setBorder(BorderFactory.createEmptyBorder(0,14,8,14));
        area.add(criarFerramentas(),BorderLayout.WEST);
        JPanel canvas=new JPanel(new BorderLayout()); canvas.setBackground(FUNDO);
        canvas.setBorder(BorderFactory.createLineBorder(new Color(58,83,96),1));
        canvas.add(campo,BorderLayout.CENTER);
        area.add(canvas,BorderLayout.CENTER);
        area.add(criarPainelDireito(),BorderLayout.EAST);
        return area;
    }

    private JPanel criarFerramentas() {
        JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(CARTAO);p.setPreferredSize(new Dimension(178,0));
        p.setBorder(BorderFactory.createEmptyBorder(15,12,12,12));
        textoSecao(p,"FERRAMENTAS");
        ferramenta(p,"Selecionar / mover",CampoFutebol.Ferramenta.SELECIONAR,true);
        ferramenta(p,"Passe →",CampoFutebol.Ferramenta.PASSE,false);
        ferramenta(p,"Chute ↗",CampoFutebol.Ferramenta.CHUTE,false);
        ferramenta(p,"Deslocamento",CampoFutebol.Ferramenta.MOVIMENTO,false);
        ferramenta(p,"Marcação",CampoFutebol.Ferramenta.MARCACAO,false);
        ferramenta(p,"Zona de pressão",CampoFutebol.Ferramenta.ZONA,false);
        p.add(Box.createVerticalStrut(22));
        textoSecao(p,"DESENHO TÁTICO");
        JLabel ajuda=rotulo("Passe/chute: arraste.\nMovimentação: arraste.\nZona: marque 3 vértices.\nJogadores: clique e arraste.");
        ajuda.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(ajuda);
        p.add(Box.createVerticalStrut(18));
        JButton limpar=botaoAcao("Limpar jogadas",()->limparJogadas());
        limpar.setAlignmentX(Component.LEFT_ALIGNMENT);limpar.setMaximumSize(new Dimension(155,35));p.add(limpar);
        p.add(Box.createVerticalGlue());
        JLabel indic=rotulo("Arraste a bola e os\njogadores no campo.");indic.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(indic);
        return p;
    }

    private void ferramenta(JPanel p,String titulo,CampoFutebol.Ferramenta f,boolean selecionado) {
        JToggleButton b=new JToggleButton(titulo,selecionado);
        b.setFont(new Font("SansSerif",Font.BOLD,12));
        configurarCores(b,true);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(160,38));b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBorder(BorderFactory.createEmptyBorder(9,10,9,8));
        grupoFerramentas.add(b);p.add(b);p.add(Box.createVerticalStrut(6));
        b.addActionListener(e->{ campo.setFerramenta(f);status("Ferramenta: "+titulo);
            // Atualiza a selecao; os demais botoes mantem contraste fixo.
            java.util.Enumeration<AbstractButton> ops=grupoFerramentas.getElements();
            while(ops.hasMoreElements()) {
                AbstractButton x=ops.nextElement(); atualizarCor(x,x.getModel().isRollover());
            }
        });
    }

    private JScrollPane criarPainelDireito() {
        JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(CARTAO);p.setBorder(BorderFactory.createEmptyBorder(13,13,13,13));
        p.setPreferredSize(new Dimension(305,870));
        textoSecao(p,"ESCALAÇÕES");
        campoTitulo(p,"Time azul");p.add(nomeAzul);espaco(p,5);
        p.add(formAzul);
        p.add(botaoPainel("Aplicar formação azul",()->aplicarFormacao("AZUL")));
        espaco(p,10);
        campoTitulo(p,"Time vermelho");p.add(nomeVermelho);espaco(p,5);
        p.add(formVermelho);
        p.add(botaoPainel("Aplicar formação vermelha",()->aplicarFormacao("VERMELHO")));
        espaco(p,16);textoSecao(p,"EDITAR JOGADOR");
        atletaSelecionado.setForeground(new Color(185,219,253));
        atletaSelecionado.setFont(new Font("SansSerif",Font.BOLD,12));
        p.add(atletaSelecionado);
        campoTitulo(p,"Nome do atleta");p.add(campoNome);
        campoTitulo(p,"Número da camisa");p.add(campoNumero);
        p.add(botaoPainel("Salvar nome e número",()->editarJogador()));
        p.add(botaoPainel("Mover atleta para reserva",()->tirarDoCampo()));
        espaco(p,13);textoSecao(p,"NOVO TITULAR");
        p.add(equipeNova);
        p.add(botaoPainel("Adicionar titular",()->adicionarTitular()));
        espaco(p,14);textoSecao(p,"BANCO DE RESERVAS");
        campoTitulo(p,"Equipe do banco");p.add(equipeBanco);
        equipeBanco.addActionListener(e->atualizarReservas());
        campoTitulo(p,"Nome do reserva");p.add(novoNome);
        campoTitulo(p,"Número da camisa");p.add(novoNumero);
        p.add(botaoPainel("+ Adicionar ao banco",()->adicionarReserva()));
        listaReservas.setVisibleRowCount(5);
        listaReservas.setFont(new Font("SansSerif",Font.PLAIN,12));
        listaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll =new JScrollPane(listaReservas);
        scroll.setMaximumSize(new Dimension(285,106));
        scroll.setPreferredSize(new Dimension(282,105));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(scroll);
        p.add(botaoPainel("Substituir jogador escolhido",()->substituir()));
        p.add(botaoPainel("Excluir reserva",()->excluirReserva()));
        espaco(p,15);textoSecao(p,"INFORMAÇÕES");
        indicador.setForeground(new Color(175,212,239));indicador.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(indicador);
        JScrollPane rolagem=new JScrollPane(p);
        rolagem.setPreferredSize(new Dimension(330,700));
        rolagem.setBorder(null);rolagem.getVerticalScrollBar().setUnitIncrement(15);
        rolagem.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return rolagem;
    }
    private void espaco(JPanel p,int h){p.add(Box.createVerticalStrut(h));}
    private void textoSecao(JPanel p,String s) {
        JLabel l=new JLabel(s);l.setForeground(new Color(140,182,206));
        l.setFont(new Font("SansSerif",Font.BOLD,12));l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);p.add(Box.createVerticalStrut(9));
    }
    private void campoTitulo(JPanel p,String s) {
        JLabel l=new JLabel(s);l.setForeground(LETRA);l.setFont(new Font("SansSerif",Font.PLAIN,12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));
    }
    private JLabel rotulo(String texto) {
        return new JLabel("<html>"+texto.replace("\n","<br>")+"</html>") {{
            setForeground(new Color(173,197,209));setFont(new Font("SansSerif",Font.PLAIN,12));
        }};
    }
    private JButton botaoPainel(String texto,Runnable acao) {
        JButton b=new JButton(texto);b.setFont(new Font("SansSerif",Font.BOLD,12));
        configurarCores(b,false);
        b.setBorder(BorderFactory.createEmptyBorder(9,8,9,8));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);b.setMaximumSize(new Dimension(280,34));
        b.addActionListener(e->acao.run());
        return b;
    }
    private JPanel criarRodape() {
        JPanel p=new JPanel(new BorderLayout());p.setBackground(new Color(20,37,51));
        p.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        rodape.setForeground(new Color(181,209,223));rodape.setFont(new Font("SansSerif",Font.PLAIN,12));
        JLabel direita=new JLabel("CGPI  •  Campo 105 × 68 m  •  JSON local");
        direita.setForeground(new Color(137,175,190));
        p.add(rodape,BorderLayout.CENTER);p.add(direita,BorderLayout.EAST);
        return p;
    }

    public void atualizarSelecao(Jogador j) {
        if (j==null) {atletaSelecionado.setText("Nenhum jogador selecionado");campoNome.setText("");return;}
        atletaSelecionado.setText((j.equipe.equals("AZUL")?"AZUL":"VERMELHO")+"  ·  #"+j.numero);
        campoNome.setText(j.nome);campoNumero.setValue(j.numero);
        equipeBanco.setSelectedItem(j.equipe);
    }
    public void atualizarContadores() {
        indicador.setText(estado.totalTitulares("AZUL")+" azuis | "+estado.totalTitulares("VERMELHO")+
            " vermelhos | "+estado.jogadas.size()+" jogadas");
        setTitle((sujo?"● ":"")+"PRANCHETA PRO — Simulador Tático de Futebol");
    }
    private void atualizarReservas() {
        Jogador antigo=listaReservas.getSelectedValue();
        modeloReservas.clear();
        for(Jogador j:estado.reservas((String)equipeBanco.getSelectedItem()))modeloReservas.addElement(j);
        if(antigo!=null)listaReservas.setSelectedValue(antigo,true);
        atualizarContadores();
    }
    private void erro(Exception e) {JOptionPane.showMessageDialog(this,e.getMessage(),"Não foi possível concluir",JOptionPane.WARNING_MESSAGE);}
    private String nomeValido(String s) {
        if(s==null || s.trim().isEmpty())throw new IllegalArgumentException("Informe o nome do atleta.");
        if(s.length()>45)throw new IllegalArgumentException("Use nomes com até 45 caracteres.");
        return s.trim();
    }
    private void atualizarNomesEquipes() {
        estado.equipeAzul=nomeAzul.getText().trim();estado.equipeVermelha=nomeVermelho.getText().trim();
    }
    private void aplicarFormacao(String equipe) {
        registrarAntes(); atualizarNomesEquipes();
        Formacoes.aplicar(estado,equipe,(String)(equipe.equals("AZUL")?formAzul:formVermelho).getSelectedItem());
        estado.situacao = "JOGO LIVRE";
        status("Formação "+(equipe.equals("AZUL")?estado.formacaoAzul:estado.formacaoVermelho)+" aplicada ao time "+equipe+".");alterado();
    }
    private void editarJogador() {
        Jogador j=campo.getSelecionado();
        if(j==null) {status("Clique em um jogador no campo para editar.");return;}
        try {String nome=nomeValido(campoNome.getText());int num=(Integer)campoNumero.getValue();
            registrarAntes();j.nome=nome;j.numero=num;alterado();atualizarSelecao(j);
        }catch(Exception e){erro(e);}
    }
    private void adicionarReserva() {
        try {String equipe=(String)equipeBanco.getSelectedItem();
            String nome=nomeValido(novoNome.getText());int num=(Integer)novoNumero.getValue();
            registrarAntes();estado.adicionarReserva(equipe,nome,num);alterado();
            status("Reserva "+nome+" incluído no banco "+equipe+".");
        }catch(Exception e){erro(e);}
    }
    private void substituir() {
        Jogador titular=campo.getSelecionado();Jogador reserva=listaReservas.getSelectedValue();
        if(titular==null || reserva==null) {status("Selecione um titular no campo e um reserva na lista.");return;}
        if(!titular.equipe.equals(reserva.equipe)) {
            erro(new IllegalArgumentException("Titular e reserva devem pertencer ao mesmo time."));return;
        }
        registrarAntes();
        reserva.x=titular.x;reserva.y=titular.y;reserva.titular=true;
        titular.titular=false;
        campo.selecionar(reserva);alterado();status("Substituição: "+reserva.nome+" entrou por "+titular.nome+".");
    }
    private void tirarDoCampo() {
        Jogador j=campo.getSelecionado();
        if(j==null){status("Selecione um titular antes de movê-lo para o banco.");return;}
        registrarAntes();j.titular=false;campo.limparSelecao();alterado();
        status("Jogador "+j.nome+" movido para o banco.");
    }
    private void adicionarTitular() {
        try {registrarAntes();String equipe=(String)equipeNova.getSelectedItem();
            Jogador j=estado.adicionarTitular(equipe,"Novo titular",99);
            campo.selecionar(j);alterado();status("Novo titular criado; personalize o nome e a camisa.");
        }catch(Exception e){erro(e);}
    }
    private void excluirReserva() {
        Jogador r=listaReservas.getSelectedValue();
        if(r==null){status("Escolha o reserva que deseja excluir.");return;}
        registrarAntes();estado.jogadores.remove(r);alterado();
    }
    private void limparJogadas() {
        if(estado.jogadas.isEmpty())return;
        registrarAntes();estado.jogadas.clear();alterado();status("Jogadas e marcações apagadas. Titulares preservados.");
    }
    private void novoEsquema() {
        int resp=JOptionPane.showConfirmDialog(this,"Iniciar outro esquema? As mudanças não salvas serão perdidas.",
            "Novo esquema",JOptionPane.YES_NO_OPTION);
        if(resp!=JOptionPane.YES_OPTION)return;
        interromper();estado=EstadoTatico.novo();arquivoAtual=null;historico.clear();sujo=false;
        refletirEstado();status("Prancheta reiniciada com 22 atletas e bola.");
    }
    private void refletirEstado() {
        nomeAzul.setText(estado.equipeAzul);nomeVermelho.setText(estado.equipeVermelha);
        formAzul.setSelectedItem(estado.formacaoAzul);formVermelho.setSelectedItem(estado.formacaoVermelho);
        campo.mudarEstado();atualizarSelecao(null);atualizarReservas();atualizarContadores();
    }
    private void desfazer() {
        if(historico.isEmpty()) {status("Nenhuma alteração para desfazer.");return;}
        try { interromper();estado=PersistenciaJSON.desserializar(historico.removeLast());
            sujo=true;refletirEstado();status("Última ação desfeita.");}
        catch(Exception e){erro(e);}
    }
    private JFileChooser seletor(String extensao,String descricao) {
        JFileChooser chooser=new JFileChooser();chooser.setFileFilter(new FileNameExtensionFilter(descricao,extensao));
        return chooser;
    }
    private void salvar() {
        atualizarNomesEquipes();
        JFileChooser chooser=seletor("json","Prancheta tática (*.json)");
        if(arquivoAtual!=null)chooser.setSelectedFile(arquivoAtual);
        else chooser.setSelectedFile(new File("jogada_tatica.json"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        File f=chooser.getSelectedFile();if(!f.getName().toLowerCase().endsWith(".json"))f=new File(f.getPath()+".json");
        try {PersistenciaJSON.salvar(f,estado);arquivoAtual=f;sujo=false;atualizarContadores();
            status("Esquema salvo em: "+f.getAbsolutePath());}
        catch(Exception e){erro(e);}
    }
    private void abrir() {
        JFileChooser chooser=seletor("json","Prancheta tática (*.json)");
        if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try {EstadoTatico carregado=PersistenciaJSON.abrir(chooser.getSelectedFile());
            interromper();estado=carregado;arquivoAtual=chooser.getSelectedFile();historico.clear();sujo=false;
            refletirEstado();status("Esquema carregado: "+arquivoAtual.getName());}
        catch(Exception e){erro(e);}
    }
    private void exportarImagem() {
        JFileChooser chooser=seletor("png","Imagem do campo (*.png)");
        chooser.setSelectedFile(new File("prancheta_tatica.png"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        File f=chooser.getSelectedFile();if(!f.getName().toLowerCase().endsWith(".png"))f=new File(f.getPath()+".png");
        try {ImageIO.write(campo.gerarImagem(),"png",f);status("Campo exportado em PNG.");}
        catch(Exception e){erro(e);}
    }
    private void interromper() {if(timer!=null)timer.stop();campo.finalizarAnimacao();}
    private void reproduzir() {
        if(timer!=null && timer.isRunning()) {interromper();status("Reprodução interrompida.");return;}
        sequenciaAnimacao=new ArrayList<Jogada>();
        for(Jogada j:estado.jogadas)
            if(j.tipo.equals("PASSE")||j.tipo.equals("CHUTE")||j.tipo.equals("MOVIMENTO"))sequenciaAnimacao.add(j);
        if(sequenciaAnimacao.isEmpty()){status("Desenhe passes, chutes ou deslocamentos antes de reproduzir.");return;}
        etapaAnimacao=0;quadroAnimacao=0;
        timer=new Timer(28,(ActionEvent e)->{
            if(etapaAnimacao>=sequenciaAnimacao.size()) {interromper();status("Reprodução concluída.");return;}
            Jogada j=sequenciaAnimacao.get(etapaAnimacao);
            campo.mostrarAnimacao(j,Math.min(1,quadroAnimacao/30.0));
            quadroAnimacao++;
            if(quadroAnimacao>30){quadroAnimacao=0;etapaAnimacao++;}
        });
        timer.start();status("Reproduzindo as rotas na ordem em que foram desenhadas.");
    }
    private void ajuda() {
        JTextArea texto=new JTextArea(
            "1. Escolha uma ferramenta na barra lateral.\n"+
            "2. SELECIONAR: arraste jogadores e bola; clique para editar um atleta.\n"+
            "3. PASSE/CHUTE/DESLOCAMENTO/MARCAÇÃO: pressione, arraste e solte.\n"+
            "4. ZONA: clique nos três vértices da área de pressão.\n"+
            "5. Aplique uma das formações para cada time.\n"+
            "6. Adicione reservas no painel lateral; selecione um titular e um reserva\n"+
            "   para fazer a substituição.\n"+
            "7. Salve a jogada em JSON e abra depois; também exporte PNG.\n"+
            "8. Bola parada: escolha Pênalti, Falta ou Escanteio e o time cobrador.\n"+
            "   O programa posiciona goleiro, cobrador, barreira e demais atletas.\n"+
            "   Arraste jogadores para ajustar a estratégia.\n"+
            "9. Reproduzir mostra passes, chutes e deslocamentos em sequência.\n"+
            "10. Atalhos: Ctrl+S para salvar, Ctrl+O para abrir e Ctrl+Z para desfazer.\n");
        texto.setEditable(false);texto.setFont(new Font("SansSerif",Font.PLAIN,13));texto.setMargin(new Insets(15,15,15,15));
        JOptionPane.showMessageDialog(this,new JScrollPane(texto),"Manual rápido",JOptionPane.INFORMATION_MESSAGE);
    }
    private void atalhos() {
        JRootPane root=getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_DOWN_MASK),"salvar");
        root.getActionMap().put("salvar",new AbstractAction(){public void actionPerformed(ActionEvent e){salvar();}});
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.CTRL_DOWN_MASK),"abrir");
        root.getActionMap().put("abrir",new AbstractAction(){public void actionPerformed(ActionEvent e){abrir();}});
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK),"desfazer");
        root.getActionMap().put("desfazer",new AbstractAction(){public void actionPerformed(ActionEvent e){desfazer();}});
    }
}
