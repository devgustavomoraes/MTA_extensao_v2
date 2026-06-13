-- Schema ProjetoMTA — compatível com TiDB Cloud / MySQL 8
-- Execute manualmente no TiDB quando usar perfil prod (ddl-auto: validate)
-- Em dev (ddl-auto: update) o Hibernate cria/atualiza automaticamente

CREATE DATABASE IF NOT EXISTS projeto_mta
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE projeto_mta;

CREATE TABLE IF NOT EXISTS usuarios (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    email            VARCHAR(255) NOT NULL,
    senha_hash       VARCHAR(255) NOT NULL,
    perfil           ENUM('ADMIN', 'USUARIO') NOT NULL DEFAULT 'USUARIO',
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    tentativas_login INT          NOT NULL DEFAULT 0,
    bloqueado_ate    DATETIME     NULL,
    criado_em        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS membros (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nome_completo   VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NULL,
    telefone        VARCHAR(20)  NULL,
    funcao          VARCHAR(100) NULL,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    observacoes     TEXT         NULL,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_membros_nome (nome_completo),
    KEY idx_membros_ativo (ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS eventos (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    titulo          VARCHAR(255) NOT NULL,
    descricao       TEXT         NULL,
    data_inicio     DATETIME     NOT NULL,
    data_fim        DATETIME     NOT NULL,
    local           VARCHAR(255) NULL,
    criado_por      BIGINT       NULL,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_eventos_data_inicio (data_inicio),
    CONSTRAINT fk_eventos_criado_por FOREIGN KEY (criado_por) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS escalas (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    evento_id       BIGINT       NOT NULL,
    membro_id       BIGINT       NOT NULL,
    funcao_escala   VARCHAR(100) NULL,
    confirmado      BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_escala_evento_membro (evento_id, membro_id),
    CONSTRAINT fk_escalas_evento FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    CONSTRAINT fk_escalas_membro FOREIGN KEY (membro_id) REFERENCES membros(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS avisos (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    titulo          VARCHAR(255) NOT NULL,
    mensagem        TEXT         NOT NULL,
    prioridade      ENUM('NORMAL', 'IMPORTANTE', 'URGENTE') NOT NULL DEFAULT 'NORMAL',
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    data_expiracao  DATETIME     NULL,
    publicado_por   BIGINT       NULL,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_avisos_prioridade (prioridade),
    KEY idx_avisos_ativo (ativo),
    CONSTRAINT fk_avisos_publicado_por FOREIGN KEY (publicado_por) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
