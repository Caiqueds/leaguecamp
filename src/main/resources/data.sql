-- Campeonatos (ligas)
INSERT INTO CAMPEONATO (ID_CAMP, NOME, EDICAO) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Campeonato Brasileiro Série A', 2026),
    (2, 'Campeonato Brasileiro Série A', 2027)
ON CONFLICT (ID_CAMP) DO NOTHING;

-- Clubes
INSERT INTO CLUBE (ID_CLUBE, NOME, CIDADE) OVERRIDING SYSTEM VALUE VALUES
    (1,  'Flamengo',    'Rio de Janeiro'),
    (2,  'Fluminense',  'Rio de Janeiro'),
    (3,  'Palmeiras',   'São Paulo'),
    (4,  'Corinthians', 'São Paulo'),
    (5,  'São Paulo',   'São Paulo'),
    (6,  'Santos',      'Santos'),
    (7,  'Atlético-MG', 'Belo Horizonte'),
    (8,  'Cruzeiro',    'Belo Horizonte'),
    (9,  'Grêmio',      'Porto Alegre'),
    (10, 'Internacional','Porto Alegre')
ON CONFLICT (ID_CLUBE) DO NOTHING;

-- Estádios (Flamengo e Fluminense jogam no Maracanã)
INSERT INTO ESTADIO (ID_ESTADIO, NOME, CIDADE, CAPACIDADE) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Maracanã',          'Rio de Janeiro', 78838),
    (2, 'Allianz Parque',    'São Paulo',      43713),
    (3, 'Neo Química Arena', 'São Paulo',      49205),
    (4, 'Morumbis',          'São Paulo',      66795),
    (5, 'Vila Belmiro',      'Santos',         16068),
    (6, 'Mineirão',          'Belo Horizonte', 61846),
    (7, 'Arena MRV',         'Belo Horizonte', 46000),
    (8, 'Beira-Rio',         'Porto Alegre',   50128),
    (9, 'Arena do Grêmio',   'Porto Alegre',   55662)
ON CONFLICT (ID_ESTADIO) DO NOTHING;

-- Árbitros
INSERT INTO ARBITRO (ID_ARB, PNOME, UNOME, DATA_NASC) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Ricardo',  'Pacheco', '1983-03-15'),
    (2, 'Marcelo',  'Tavares', '1980-07-22'),
    (3, 'Anderson', 'Moraes',  '1987-11-02'),
    (4, 'Wilson',   'Bastos',  '1985-01-30')
ON CONFLICT (ID_ARB) DO NOTHING;

