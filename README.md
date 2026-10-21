<div align="center">

# MindInk

**Publique e leia artigos sobre tecnologia, direto do Android.**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black)
![Min SDK](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)
![Target SDK](https://img.shields.io/badge/targetSdk-35-3DDC84?logo=android&logoColor=white)

[English](en/README.md) · [Funcionalidades](#funcionalidades) · [Como executar](#como-executar) · [Arquitetura](#arquitetura)

</div>

---

## Sobre o projeto

O **MindInk** é um aplicativo Android para publicação e leitura de artigos sobre tecnologia: desenvolvimento, inteligência artificial, dispositivos móveis, cloud e temas relacionados.

Os destaques são o login social (Google e GitHub), perfis públicos de autores, edição de artigos em Markdown e **leitura em voz alta** para acessibilidade.

> **Projeto acadêmico:** A3 de Usabilidade, Desenvolvimento Web, Mobile e Jogos.

<!--
Adicione aqui capturas de tela do app. Exemplo:

## Telas

| Início | Artigo | Editor | Perfil |
| :---: | :---: | :---: | :---: |
| ![Início](docs/screenshots/home.png) | ![Artigo](docs/screenshots/article.png) | ![Editor](docs/screenshots/editor.png) | ![Perfil](docs/screenshots/profile.png) |
-->

## Sumário

- [MindInk](#mindink)
  - [Sobre o projeto](#sobre-o-projeto)
  - [Sumário](#sumário)
  - [Funcionalidades](#funcionalidades)
    - [Conta e autenticação](#conta-e-autenticação)
    - [Artigos](#artigos)
    - [Autores](#autores)
    - [Acessibilidade](#acessibilidade)
  - [Tecnologias](#tecnologias)
  - [Como executar](#como-executar)
    - [Requisitos](#requisitos)
    - [Passo a passo](#passo-a-passo)
    - [Comandos úteis](#comandos-úteis)
  - [Arquitetura](#arquitetura)
    - [Fluxo de dependências](#fluxo-de-dependências)
    - [Responsabilidade por pacote](#responsabilidade-por-pacote)
    - [Fluxo de atualização do nome do autor](#fluxo-de-atualização-do-nome-do-autor)
  - [Modelo de dados (Firestore)](#modelo-de-dados-firestore)
    - [`articles/{articleId}`](#articlesarticleid)
    - [`user_profiles/{uid}`](#user_profilesuid)
  - [Segurança e limitações conhecidas](#segurança-e-limitações-conhecidas)
    - [Regras do Firestore](#regras-do-firestore)
    - [Pendências antes de ir para produção](#pendências-antes-de-ir-para-produção)
  - [Testes](#testes)
  - [Estrutura do projeto](#estrutura-do-projeto)
  - [Equipe](#equipe)

---

## Funcionalidades

### Conta e autenticação

- **Login social** com Google ou GitHub via Firebase Authentication.
- **Vinculação de contas:** quando os provedores entram em conflito, o app oferece o fluxo para vincular as duas contas.
- **Configurações:** edição de nome e links sociais (GitHub e LinkedIn). O e-mail vem do provedor de autenticação e é somente leitura.
- **Exclusão de conta** com confirmação e tratamento da exigência de autenticação recente.
- **Menu do avatar** com acesso separado a perfil, configurações e sair.

### Artigos

- **Criação e edição** com título, subtítulo, categoria, conteúdo em Markdown e pré-visualização.
- **Leitura** com renderização de Markdown.
- **Página inicial** com artigos recentes, autores e lista paginada para explorar mais conteúdo.
- **Avaliação por estrelas:** a interface destaca a estrela de quem já curtiu; contagem e lista de usuários são atualizadas no Firestore.

### Autores

- **Perfil público** com artigos publicados, total de estrelas e links opcionais.
- **Sincronização de nome:** ao salvar um novo nome, o app atualiza o perfil no Firebase e o campo `authorName` em todos os artigos do autor, em lotes paginados.

### Acessibilidade

- **Leitura em voz alta** (Android TextToSpeech) de título, subtítulo e conteúdo em português, com remoção da sintaxe Markdown e controle para iniciar ou parar.

---

## Tecnologias

| Categoria | Tecnologia |
| --- | --- |
| Linguagem | Kotlin |
| Interface | Jetpack Compose, Material 3 e Compose BOM |
| Arquitetura | MVVM, com separação em `ui`, `domain`, `data` e `core` |
| Injeção de dependência | Hilt e KSP |
| Autenticação | Firebase Authentication, Credential Manager, provedores Google e GitHub |
| Banco de dados | Cloud Firestore |
| Imagens | Coil 3 |
| Markdown | multiplatform-markdown-renderer |
| Voz | Android TextToSpeech |
| Testes | JUnit 4 |

> As versões exatas de SDKs, plugins e bibliotecas estão em `app/build.gradle.kts` e `gradle/libs.versions.toml`.

---

## Como executar

### Requisitos

- Android Studio compatível com o Android Gradle Plugin do projeto
- JDK 21 para o daemon do Gradle (definido em `gradle/gradle-daemon-jvm.properties`)
- Dispositivo ou emulador com Android **API 24 ou superior**
- Um projeto no [Firebase Console](https://console.firebase.google.com/)

> O código Java/Kotlin usa compatibilidade com Java 11. O projeto compila contra a API 37 e define `targetSdk` 35.

### Passo a passo

1. **Clone o repositório** e abra-o no Android Studio.
2. **Adicione o `google-services.json`** na pasta `app/` (o arquivo é ignorado pelo Git).
3. **Habilite os provedores** Google e GitHub no Firebase Authentication e configure as credenciais OAuth de cada um.
4. **Cadastre o SHA-1** do keystore de debug no projeto Firebase.
5. **Configure o Cloud Firestore** e publique as regras de referência de [`docs/firestore.rules.txt`](docs/firestore.rules.txt) pelo Firebase Console. O Gradle não as aplica automaticamente.
6. **Sincronize o Gradle** e execute o app no dispositivo ou emulador.

### Comandos úteis

```bash
# Gerar o APK de debug
./gradlew :app:assembleDebug

# Rodar os testes unitários
./gradlew :app:testDebugUnitTest
```

---

## Arquitetura

O app é um único módulo Android (`:app`) seguindo **MVVM** com Clean Architecture simplificada.

- O `MainActivity` controla os destinos principais com estado Compose salvo e callbacks de navegação.
- As telas observam os ViewModels por `StateFlow`.
- Os listeners do Firestore são expostos como `Flow` (via `callbackFlow`) e mantêm artigos, avaliações e perfis atualizados em tempo real.
- Operações pontuais usam coroutines e aguardam as tarefas assíncronas do Firebase.
- A camada `data` não depende dos modelos de apresentação.

### Fluxo de dependências

```text
Compose Screen -> ViewModel -> UseCase -> Repository (interface)
                                                  ^
                                                  |
                                     Repository (implementação)
```

### Responsabilidade por pacote

| Pacote | Responsabilidade |
| --- | --- |
| `core/` | Formatação de nomes, preparação de Markdown para voz e suporte a TextToSpeech |
| `domain/model/` | Modelos independentes das APIs do Firebase e da interface |
| `domain/repository/` | Contratos de persistência e autenticação, com resultados tipados |
| `domain/usecase/` | Operações de aplicação para artigos, autenticação e configurações |
| `data/article/` | DTOs, conversão Firestore ↔ domínio e repositório de artigos |
| `data/auth/` | Firebase Authentication, Google Credential Manager e GitHub OAuth |
| `data/settings/` | DTO e persistência das configurações em `user_profiles` |
| `ui/articles/` | Lista, detalhes, editor, componentes e modelos de apresentação |
| `ui/articles/mapper/` | Conversão de modelos de domínio para modelos dos cards |
| `ui/auth/` | Login, estado de sessão e vinculação de provedores |
| `ui/profile/` | Perfil público do autor e links sociais |
| `ui/settings/` | Tela, estado e ViewModel das configurações da conta |
| `ui/splash/` | Tela de abertura |
| `ui/components/` e `ui/theme/` | Componentes compartilhados e tokens visuais |
| `di/` | Bindings e providers do Hilt |

### Fluxo de atualização do nome do autor

1. O perfil do usuário é salvo primeiro.
2. O repositório consulta todos os artigos por `authorUid`, em páginas.
3. `authorName` é atualizado em lotes de até 450 documentos.
4. O nome no Firebase Authentication é atualizado.
5. O app só exibe sucesso depois que todos os lotes e a atualização do nome terminarem.

> [!NOTE]
> Se ocorrer uma falha após alguns lotes, a sincronização pode ficar parcial. Salvar novamente repete a atualização para os artigos restantes.

---

## Modelo de dados (Firestore)

### `articles/{articleId}`

| Campo | Descrição |
| --- | --- |
| `authorUid` | UID do proprietário; usado para carregar o perfil e sincronizar o nome |
| `authorName`, `authorUsername`, `authorPhotoUrl` | Dados de exibição do autor |
| `category`, `title`, `subtitle`, `content`, `linkUrl` | Conteúdo do artigo |
| `starredBy` | UIDs dos usuários que avaliaram o artigo |
| `starCount` | Total de estrelas, atualizado na mesma transação de `starredBy` |
| `createdAt`, `updatedAt` | Datas de criação e atualização |

A lista de exploração combina observação em tempo real com carregamento paginado.

### `user_profiles/{uid}`

| Campo | Descrição |
| --- | --- |
| `name` | Nome de exibição |
| `github` | Link do GitHub (opcional) |
| `linkedin` | Link do LinkedIn (opcional) |

- O e-mail **não** é salvo nesse documento: ele vem do Firebase Authentication.
- Os campos antigos de idade e Instagram são removidos quando as configurações são salvas.

---

## Segurança e limitações conhecidas

### Regras do Firestore

O arquivo [`docs/firestore.rules.txt`](docs/firestore.rules.txt) contém a versão de referência das regras. Elas permitem:

- que cada usuário autenticado leia e grave apenas o próprio `user_profiles/{uid}`;
- leitura pública de artigos, com escrita de conteúdo restrita ao autor autenticado;
- atualização dos campos de estrelas por qualquer usuário autenticado.

### Pendências antes de ir para produção

> [!WARNING]
> **Regra de estrelas:** ela limita quais campos podem ser alterados, mas não verifica se a mudança em `starredBy` envolve apenas o UID de quem está autenticado. Isso permite manipular avaliações de outros usuários. Restrinja essa validação antes de publicar.

> [!WARNING]
> **Exclusão de conta:** a operação atual remove somente a identidade do Firebase Authentication. O documento `user_profiles/{uid}` e os artigos já publicados permanecem. Para apagá-los também, implemente a limpeza em um backend confiável (por exemplo, uma Cloud Function acionada pela exclusão da conta). Não conceda ao cliente permissões amplas de exclusão.

---

## Testes

Os testes unitários atuais cobrem:

- conversão de artigos para cards;
- formatação de nomes;
- preparação de Markdown para leitura em voz alta.

```bash
./gradlew :app:testDebugUnitTest
```

Os fluxos de interface e a integração com o Firebase ainda não têm testes instrumentados ou de integração. O teste instrumentado de exemplo foi removido, e a ampliação da cobertura está prevista para etapas futuras.

> **Escopo acadêmico:** esta versão prioriza a entrega dos fluxos principais e uma arquitetura clara.

---

## Estrutura do projeto

```text
app/src/
├── main/
│   ├── java/com/mindInk/app/
│   │   ├── core/
│   │   ├── data/
│   │   │   ├── article/
│   │   │   ├── auth/
│   │   │   └── settings/
│   │   ├── di/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   ├── ui/
│   │   │   ├── articles/
│   │   │   ├── auth/
│   │   │   ├── components/
│   │   │   ├── profile/
│   │   │   ├── settings/
│   │   │   ├── splash/
│   │   │   └── theme/
│   │   ├── MainActivity.kt
│   │   └── MindInkApplication.kt
│   └── res/
├── test/            # Testes unitários (JVM)
└── androidTest/     # Testes instrumentados (Android)
```

---

## Equipe

**Orientador:** Prof. Daniel Henrique Matos de Paiva

| Nome | RA |
| --- | --- |
| Eliseu Pereira da Silva | 124220479 |
| Maycon Soares De Resende | 125111404445 |
| Fabricio Rocha Souza Neves | 125111399975 |
| Gustavo Silva Lourenço | 125111410319 |
| Gustavo Morais de Oliveira Siqueira | 1242022304 |
| Bruno Resende Ribeiro | 124220140 |
| Ana Júlia Brum | 124222016 |
