import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Viewport da prancheta. Converte o campo real 105x68 m em pixels,
 * desenha as primitivas gráficas e implementa a translação elástica.
 */
public class CampoFutebol extends JPanel {
    public enum Ferramenta { SELECIONAR, PASSE, CHUTE, MOVIMENTO, MARCACAO, ZONA }
    private final TelaPrincipal tela;
    private Ferramenta ferramenta = Ferramenta.SELECIONAR;
    private Jogador selecionado;
    private boolean bolaSelecionada;
    private boolean arrastando;
    private double inicioX, inicioY, atualX, atualY;
    private int zonaEtapa;
    private double zonaX1,zonaY1,zonaX2,zonaY2;
    private String antesArrasto;
    private Jogada animacao;
    private double progressoAnimacao = -1;
    private static final Color AZUL = new Color(36,108,236);
    private static final Color VERMELHO = new Color(230,68,77);

    public CampoFutebol(TelaPrincipal tela) {
        this.tela = tela;
        setOpaque(true);
        setBackground(new Color(14,31,38));
        MouseAdapter mouse = new MouseAdapter() {
            public void mousePressed(MouseEvent e) { pressionar(e); }
            public void mouseDragged(MouseEvent e) { arrastar(e); }
            public void mouseReleased(MouseEvent e) { soltar(e); }
            public void mouseMoved(MouseEvent e) {
                double[] p = converter(e.getPoint());
                tela.status(String.format("Coordenadas: %.1f m × %.1f m  |  %s", p[0],p[1], ferramenta));
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setFerramenta(Ferramenta f) {
        ferramenta = f; arrastando = false; zonaEtapa=0;
        repaint();
    }
    public Ferramenta getFerramenta() { return ferramenta; }
    public Jogador getSelecionado() { return selecionado; }
    public void selecionar(Jogador j) {
        selecionado=j;
        bolaSelecionada=false;
        tela.atualizarSelecao(j);
        repaint();
    }
    public void limparSelecao() { selecionar(null); }
    public void mudarEstado() { selecionado=null; bolaSelecionada=false; zonaEtapa=0; arrastando=false; repaint(); }
    public void mostrarAnimacao(Jogada j, double progresso) { animacao=j; progressoAnimacao=progresso; repaint(); }
    public void finalizarAnimacao() { animacao=null; progressoAnimacao=-1; repaint(); }

    /** Mantém a proporção oficial do campo ao redimensionar a janela. */
    private Rectangle viewport() {
        int margem=20;
        double escala = Math.min((getWidth()-2.0*margem)/105.0, (getHeight()-2.0*margem)/68.0);
        escala=Math.max(0.2, escala);
        int w=(int)Math.round(105*escala),h=(int)Math.round(68*escala);
        return new Rectangle((getWidth()-w)/2,(getHeight()-h)/2,w,h);
    }
    private int xx(double x) { Rectangle r=viewport(); return r.x+(int)Math.round(x*r.width/105.0); }
    private int yy(double y) { Rectangle r=viewport(); return r.y+(int)Math.round(y*r.height/68.0); }
    private double[] converter(Point p) {
        Rectangle r=viewport();
        return new double[]{ Math.max(0,Math.min(105,105.0*(p.x-r.x)/r.width)),
                             Math.max(0,Math.min(68,68.0*(p.y-r.y)/r.height)) };
    }
    private Color corTime(Jogador j) { return j.equipe.equals("AZUL") ? AZUL : VERMELHO; }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D c=(Graphics2D)g.create();
        c.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        c.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle r=viewport();
        c.setColor(new Color(5,15,23,100)); c.fillRoundRect(r.x+5,r.y+9,r.width,r.height,18,18);
        c.setColor(new Color(31,117,64)); c.fillRect(r.x,r.y,r.width,r.height);
        for (int i=0;i<14;i++) {
            if ((i&1)==0) { c.setColor(new Color(255,255,255,10));
                int x=r.x+i*r.width/14; int w=(i+1)*r.width/14-i*r.width/14;
                c.fillRect(x,r.y,w,r.height); }
        }
        c.setColor(new Color(242,255,239,210));
        c.setStroke(new BasicStroke(1.8f));
        c.drawRect(r.x+2,r.y+2,r.width-4,r.height-4);
        c.drawLine(xx(52.5),yy(0),xx(52.5),yy(68));
        int raio=(int)Math.round(r.width*9.15/105);
        c.drawOval(xx(52.5)-raio,yy(34)-raio,2*raio,2*raio);
        c.fillOval(xx(52.5)-3,yy(34)-3,6,6);
        // Áreas técnicas e gols: coordenadas em metros.
        retCampo(c,0,13.84,16.5,40.32); retCampo(c,105-16.5,13.84,16.5,40.32);
        retCampo(c,0,24.84,5.5,18.32); retCampo(c,99.5,24.84,5.5,18.32);
        c.fillOval(xx(11)-2,yy(34)-2,4,4);
        c.fillOval(xx(94)-2,yy(34)-2,4,4);
        c.setColor(new Color(249,250,251,220));
        c.setStroke(new BasicStroke(3));
        c.drawRect(xx(0)-7,yy(30),7,yy(38)-yy(30));
        c.drawRect(xx(105),yy(30),7,yy(38)-yy(30));
        c.setClip(r.x,r.y,r.width,r.height);
        // Indica o cenario selecionado tambem no PNG exportado.
        if (!"JOGO LIVRE".equals(tela.estado().situacao)) {
            String legenda = tela.estado().situacao;
            c.setFont(new Font("SansSerif",Font.BOLD,14));
            int w=c.getFontMetrics().stringWidth(legenda)+22;
            c.setColor(new Color(6,29,41,205));
            c.fillRoundRect(r.x+13,r.y+12,w,31,10,10);
            c.setColor(Color.WHITE);
            c.drawString(legenda,r.x+24,r.y+33);
        }
        for (Jogada j:tela.estado().jogadas) desenharJogada(c,j,false);
        if (arrastando && ferramenta!=Ferramenta.SELECIONAR) {
            Jogada temp=new Jogada(ferramenta.name(),inicioX,inicioY,atualX,atualY,0,0,
                selecionado==null ? -1 : selecionado.id);
            desenharJogada(c,temp,true);
        }
        if (zonaEtapa>0) {
            c.setStroke(new BasicStroke(2)); c.setColor(new Color(255,211,87,220));
            c.drawLine(xx(zonaX1),yy(zonaY1),xx(atualX),yy(atualY));
            if (zonaEtapa==2) {
                c.drawLine(xx(zonaX1),yy(zonaY1),xx(zonaX2),yy(zonaY2));
                c.drawLine(xx(zonaX2),yy(zonaY2),xx(atualX),yy(atualY));
            }
        }
        for (Jogador j:tela.estado().jogadores) {
            if (!j.titular) continue;
            if (animacao!=null && animacao.tipo.equals("MOVIMENTO") && j.id==animacao.jogadorId) continue;
            desenharJogador(c,j);
        }
        if (animacao==null || (!animacao.tipo.equals("PASSE") && !animacao.tipo.equals("CHUTE")))
            desenharBola(c,tela.estado().bolaX,tela.estado().bolaY,bolaSelecionada);
        if (animacao!=null && progressoAnimacao>=0) desenharAnimacao(c);
        c.dispose();
    }

    private void retCampo(Graphics2D c,double x,double y,double w,double h) {
        c.draw(new Rectangle2D.Double(xx(x),yy(y),xx(x+w)-xx(x),yy(y+h)-yy(y)));
    }

    private void desenharJogador(Graphics2D c,Jogador j) {
        int px=xx(j.x),py=yy(j.y),rad=15;
        c.setColor(new Color(0,0,0,65)); c.fillOval(px-rad+2,py-rad+4,rad*2,rad*2);
        if (j==selecionado) {c.setColor(new Color(255,219,72));c.fillOval(px-rad-5,py-rad-5,2*rad+10,2*rad+10);}
        c.setColor(corTime(j));c.fillOval(px-rad,py-rad,2*rad,2*rad);
        c.setStroke(new BasicStroke(2)); c.setColor(Color.WHITE);c.drawOval(px-rad,py-rad,2*rad,2*rad);
        c.setFont(new Font("SansSerif",Font.BOLD,13));
        String n=String.valueOf(j.numero); int largura=c.getFontMetrics().stringWidth(n);
        c.drawString(n,px-largura/2,py+5);
        // Rótulo com nome legível mesmo com atletas próximos.
        c.setFont(new Font("SansSerif",Font.BOLD,10));
        String nome=j.nome.length()>15 ? j.nome.substring(0,14)+"…" : j.nome;
        int w=c.getFontMetrics().stringWidth(nome)+8;
        Rectangle area=viewport();
        int xLabel=px-w/2;
        int yLabel=py+18;
        // Na barreira de falta, nomes ficam ao lado dos atletas para evitar
        // sobreposicao vertical de etiquetas com jogadores adjacentes.
        if (tela.estado().situacao.startsWith("FALTA FRONTAL") &&
                ((Math.abs(j.x-91.5)<0.2 && Math.abs(j.y-34)<8) ||
                 (Math.abs(j.x-13.5)<0.2 && Math.abs(j.y-34)<8))) {
            xLabel = j.x>52.5 ? px+17 : px-w-17;
            yLabel = py-8;
        }
        xLabel=Math.max(area.x+2,Math.min(area.x+area.width-w-2,xLabel));
        yLabel=Math.max(area.y+2,Math.min(area.y+area.height-16,yLabel));
        c.setColor(new Color(8,25,28,195));c.fillRoundRect(xLabel,yLabel,w,15,6,6);
        c.setColor(Color.WHITE);c.drawString(nome,xLabel+4,yLabel+11);
    }

    private void desenharBola(Graphics2D c,double x,double y,boolean selecionada) {
        int px=xx(x),py=yy(y),r=10;
        if (selecionada) {c.setColor(new Color(255,213,61));c.fillOval(px-15,py-15,30,30);}
        c.setColor(Color.WHITE);c.fillOval(px-r,py-r,r*2,r*2);
        c.setColor(new Color(25,38,42));c.setStroke(new BasicStroke(1));c.drawOval(px-r,py-r,r*2,r*2);
        Path2D path=new Path2D.Double();
        for (int i=0;i<5;i++) {
            double a=-Math.PI/2+2*Math.PI*i/5;
            double tx=px+5*Math.cos(a),ty=py+5*Math.sin(a);
            if (i==0)path.moveTo(tx,ty);else path.lineTo(tx,ty);
        }
        path.closePath();c.fill(path);
    }

    private Color corJogada(String tipo) {
        if (tipo.equals("PASSE")) return new Color(255,253,168);
        if (tipo.equals("CHUTE")) return new Color(244,112,112);
        if (tipo.equals("MOVIMENTO")) return new Color(136,225,255);
        if (tipo.equals("MARCACAO")) return new Color(255,182,74);
        return new Color(255,224,107);
    }

    private void desenharJogada(Graphics2D c,Jogada j,boolean temporario) {
        c.setColor(corJogada(j.tipo));
        if (j.tipo.equals("ZONA")) {
            Path2D pol=new Path2D.Double();
            pol.moveTo(xx(j.x1),yy(j.y1));pol.lineTo(xx(j.x2),yy(j.y2));
            pol.lineTo(xx(j.x3),yy(j.y3));pol.closePath();
            Color base=corJogada(j.tipo);
            c.setColor(new Color(base.getRed(),base.getGreen(),base.getBlue(),55));c.fill(pol);
            c.setColor(base);c.setStroke(new BasicStroke(2));c.draw(pol);return;
        }
        int ax=xx(j.x1),ay=yy(j.y1),bx=xx(j.x2),by=yy(j.y2);
        c.setStroke(new BasicStroke(temporario?2:3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        if (j.tipo.equals("MARCACAO")) {
            Geometria2D.bresenham(c,ax,ay,bx,by,3);
        } else c.drawLine(ax,ay,bx,by);
        double ang=Math.atan2(by-ay,bx-ax);
        int ponta=12;
        c.drawLine(bx,by,bx-(int)(ponta*Math.cos(ang-.48)),by-(int)(ponta*Math.sin(ang-.48)));
        c.drawLine(bx,by,bx-(int)(ponta*Math.cos(ang+.48)),by-(int)(ponta*Math.sin(ang+.48)));
        if (temporario) {
            c.setColor(new Color(255,255,255,130));c.fillOval(bx-4,by-4,8,8);
        }
    }

    private void desenharAnimacao(Graphics2D c) {
        double x=animacao.x1+(animacao.x2-animacao.x1)*progressoAnimacao;
        double y=animacao.y1+(animacao.y2-animacao.y1)*progressoAnimacao;
        if (animacao.tipo.equals("PASSE") || animacao.tipo.equals("CHUTE")) {
            desenharBola(c,x,y,true);
        } else if (animacao.tipo.equals("MOVIMENTO")) {
            Jogador base = tela.estado().buscar(animacao.jogadorId);
            if (base!=null) {
                // Reproduz a translação sem modificar o estado real nem o arquivo salvo.
                double antigoX=base.x, antigoY=base.y;
                base.x=x; base.y=y; desenharJogador(c,base);
                base.x=antigoX; base.y=antigoY;
            } else {
                c.setColor(new Color(130,241,255));c.setStroke(new BasicStroke(3));
                c.drawOval(xx(x)-17,yy(y)-17,34,34);
            }
        }
    }

    private Jogador acharJogador(Point p) {
        Jogador achado=null; double menor=18;
        for (Jogador j:tela.estado().jogadores) {
            if (!j.titular)continue;
            double d=Geometria2D.distancia(p.x,p.y,xx(j.x),yy(j.y));
            if (d<menor) {menor=d;achado=j;}
        }
        return achado;
    }

    private void pressionar(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e) || !viewport().contains(e.getPoint()))return;
        double[] p=converter(e.getPoint());
        if (ferramenta==Ferramenta.SELECIONAR) {
            if (Geometria2D.distancia(e.getX(),e.getY(),xx(tela.estado().bolaX),yy(tela.estado().bolaY))<14) {
                selecionado=null;bolaSelecionada=true;tela.atualizarSelecao(null);
            } else {
                selecionado=acharJogador(e.getPoint());bolaSelecionada=false;
                tela.atualizarSelecao(selecionado);
            }
            if (selecionado!=null || bolaSelecionada) {arrastando=true;antesArrasto=PersistenciaJSON.serializar(tela.estado());}
        } else if (ferramenta==Ferramenta.ZONA) {
            if (zonaEtapa==0) {zonaX1=p[0];zonaY1=p[1];zonaEtapa=1;}
            else if (zonaEtapa==1) {zonaX2=p[0];zonaY2=p[1];zonaEtapa=2;}
            else {
                tela.registrarAntes();
                tela.estado().jogadas.add(new Jogada("ZONA",zonaX1,zonaY1,zonaX2,zonaY2,p[0],p[1],-1));
                tela.alterado();zonaEtapa=0;
            }
        } else {
            inicioX=p[0];inicioY=p[1];atualX=inicioX;atualY=inicioY;arrastando=true;
        }
        repaint();
    }
    private void arrastar(MouseEvent e) {
        if (!arrastando)return;
        double[] p=converter(e.getPoint());
        if (ferramenta==Ferramenta.SELECIONAR) {
            if (selecionado!=null) {selecionado.x=p[0];selecionado.y=p[1];}
            else if (bolaSelecionada) {tela.estado().bolaX=p[0];tela.estado().bolaY=p[1];}
        } else {atualX=p[0];atualY=p[1];}
        repaint();
    }
    private void soltar(MouseEvent e) {
        if (!arrastando)return;
        double[] p=converter(e.getPoint());
        if (ferramenta==Ferramenta.SELECIONAR) {
            String depois=PersistenciaJSON.serializar(tela.estado());
            if (!depois.equals(antesArrasto)) {tela.registrarAntes(antesArrasto);tela.alterado();}
        } else {
            atualX=p[0];atualY=p[1];
            if (Geometria2D.distancia(inicioX,inicioY,atualX,atualY)>.5) {
                tela.registrarAntes();
                tela.estado().jogadas.add(new Jogada(ferramenta.name(),inicioX,inicioY,
                    atualX,atualY,0,0,selecionado==null?-1:selecionado.id));
                tela.alterado();
            }
        }
        arrastando=false;repaint();
    }

    /** Exporta o campo, jogadores e anotações em PNG. */
    public BufferedImage gerarImagem() {
        BufferedImage im=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D c=im.createGraphics();paint(c);c.dispose();return im;
    }
}
