CREATE TABLE IF NOT EXISTS marca (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    CONSTRAINT uk_marca_nome UNIQUE (nome)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS modelo (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    marca_id BIGINT NOT NULL,
    CONSTRAINT uk_modelo_marca_nome UNIQUE (marca_id, nome),
    CONSTRAINT fk_modelo_marca FOREIGN KEY (marca_id) REFERENCES marca(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS veiculo (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    modelo_id BIGINT NOT NULL,
    ano_fabricacao INT NOT NULL,
    cor VARCHAR(50) NOT NULL,
    preco DECIMAL(12,2) NOT NULL,
    quilometragem INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    CONSTRAINT fk_veiculo_modelo FOREIGN KEY (modelo_id) REFERENCES modelo(id),
    CONSTRAINT ck_preco CHECK (preco > 0),
    CONSTRAINT ck_quilometragem CHECK (quilometragem >= 0),
    CONSTRAINT ck_status CHECK (status IN ('DISPONIVEL','VENDIDO','DESCONTINUADO')),
    CONSTRAINT ck_tipo CHECK (tipo IN ('CARRO','MOTO','UTILITARIO')),
    INDEX idx_preco (preco),
    INDEX idx_ano (ano_fabricacao),
    INDEX idx_status (status)
) ENGINE=InnoDB;
