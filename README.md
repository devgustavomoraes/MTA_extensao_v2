# ProjetoDeExten-oSIGI
Este é um projeto de extensão acadêmica que visa desenvolver uma plataforma digital integrada para gestão institucional. O sistema atua como um CRM para a gestão de membros e uma agenda centralizada de eventos, resolvendo o problema atual de descentralização de dados, registros em papel e falhas de comunicação via aplicativos de mensagens.

<h1 align="center">
  Plataforma de Gestão Institucional (CRM & Agenda)
</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white" alt="Vercel" />
  <img src="https://img.shields.io/badge/Render-%2346E3B7.svg?style=for-the-badge&logo=render&logoColor=white" alt="Render" />
</p>

> Projeto de extensão universitária focado em desenvolver uma plataforma digital integrada para a gestão institucional de uma ONG, composta por um CRM (Gestão de Membros) e uma Agenda de Eventos.

## 📌 Sobre o Projeto

Atualmente, a instituição sofre com a descentralização de dados e ruídos na comunicação, utilizando registros informais em papel ou planilhas isoladas. O objetivo deste sistema é centralizar as informações, garantindo que a liderança tenha controle total sobre os registros de membros e a organização de escalas em um ambiente seguro e de fácil acesso.

O projeto foi inteiramente arquitetado para funcionar com **custo zero** de infraestrutura para a instituição.

### 🎯 Público-Alvo
- **Primário:** Administração e liderança da instituição (controle e gestão).
- **Secundário:** Membros e voluntários (consulta de escalas, horários e avisos via PWA).

## 🚀 Tecnologias e Arquitetura

O sistema segue a arquitetura de um Progressive Web App (PWA) de alta disponibilidade:

- **Back-end:** Java com Spring Boot.
- **Banco de Dados:** MySQL hospedado no TiDB Cloud (Sempre gratuito).
- **Front-end:** PWA (Progressive Web App), acessível via navegador e mobile.
- **Hospedagem:** Render (Back-end) e Vercel (Front-end).
- **CI/CD & Versionamento:** GitHub.

## ✨ Funcionalidades (MVP)

### 🔐 Autenticação e Segurança
- [x] Cadastro, login e logout de usuários.
- [x] Recuperação e redefinição de senha.
- [x] Controle de acesso por perfil (Administrador e Usuário).
- [x] Bloqueio após 5 tentativas de login inválidas.
- [x] Senhas armazenadas com criptografia.

### 👥 Módulo CRM (Gestão de Membros)
- [x] Cadastro, edição e exclusão de membros (Apenas Admin).
- [x] Registro de funções e controle de participação dos membros.

### 📅 Módulo Agenda (Eventos)
- [x] Criação, visualização, edição e exclusão de eventos.
- [x] Associação de membros aos eventos (Escalas).
- [x] Criação e gestão de avisos institucionais.
- [x] Notificações e lembretes de eventos próximos.

## ⚙️ Requisitos Não Funcionais (Destaques)
- Suporte a no mínimo 100 usuários simultâneos.
- Disponibilidade 24 horas por dia.
- Tempo de aprendizado da interface inferior a 10 minutos para usuários leigos.
- Código estruturado em módulos independentes seguindo boas práticas.

## 🛠️ Como executar o projeto localmente

### Pré-requisitos
- Java 17+
- Maven ou Gradle
- MySQL local ou conexão com o TiDB Cloud configurada