-- Equipe: ids 1 a 30 são atletas (3 por clube), ids 31 a 40 são comissão técnica (1 por clube)
INSERT INTO EQUIPE (ID_EQ, PNOME, UNOME, DATA_NASC, ID_CLUBE) OVERRIDING SYSTEM VALUE VALUES
    (1,  'Rafael',   'Moura',      '1996-02-10', 1),
    (2,  'Bruno',    'Teixeira',   '1998-06-21', 1),
    (3,  'Diego',    'Farias',     '1994-09-05', 1),
    (4,  'Lucas',    'Andrade',    '1997-01-18', 2),
    (5,  'Mateus',   'Barros',     '1999-04-27', 2),
    (6,  'Thiago',   'Rocha',      '1992-12-03', 2),
    (7,  'Gustavo',  'Pires',      '1995-08-14', 3),
    (8,  'Felipe',   'Duarte',     '1996-05-30', 3),
    (9,  'Henrique', 'Lopes',      '1993-10-09', 3),
    (10, 'Caio',     'Ribeiro',    '2000-03-12', 4),
    (11, 'Vinícius', 'Prado',      '1998-11-25', 4),
    (12, 'André',    'Cardoso',    '1991-07-07', 4),
    (13, 'Leandro',  'Matos',      '1997-09-16', 5),
    (14, 'Otávio',   'Brandão',    '1999-02-28', 5),
    (15, 'Renan',    'Siqueira',   '1994-06-11', 5),
    (16, 'Pedro',    'Lima',       '1998-12-19', 6),
    (17, 'Murilo',   'Campos',     '2001-01-23', 6),
    (18, 'Fábio',    'Nogueira',   '1995-04-04', 6),
    (19, 'Eduardo',  'Neves',      '1996-07-19', 7),
    (20, 'Gabriel',  'Sampaio',    '1997-10-01', 7),
    (21, 'Rodrigo',  'Tavares',    '1993-03-26', 7),
    (22, 'Samuel',   'Veloso',     '1999-08-08', 8),
    (23, 'Davi',     'Pinheiro',   '2000-05-15', 8),
    (24, 'Jonas',    'Albuquerque','1992-01-29', 8),
    (25, 'Igor',     'Fontes',     '1998-02-17', 9),
    (26, 'Nelson',   'Quadros',    '1996-12-12', 9),
    (27, 'Alex',     'Medeiros',   '1994-04-22', 9),
    (28, 'Tiago',    'Vasques',    '1997-06-30', 10),
    (29, 'Cleber',   'Amorim',     '1995-09-09', 10),
    (30, 'Enzo',     'Carvalho',   '2001-10-14', 10),
    (31, 'Carlos',   'Mendes',     '1970-05-20', 1),
    (32, 'Roberto',  'Alves',      '1968-09-13', 2),
    (33, 'Sérgio',   'Batista',    '1975-03-08', 3),
    (34, 'Marcos',   'Teles',      '1972-11-30', 4),
    (35, 'Paulo',    'Guedes',     '1980-08-17', 5),
    (36, 'Ivan',     'Santana',    '1966-02-02', 6),
    (37, 'Wagner',   'Lacerda',    '1971-04-25', 7),
    (38, 'Orlando',  'Beltrão',    '1969-12-05', 8),
    (39, 'Julio',    'Frota',      '1974-07-11', 9),
    (40, 'Nilton',   'Esteves',    '1967-10-28', 10)
ON CONFLICT (ID_EQ) DO NOTHING;

-- Atletas: o primeiro de cada clube é atacante, o segundo é meia e o terceiro é goleiro ou zagueiro
INSERT INTO ATLETA (ID_EQ, POSICAO, NUMERO_CAMISA) VALUES
    (1,  'Atacante', 9),  (2,  'Meia', 10), (3,  'Goleiro',  1),
    (4,  'Atacante', 11), (5,  'Meia', 8),  (6,  'Zagueiro', 4),
    (7,  'Atacante', 9),  (8,  'Meia', 10), (9,  'Goleiro',  1),
    (10, 'Atacante', 11), (11, 'Meia', 8),  (12, 'Zagueiro', 4),
    (13, 'Atacante', 9),  (14, 'Meia', 10), (15, 'Goleiro',  1),
    (16, 'Atacante', 11), (17, 'Meia', 8),  (18, 'Zagueiro', 4),
    (19, 'Atacante', 9),  (20, 'Meia', 10), (21, 'Goleiro',  1),
    (22, 'Atacante', 11), (23, 'Meia', 8),  (24, 'Zagueiro', 4),
    (25, 'Atacante', 9),  (26, 'Meia', 10), (27, 'Goleiro',  1),
    (28, 'Atacante', 11), (29, 'Meia', 8),  (30, 'Zagueiro', 4)
ON CONFLICT (ID_EQ) DO NOTHING;

INSERT INTO COMISSAO_TECNICA (ID_EQ, FUNCAO) VALUES
    (31, 'TECNICO'),
    (32, 'TECNICO'),
    (33, 'AUXILIAR_TECNICO'),
    (34, 'TECNICO'),
    (35, 'PREPARADOR_FISICO'),
    (36, 'TECNICO'),
    (37, 'TECNICO'),
    (38, 'AUXILIAR_TECNICO'),
    (39, 'TECNICO'),
    (40, 'PREPARADOR_FISICO')
ON CONFLICT (ID_EQ) DO NOTHING;

