PRANCHETA PRO - Simulador Tatico de Futebol
Projeto Extensionista de Computacao Grafica / Estruturas Dinamicas

INTEGRANTES (conforme relatorio apresentado):
- Joao Lucas de Lima Souza - RA00360044
- Pedro Chagas Neves de Farias Nascimento - RA00359511
- Guilherme Pereira de Rivoredo - RA00359303
- Eduardo Achkar Avancini - RA00359889

ABRIR NO BLUEJ
1. Descompacte o ZIP.
2. Abra a pasta Prancheta_Tatica_Futebol_BlueJ pelo menu Projeto > Abrir Projeto.
3. Compile todas as classes.
4. Clique com o botao direito na classe Main e execute main(String[] args).

EXECUTAR SEM BLUEJ
- Utilize o arquivo Prancheta_Tatica_Futebol.jar na pasta principal da entrega.
- Em computador com Java 8+ instalado: java -jar Prancheta_Tatica_Futebol.jar

OPERACOES
- Selecione um atleta ou a bola e arraste para reposicionar.
- Selecione uma das 12 formacoes em cada equipe; pressione Aplicar formacao.
- Selecione o atleta no campo, altere Nome e Numero e salve.
- Para adicionar reserva, informe nome, numero e equipe e clique Adicionar ao banco.
- Para substituir, selecione titular no campo + reserva da mesma equipe na lista.
- Ferramentas Passe, Chute, Movimento e Marcacao: pressione, arraste e solte.
- Ferramenta Zona de pressao: clique em tres pontos para formar o poligono.
- Bola parada: Escanteio direito/esquerdo, falta frontal ou penalti.
- Reproduzir: anima passes, chutes e deslocamentos registrados.
- Salvar JSON e Abrir JSON: persistencia completa e recuperacao de planos.
- Exportar PNG: imagem do campo, atletas, bola e marcacoes.
- Limpar jogadas: apaga linhas e areas, preserva atletas.
- Desfazer: Ctrl+Z. Salvar: Ctrl+S. Abrir: Ctrl+O.

FORMACOES
4-3-3, 4-4-2, 4-2-3-1, 3-5-2, 3-4-3, 4-1-4-1,
5-3-2, 4-5-1, 4-3-2-1, 3-4-2-1, 4-2-2-2 e 4-1-2-1-2.

ARQUIVO DE EXEMPLO
exemplo_jogada.json: abrir por Arquivo > Abrir JSON no programa.

TESTE AUTOMATICO
Compile e execute TestesProjeto.main(String[] args) (sem interface grafica).

Tecnologia: Java Swing, Graphics2D, Bresenham, coordenadas 105 x 68 m.
Sem dependencias externas. Projeto de finalidade educacional.


ATUALIZACAO - BOLAS PARADAS E BOTOES
- Botoes claros com texto escuro, totalmente visivel sem passar o mouse.
- O botao ativo de ferramenta fica azul com texto branco.
- Menu Bola parada: Penalti Azul/Vermelho e Falta frontal Azul/Vermelho.
- Penalti: cobrador perto da marca, goleiro na linha, demais jogadores fora
  da grande area e a pelo menos 9,15 m da bola.
- Falta frontal: barreira a mais de 9,15 m da bola, goleiro e ataques posicionados.
- Escanteios: equipes reposicionadas no campo, alem da bola na bandeirinha.
- O cenario e salvo no JSON e exibido no campo e PNG. JSON antigos continuam abrindo.
- Para alterar as posicoes apos aplicar, use Selecionar/mover e arraste os atletas.
- Use Desfazer para recuperar o esquema anterior a aplicacao do cenario.
