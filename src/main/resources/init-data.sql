-- Script de inicialização - MaturidadeTI v2.0
-- Execute este script após criar o banco de dados

-- Usuários de exemplo (senha: admin123)
INSERT INTO usuarios (email, senha, nome, sobrenome, role, ativo, criado_em, atualizado_em) VALUES
('admin@maturidadeti.com', '$2a$10$8.UnVuG9HHgffUZ9r3i1Ou8t8aM8KIUgfH8KvUVHVH8JZ5Z5Z5Z5Z', 'Administrador', 'Sistema', 'ADMIN', true, NOW(), NOW()),
('avaliador@maturidadeti.com', '$2a$10$8.UnVuG9HHgffUZ9r3i1Ou8t8aM8KIUgfH8KvUVHVH8JZ5Z5Z5Z5Z', 'João', 'Silva', 'AVALIADOR', true, NOW(), NOW()),
('gestor@maturidadeti.com', '$2a$10$8.UnVuG9HHgffUZ9r3i1Ou8t8aM8KIUgfH8KvUVHVH8JZ5Z5Z5Z5Z', 'Maria', 'Santos', 'GESTOR', true, NOW(), NOW());

-- Empresas de exemplo
INSERT INTO empresas (razao_social, cnpj, setor, porte, status, responsavel, email, telefone, criado_em, atualizado_em) VALUES
('Grupo Alpha Consultoria', '11.222.333/0001-44', 'FINANCEIRO', 'GRANDE', 'ATIVO', 'Carlos Lima', 'carlos@alpha.com', '(71) 99999-1111', NOW(), NOW()),
('Inova Saúde Tecnologia', '55.666.777/0001-88', 'SAUDE', 'MEDIA', 'ATIVO', 'Ana Souza', 'ana@inova.com', '(71) 99999-2222', NOW(), NOW()),
('TecWise Sistemas', '99.888.777/0001-66', 'TECNOLOGIA', 'PEQUENA', 'PENDENTE', 'Bruno Costa', 'bruno@tecwise.com', '(71) 99999-3333', NOW(), NOW());

-- Questões de exemplo - Governança
INSERT INTO questoes (area, enunciado, framework, criticidade, evidencia_esperada, ativa, criado_em, atualizado_em) VALUES
('GOVERNANCA', 'A empresa possui uma estrutura de governança de TI formalmente estabelecida?', 'COBIT', 'ALTA', 'Documento de governança, organograma', true, NOW(), NOW()),
('GOVERNANCA', 'Existe um comitê de TI ou direcionamento executivo sobre TI?', 'COBIT', 'ALTA', 'Ata de constituição do comitê, registro de reuniões', true, NOW(), NOW()),
('GOVERNANCA', 'A empresa alinha a estratégia de TI com a estratégia de negócio?', 'COBIT', 'MEDIA', 'Plano estratégico de TI, documento de alinhamento', true, NOW(), NOW());

-- Questões de exemplo - Segurança
INSERT INTO questoes (area, enunciado, framework, criticidade, evidencia_esperada, ativa, criado_em, atualizado_em) VALUES
('SEGURANCA', 'Existe uma política de segurança de informação documentada e aprovada?', 'ISO 27001', 'ALTA', 'Política formal, aprovação gerencial', true, NOW(), NOW()),
('SEGURANCA', 'A empresa realiza testes de penetração ou varreduras de vulnerabilidade?', 'ISO 27001', 'ALTA', 'Relatórios de testes, plano de remediação', true, NOW(), NOW()),
('SEGURANCA', 'Existe controle de acesso e autenticação multifator?', 'ISO 27001', 'MEDIA', 'Documentação de controles, logs de acesso', true, NOW(), NOW());

-- Questões de exemplo - Infraestrutura
INSERT INTO questoes (area, enunciado, framework, criticidade, evidencia_esperada, ativa, criado_em, atualizado_em) VALUES
('INFRAESTRUTURA', 'A empresa possui um plano de continuidade e recuperação de desastres?', 'ITIL', 'ALTA', 'Plano de continuidade, testes realizados', true, NOW(), NOW()),
('INFRAESTRUTURA', 'Os servidores e sistemas críticos possuem redundância?', 'ITIL', 'ALTA', 'Documentação de infraestrutura, diagramas', true, NOW(), NOW()),
('INFRAESTRUTURA', 'Existe monitoramento contínuo da infraestrutura de TI?', 'ITIL', 'MEDIA', 'Ferramentas de monitoramento, alertas configurados', true, NOW(), NOW());

-- Questões de exemplo - Desenvolvimento
INSERT INTO questoes (area, enunciado, framework, criticidade, evidencia_esperada, ativa, criado_em, atualizado_em) VALUES
('DESENVOLVIMENTO', 'A empresa segue um processo de desenvolvimento de software estruturado?', 'COBIT', 'MEDIA', 'Metodologia documentada, processos definidos', true, NOW(), NOW()),
('DESENVOLVIMENTO', 'Existe controle de versão e integração contínua?', 'COBIT', 'MEDIA', 'Repositório de código, pipeline de CI/CD', true, NOW(), NOW()),
('DESENVOLVIMENTO', 'Os requisitos de segurança são considerados no desenvolvimento?', 'ISO 27001', 'ALTA', 'Documentação de requisitos, testes de segurança', true, NOW(), NOW());

-- Questões de exemplo - Suporte
INSERT INTO questoes (area, enunciado, framework, criticidade, evidencia_esperada, ativa, criado_em, atualizado_em) VALUES
('SUPORTE', 'Existe um plano de suporte técnico com SLA definido?', 'ITIL', 'MEDIA', 'Contrato de suporte, documentação de SLA', true, NOW(), NOW()),
('SUPORTE', 'A empresa possui equipe de suporte treinada e certificada?', 'ITIL', 'MEDIA', 'Certificações dos técnicos, registros de treinamento', true, NOW(), NOW()),
('SUPORTE', 'Existe sistema de tickets ou gestão de incidentes?', 'ITIL', 'MEDIA', 'Sistema implantado, métricas de atendimento', true, NOW(), NOW());

-- Questões de exemplo - Gestão de Riscos
INSERT INTO questoes (area, enunciado, framework, criticidade, evidencia_esperada, ativa, criado_em, atualizado_em) VALUES
('RISCO', 'A empresa realiza avaliação de riscos de TI regularmente?', 'ISO 38500', 'ALTA', 'Relatório de avaliação de riscos, matriz de riscos', true, NOW(), NOW()),
('RISCO', 'Existe um plano de mitigação de riscos identificados?', 'ISO 38500', 'ALTA', 'Plano de mitigação, ações implementadas', true, NOW(), NOW()),
('RISCO', 'A empresa possui seguros de TI e cibersegurança?', 'ISO 38500', 'MEDIA', 'Apólices de seguro, cobertas contratada', true, NOW(), NOW());