-- Clubes inscritos: os 10 na edição 2026 e os 10 na edição 2027 (que ainda não tem partidas)
INSERT INTO PARTICIPANTES (ID_CLUBE, ID_CAMP) VALUES
    (1, 1), (2, 1), (3, 1), (4, 1), (5, 1),
    (6, 1), (7, 1), (8, 1), (9, 1), (10, 1),
    (1, 2), (2, 2), (3, 2), (4, 2), (5, 2),
    (6, 2), (7, 2), (8, 2), (9, 2), (10, 2)
ON CONFLICT (ID_CLUBE, ID_CAMP) DO NOTHING;

-- Partidas do Brasileirão: rodadas 1 a 3 finalizadas, rodada 4 agendada (uma adiada)
INSERT INTO PARTIDA (ID_PARTIDA, RODADA, DATA_HORA, GOLS_CASA, GOLS_FORA, STATUS, ID_CAMP, ID_CLUBE_CASA, ID_CLUBE_FORA, ID_ESTADIO) OVERRIDING SYSTEM VALUE VALUES
    -- Rodada 1
    (1,  1, '2026-10-24 16:00', 2, 1, 'FINALIZADA', 1, 1,  2,  1),
    (2,  1, '2026-10-24 18:30', 0, 0, 'FINALIZADA', 1, 3,  4,  2),
    (3,  1, '2026-10-25 16:00', 3, 1, 'FINALIZADA', 1, 5,  6,  4),
    (4,  1, '2026-10-25 18:30', 1, 2, 'FINALIZADA', 1, 7,  8,  7),
    (5,  1, '2026-10-25 21:00', 2, 2, 'FINALIZADA', 1, 9,  10, 9),
    -- Rodada 2
    (6,  2, '2026-10-31 16:00', 2, 0, 'FINALIZADA', 1, 3,  1,  2),
    (7,  2, '2026-10-31 18:30', 1, 1, 'FINALIZADA', 1, 4,  5,  3),
    (8,  2, '2026-11-01 16:00', 3, 2, 'FINALIZADA', 1, 6,  7,  5),
    (9,  2, '2026-11-01 18:30', 0, 1, 'FINALIZADA', 1, 8,  9,  6),
    (10, 2, '2026-11-01 21:00', 2, 2, 'FINALIZADA', 1, 10, 2,  8),
    -- Rodada 3
    (11, 3, '2026-11-07 16:00', 3, 0, 'FINALIZADA', 1, 1,  4,  1),
    (12, 3, '2026-11-07 18:30', 1, 1, 'FINALIZADA', 1, 2,  3,  1),
    (13, 3, '2026-11-08 16:00', 2, 0, 'FINALIZADA', 1, 5,  8,  4),
    (14, 3, '2026-11-08 18:30', 1, 3, 'FINALIZADA', 1, 10, 6,  8),
    (15, 3, '2026-11-08 21:00', 0, 0, 'FINALIZADA', 1, 7,  9,  7),
    -- Rodada 4
    (16, 4, '2026-11-21 16:00', 0, 0, 'AGENDADA',   1, 1,  6,  1),
    (17, 4, '2026-11-21 18:30', 0, 0, 'AGENDADA',   1, 5,  2,  4),
    (18, 4, '2026-11-22 16:00', 0, 0, 'AGENDADA',   1, 7,  3,  7),
    (19, 4, '2026-11-22 18:30', 0, 0, 'ADIADA',     1, 9,  4,  9),
    (20, 4, '2026-11-22 21:00', 0, 0, 'AGENDADA',   1, 8,  10, 6)
ON CONFLICT (ID_PARTIDA) DO NOTHING;

