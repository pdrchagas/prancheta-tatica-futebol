/** Testes automáticos independentes da interface gráfica. */
public class TestesProjeto {
    private static void verificar(boolean ok,String mensagem) {
        if(!ok)throw new AssertionError(mensagem);
        System.out.println("OK: "+mensagem);
    }
    private static void testarBolasParadas() {
        for (String equipe: new String[]{"AZUL", "VERMELHO"}) {
            EstadoTatico p=EstadoTatico.novo();
            p.jogadores.get(1).nome="Artilheiro";
            p.jogadores.get(1).numero=99;
            p.adicionarReserva("AZUL","Banco teste",23);
            BolasParadas.aplicar(p,BolasParadas.PENALTI,equipe);
            verificar(p.totalTitulares("AZUL")==11 && p.totalTitulares("VERMELHO")==11,
                      "Penalti "+equipe+": 11 titulares de cada time");
            verificar(Math.abs(p.bolaX-(equipe.equals("AZUL")?94:11))<0.01,
                      "Penalti "+equipe+": bola na marca correta");
            verificar(p.jogadores.get(1).numero==99 && p.jogadores.get(1).nome.equals("Artilheiro")
                      && p.reservas("AZUL").size()==1,
                      "Personalizacao e banco preservados no penalti");
            String defensor=equipe.equals("AZUL")?"VERMELHO":"AZUL";
            int atacantesFora=0;
            int z=0;
            for (Jogador j:p.jogadores) {
                if (!j.titular)continue;
                if (j.equipe.equals(equipe)) {
                    if (++z>2) {
                        verificar(equipe.equals("AZUL") ? j.x<88.5 : j.x>16.5,
                                  "Atacante fora da grande area");
                        atacantesFora++;
                    }
                } else {
                    if (j.numero!=1) {
                        verificar(equipe.equals("AZUL") ? j.x<88.5 : j.x>16.5,
                                  "Defensor fora da grande area");
                    }
                }
            }
            verificar(atacantesFora==9,"Nove atacantes fora da area");
            EstadoTatico r=PersistenciaJSON.desserializar(PersistenciaJSON.serializar(p));
            verificar(r.situacao.equals(p.situacao),"Situacao do penalti persistida no JSON");
            BolasParadas.aplicar(p,BolasParadas.FALTA,equipe);
            verificar(Math.abs(p.bolaX-(equipe.equals("AZUL")?82:23))<0.01,
                      "Falta "+equipe+": bola no local esperado");
            // Goleiro adversario + cinco jogadores na barreira.
            int naBarreira=0;
            for (Jogador j:p.jogadores) if (j.titular && j.equipe.equals(defensor)) {
                double distancia=Geometria2D.distancia(j.x,j.y,p.bolaX,p.bolaY);
                if (distancia>=9.15 && distancia<12.0) naBarreira++;
            }
            verificar(naBarreira>=5,"Falta "+equipe+": cinco atletas proximos da barreira");
            BolasParadas.aplicar(p,BolasParadas.ESCANTEIO,equipe);
            verificar(equipe.equals("AZUL")?p.bolaX>104:p.bolaX<1,
                      "Escanteio "+equipe+": posicao da bola na bandeirinha");
        }
        // JSON legado nao possui situacao: deve continuar abrindo.
        EstadoTatico antigo=PersistenciaJSON.desserializar(
            PersistenciaJSON.serializar(EstadoTatico.novo()).replace("\"situacao\":\"JOGO LIVRE\",", ""));
        verificar("JOGO LIVRE".equals(antigo.situacao),"JSON antigo continua compativel");
    }

    public static void main(String[] args) throws Exception {
        EstadoTatico e=EstadoTatico.novo();
        verificar(e.totalTitulares("AZUL")==11 && e.totalTitulares("VERMELHO")==11,
                 "11 jogadores em cada lado");
        for(String f:Formacoes.OPCOES) {
            double[][] p=Formacoes.posicoes(f,true);
            verificar(p.length==11,"Formação "+f);
        }
        Jogador j=e.jogadores.get(1);
        j.nome="João \"Técnico\" / ç";j.numero=77;
        Formacoes.aplicar(e,"AZUL","4-2-3-1");
        verificar(j.nome.equals("João \"Técnico\" / ç") && j.numero==77,"Personalização preservada");
        Jogador r=e.adicionarReserva("AZUL","Meia Reserva",18);
        verificar(e.reservas("AZUL").size()==1,"Banco de reservas");
        e.jogadas.add(new Jogada("PASSE",23,30,60,38,0,0,j.id));
        e.jogadas.add(new Jogada("ZONA",12,20,28,21,25,40,-1));
        String json=PersistenciaJSON.serializar(e);
        EstadoTatico carregado=PersistenciaJSON.desserializar(json);
        verificar(carregado.jogadores.size()==23 && carregado.jogadas.size()==2,
                 "JSON salvando jogadores, reservas e jogadas");
        verificar(carregado.buscar(j.id).nome.equals(j.nome),"JSON preserva acentos e aspas");
        verificar(Math.abs(Geometria2D.distanciaSegmento(3,4,0,0,10,0)-4)<.0001,
                 "Geometria de ponto e segmento");
        r.titular=true;
        verificar(e.totalTitulares("AZUL")==12,"Troca de estado reserva/titular");
        testarBolasParadas();
        System.out.println("TODOS OS TESTES CONCLUÍDOS.");
    }
}