-- Árbitros escalados: partidas finalizadas têm 3 árbitros, as demais só o principal
INSERT INTO APITA (ID_ARB, ID_PARTIDA, FUNCAO) VALUES
    (1, 1,  'PRINCIPAL'), (2, 1,  'ASSISTENTE_1'), (3, 1,  'ASSISTENTE_2'),
    (2, 2,  'PRINCIPAL'), (3, 2,  'ASSISTENTE_1'), (4, 2,  'ASSISTENTE_2'),
    (3, 3,  'PRINCIPAL'), (4, 3,  'ASSISTENTE_1'), (1, 3,  'ASSISTENTE_2'),
    (4, 4,  'PRINCIPAL'), (1, 4,  'ASSISTENTE_1'), (2, 4,  'ASSISTENTE_2'),
    (1, 5,  'PRINCIPAL'), (2, 5,  'ASSISTENTE_1'), (3, 5,  'ASSISTENTE_2'),
    (2, 6,  'PRINCIPAL'), (3, 6,  'ASSISTENTE_1'), (4, 6,  'ASSISTENTE_2'),
    (3, 7,  'PRINCIPAL'), (4, 7,  'ASSISTENTE_1'), (1, 7,  'ASSISTENTE_2'),
    (4, 8,  'PRINCIPAL'), (1, 8,  'ASSISTENTE_1'), (2, 8,  'ASSISTENTE_2'),
    (1, 9,  'PRINCIPAL'), (2, 9,  'ASSISTENTE_1'), (3, 9,  'ASSISTENTE_2'),
    (2, 10, 'PRINCIPAL'), (3, 10, 'ASSISTENTE_1'), (4, 10, 'ASSISTENTE_2'),
    (3, 11, 'PRINCIPAL'), (4, 11, 'ASSISTENTE_1'), (1, 11, 'ASSISTENTE_2'),
    (4, 12, 'PRINCIPAL'), (1, 12, 'ASSISTENTE_1'), (2, 12, 'ASSISTENTE_2'),
    (1, 13, 'PRINCIPAL'), (2, 13, 'ASSISTENTE_1'), (3, 13, 'ASSISTENTE_2'),
    (2, 14, 'PRINCIPAL'), (3, 14, 'ASSISTENTE_1'), (4, 14, 'ASSISTENTE_2'),
    (3, 15, 'PRINCIPAL'), (4, 15, 'ASSISTENTE_1'), (1, 15, 'ASSISTENTE_2'),
    (1, 16, 'PRINCIPAL'),
    (2, 17, 'PRINCIPAL'),
    (3, 18, 'PRINCIPAL'),
    (4, 19, 'PRINCIPAL'),
    (1, 20, 'PRINCIPAL')
ON CONFLICT (ID_ARB, ID_PARTIDA) DO NOTHING;

-- Súmula: GOL e GOL_CONTRA somados batem com o placar de cada partida
-- GOL_CONTRA conta para o time adversário do jogador que errou
INSERT INTO SUMULA (ID_SUMULA, TIPO_EVENTO, MIN, ID_PARTIDA, ID_MEMBRO) OVERRIDING SYSTEM VALUE VALUES
    -- Partida 1: Flamengo 2 x 1 Fluminense
    (1,  'GOL',             23, 1,  1),
    (2,  'CARTAO_AMARELO',  40, 1,  5),
    (3,  'GOL',             55, 1,  4),
    (4,  'GOL',             67, 1,  2),
    -- Partida 2: Palmeiras 0 x 0 Corinthians
    (5,  'CARTAO_AMARELO',  30, 2,  8),
    (6,  'CARTAO_VERMELHO', 78, 2,  11),
    -- Partida 3: São Paulo 3 x 1 Santos
    (7,  'GOL',             12, 3,  13),
    (8,  'GOL',             34, 3,  16),
    (9,  'GOL',             58, 3,  14),
    (10, 'CARTAO_AMARELO',  70, 3,  17),
    (11, 'GOL',             80, 3,  13),
    -- Partida 4: Atlético-MG 1 x 2 Cruzeiro
    (12, 'GOL',             10, 4,  19),
    (13, 'GOL',             44, 4,  22),
    (14, 'CARTAO_AMARELO',  60, 4,  20),
    (15, 'GOL',             81, 4,  23),
    -- Partida 5: Grêmio 2 x 2 Internacional
    (16, 'GOL',             15, 5,  25),
    (17, 'GOL',             33, 5,  28),
    (18, 'GOL',             50, 5,  28),
    (19, 'GOL',             79, 5,  26),
    (20, 'CARTAO_VERMELHO', 90, 5,  27),
    -- Partida 6: Palmeiras 2 x 0 Flamengo
    (21, 'GOL',             20, 6,  7),
    (22, 'GOL',             52, 6,  8),
    (23, 'CARTAO_AMARELO',  66, 6,  2),
    -- Partida 7: Corinthians 1 x 1 São Paulo
    (24, 'GOL',             28, 7,  10),
    (25, 'GOL',             73, 7,  13),
    (26, 'CARTAO_AMARELO',  80, 7,  12),
    -- Partida 8: Santos 3 x 2 Atlético-MG
    (27, 'GOL',              8, 8,  16),
    (28, 'GOL',             25, 8,  19),
    (29, 'GOL',             39, 8,  17),
    (30, 'GOL',             61, 8,  20),
    (31, 'GOL',             74, 8,  16),
    (32, 'CARTAO_VERMELHO', 86, 8,  21),
    -- Partida 9: Cruzeiro 0 x 1 Grêmio
    (33, 'CARTAO_AMARELO',  45, 9,  24),
    (34, 'GOL',             67, 9,  25),
    -- Partida 10: Internacional 2 x 2 Fluminense
    (35, 'GOL',             18, 10, 28),
    (36, 'GOL',             40, 10, 4),
    (37, 'GOL',             55, 10, 5),
    (38, 'GOL',             77, 10, 29),
    -- Partida 11: Flamengo 3 x 0 Corinthians (um gol contra de jogador do Corinthians)
    (39, 'CARTAO_AMARELO',  33, 11, 10),
    (40, 'GOL',             14, 11, 1),
    (41, 'GOL_CONTRA',      50, 11, 11),
    (42, 'GOL',             77, 11, 2),
    -- Partida 12: Fluminense 1 x 1 Palmeiras
    (43, 'GOL',             35, 12, 4),
    (44, 'CARTAO_AMARELO',  55, 12, 6),
    (45, 'GOL',             82, 12, 7),
    -- Partida 13: São Paulo 2 x 0 Cruzeiro
    (46, 'GOL',             22, 13, 13),
    (47, 'GOL',             70, 13, 14),
    (48, 'CARTAO_VERMELHO', 88, 13, 24),
    -- Partida 14: Internacional 1 x 3 Santos
    (49, 'GOL',             11, 14, 17),
    (50, 'GOL',             30, 14, 16),
    (51, 'GOL',             52, 14, 28),
    (52, 'CARTAO_AMARELO',  66, 14, 29),
    (53, 'GOL',             85, 14, 16),
    -- Partida 15: Atlético-MG 0 x 0 Grêmio
    (54, 'CARTAO_AMARELO',  40, 15, 20),
    (55, 'CARTAO_AMARELO',  72, 15, 26)
ON CONFLICT (ID_SUMULA) DO NOTHING;

-- Acerta os contadores de id depois de inserir ids manuais
-- Sem isso, o próximo cadastro pela aplicação tentaria usar o id 1 e daria erro
SELECT SETVAL(PG_GET_SERIAL_SEQUENCE('campeonato', 'id_camp'),    (SELECT MAX(ID_CAMP)    FROM CAMPEONATO));
SELECT SETVAL(PG_GET_SERIAL_SEQUENCE('clube',      'id_clube'),   (SELECT MAX(ID_CLUBE)   FROM CLUBE));
SELECT SETVAL(PG_GET_SERIAL_SEQUENCE('estadio',    'id_estadio'), (SELECT MAX(ID_ESTADIO) FROM ESTADIO));
SELECT SETVAL(PG_GET_SERIAL_SEQUENCE('arbitro',    'id_arb'),     (SELECT MAX(ID_ARB)     FROM ARBITRO));
SELECT SETVAL(PG_GET_SERIAL_SEQUENCE('equipe',     'id_eq'),      (SELECT MAX(ID_EQ)      FROM EQUIPE));
SELECT SETVAL(PG_GET_SERIAL_SEQUENCE('partida',    'id_partida'), (SELECT MAX(ID_PARTIDA) FROM PARTIDA));
SELECT SETVAL(PG_GET_SERIAL_SEQUENCE('sumula',     'id_sumula'),  (SELECT MAX(ID_SUMULA)  FROM SUMULA));