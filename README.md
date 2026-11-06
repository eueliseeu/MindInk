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

O **MindInk** é um aplicativo Android para publicação e leitura de artigos sobre tecnologia: desenvolvimento, inteligência artificial, mobile, cloud e outros temas relacionados.

Os destaques são o login social (Google e GitHub), perfis públicos de autores, edição em Markdown com pré-visualização, feed da comunidade com filtros e scroll infinito, tratamento de offline e **leitura em voz alta** para acessibilidade.

> **Projeto acadêmico:** A3 de Usabilidade, Desenvolvimento Web, Mobile e Jogos.

<!--
Adicione aqui capturas de tela do app. Exemplo:

## Telas

| Início | Comunidade | Artigo | Editor |
| :---: | :---: | :---: | :---: |
| ![Início](docs/screenshots/home.png) | ![Comunidade](docs/screenshots/community.png) | ![Artigo](docs/screenshots/article.png) | ![Editor](docs/screenshots/editor.png) |
-->

## Sumário

- [MindInk](#mindink)
  - [Sobre o projeto](#sobre-o-projeto)
  - [Sumário](#sumário)
  - [Funcionalidades](#funcionalidades)
    - [Conta e autenticação](#conta-e-autenticação)
    - [Home (usuário logado)](#home-usuário-logado)
    - [Comunidade e artigos](#comunidade-e-artigos)
    - [Experiência de leitura](#experiência-de-leitura)
    - [Autores](#autores)
    - [Modo convidado](#modo-convidado)
    - [Acessibilidade](#acessibilidade)
  - [Tecnologias](#tecnologias)
  - [Como executar](#como-executar)
    - [Requisitos](#requisitos)
    - [Passo a passo](#passo-a-passo)
    - [Índices do Firestore](#índices-do-firestore)
    - [Comandos úteis](#comandos-úteis)
  - [Arquitetura](#arquitetura)
    - [Fluxo de dependências](#fluxo-de-dependências)
    - [Responsabilidade por pacote](#responsabilidade-por-pacote)
    - [Fluxo de atualização do nome do autor](#fluxo-de-atualização-do-nome-do-autor)
  - [Modelo de dados (Firestore)](#modelo-de-dados-firestore)
    - [`articles/{articleId}`](#articlesarticleid)
    - [`articles/{articleId}/content/body`](#articlesarticleidcontentbody)
    - [`user_profiles/{uid}`](#user_profilesuid)
    - [Outras coleções](#outras-coleções)
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
- **Vinculação de contas** quando os provedores entram em conflito.
- **Configurações:** edição de nome e links sociais (GitHub e LinkedIn). O e-mail vem do provedor e é somente leitura.
- **Exclusão de conta** com confirmação e tratamento de autenticação recente.
- **Menu do avatar** com perfil, configurações e sair (borda de evento opcional no avatar da top bar).

### Home (usuário logado)

- Saudação e acesso à comunidade.
- **Mural:** estrelas recebidas nos seus artigos e novos posts de autores que você curtiu (limite de itens; abre perfil ou artigo conforme o tipo).
- **Continue lendo:** artigos abertos sem scroll até o fim (Firestore para logados; pendências de guest podem sincronizar no login).
- Autores favoritos a partir das estrelas que você deu.

### Comunidade e artigos

- **Tela Comunidade:** filtro por categoria (**Comunidade** = todos, ou Tech / IA / Mobile / Cloud / DevOps) no servidor, busca local nos itens já carregados, scroll infinito, botão voltar ao topo e FAB de criar.
- **Criação e edição** na mesma tela (título, subtítulo, categoria, Markdown e preview). Só o **autor** edita; lápis discreto ao lado do avatar do autor no detalhe.
- **Cards** com categoria, estrelas e visualizações.

### Experiência de leitura

- Renderização Markdown no detalhe.
- **Atualização em tempo real:** com o detalhe aberto, listeners do Firestore atualizam metadados e corpo; a gravação do autor aparece para todos sem reabrir o artigo.
- Estrelas e contagem de views únicas (subcoleção).
- Gate offline: sem conexão, o app mostra a tela de offline até a rede voltar.

### Autores

- **Perfil público** (faixa, verificado, links sociais, lista de artigos, totais de views/estrelas).
- Perfil próprio com marcador `(me)`.
- **Sincronização de nome:** ao salvar, atualiza Firebase Auth e `authorName` nos artigos do autor em lotes paginados.

### Modo convidado

- Navegar na home, abrir artigos (com limites de preview quando aplicável) e perfis.
- Login exigido para ações autenticadas.

### Acessibilidade

- **Leitura em voz alta** (TextToSpeech) de título, subtítulo e conteúdo em português, sem sintaxe Markdown, com iniciar/parar.

---

## Tecnologias

| Categoria | Tecnologia |
| --- | --- |
| Linguagem | Kotlin |
| Interface | Jetpack Compose, Material 3 e Compose BOM |
| Arquitetura | MVVM com `ui`, `domain`, `data` e `core` |
| Injeção de dependência | Hilt e KSP |
| Autenticação | Firebase Authentication, Credential Manager, Google e GitHub |
| Banco de dados | Cloud Firestore |
| Imagens | Coil 3 |
| Markdown | multiplatform-markdown-renderer |
| Voz | Android TextToSpeech |
| Testes | JUnit 4 |

> Versões exatas em `app/build.gradle.kts` e `gradle/libs.versions.toml`.

---

## Como executar

### Requisitos

- Android Studio compatível com o Android Gradle Plugin do projeto
- JDK 21 para o daemon do Gradle (`gradle/gradle-daemon-jvm.properties`)
- Dispositivo ou emulador com Android **API 24+**
- Projeto no [Firebase Console](https://console.firebase.google.com/)

> Compatibilidade Java/Kotlin 11. Compila contra API 37 com `targetSdk` 35.

### Passo a passo

1. **Clone** o repositório e abra no Android Studio.
2. **Adicione o `google-services.json`** em `app/` (ignorado pelo Git).
3. **Habilite** Google e GitHub no Firebase Authentication e configure o OAuth.
4. **Cadastre o SHA-1** de debug no projeto Firebase.
5. **Configure o Cloud Firestore** e publique as regras de [`docs/firestore.rules.txt`](docs/firestore.rules.txt) (o Gradle não publica regras).
6. **Crie os índices compostos** exigidos pelos filtros da comunidade (abaixo).
7. **Sincronize o Gradle** e execute.

### Índices do Firestore

Filtro por categoria na comunidade:

```text
Coleção: articles
Campos: category Ascending, createdAt Descending
```

Se aparecer `FAILED_PRECONDITION` com link no log, abra o link e crie o índice. Consultas por autor podem exigir `authorUid` + `createdAt` conforme a query.

### Comandos úteis

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

---

## Arquitetura

Módulo único (`:app`) com **MVVM** e Clean Architecture simplificada.

- `MainActivity` controla destinos com flags `rememberSaveable` e callbacks.
- Telas coletam `StateFlow` dos ViewModels.
- Listeners do Firestore viram `Flow` (`callbackFlow`) para listas, detalhe (metadados + conteúdo), estrelas e progresso de leitura.
- Escritas pontuais usam coroutines e Tasks do Firebase.
- A camada `data` não depende de modelos de UI.

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
| `core/` | Formatação de nomes, Markdown para voz, TTS, URLs sociais, conectividade |
| `domain/model/` | Modelos independentes de Firebase e UI |
| `domain/repository/` | Contratos de persistência e auth com resultados tipados |
| `domain/usecase/` | Casos de uso (artigos, auth, settings, leitura, estrelas) |
| `data/article/` | DTOs, mappers e repositório (incluindo realtime de artigo e conteúdo) |
| `data/auth/` | Firebase Auth, Credential Manager, GitHub OAuth |
| `data/settings/` | Configurações em `user_profiles` |
| `data/reading/`, `data/favorite/`, `data/star/` | Continue lendo, favoritos e estrelas |
| `ui/articles/` | Home, detalhe, editor, comunidade e cards |
| `ui/articles/community/` | Lista da comunidade, filtro, busca e paginação |
| `ui/home/` | Home e detalhe/perfil para guest |
| `ui/auth/` | Login, sessão e vinculação |
| `ui/profile/` | Perfil do autor |
| `ui/settings/` | Configurações da conta |
| `ui/splash/` | Splash e tela offline |
| `ui/components/`, `ui/theme/` | UI compartilhada e tema |
| `di/` | Módulos Hilt |

### Fluxo de atualização do nome do autor

1. Perfil salvo.
2. Artigos consultados por `authorUid` em páginas.
3. `authorName` atualizado em lotes (até 450 docs).
4. Nome no Firebase Authentication atualizado.
5. Sucesso só após lotes e Auth.

> [!NOTE]
> Falha no meio do lote pode deixar sync parcial. Salvar de novo continua o restante.

---

## Modelo de dados (Firestore)

### `articles/{articleId}`

| Campo | Descrição |
| --- | --- |
| `authorUid` | UID do dono |
| `authorName`, `authorPhotoUrl` | Exibição |
| `category`, `title`, `subtitle` | Metadados |
| `excerpt` | Resumo gerado na gravação |
| `starredBy`, `starCount` | Avaliações |
| `viewCount` | Views únicas (com subcoleção `views`) |
| `createdAt`, `updatedAt` | Timestamps |

O corpo completo não fica só no documento raiz neste desenho.

### `articles/{articleId}/content/body`

| Campo | Descrição |
| --- | --- |
| `text` | Markdown completo |

O detalhe observa o documento do artigo e este documento de conteúdo em tempo real.

### `user_profiles/{uid}`

| Campo | Descrição |
| --- | --- |
| `name` | Nome de exibição |
| `github`, `linkedin` | Links opcionais |
| `photoUrl` | Foto opcional |

E-mail não é gravado aqui; vem do Firebase Authentication.

### Outras coleções

| Caminho | Função |
| --- | --- |
| `articles/{id}/views/{viewerUid}` | Views únicas |
| `user_reading/{uid}/items/{articleId}` | Continue lendo |
| Docs de autores favoritos (por usuário) | Autores que o usuário curtiu |
| Projeções de estrelas recebidas | Entradas do Mural |

Caminhos exatos seguem os repositórios em `data/`.

---

## Segurança e limitações conhecidas

### Regras do Firestore

Referência em [`docs/firestore.rules.txt`](docs/firestore.rules.txt). Intenção:

- cada usuário lê/grava só o próprio `user_profiles/{uid}`;
- artigos com leitura pública; escrita de conteúdo restrita ao autor;
- campos de estrela conforme as regras para autenticados;
- progresso de leitura e paths privados no dono.

### Pendências antes de ir para produção

> [!WARNING]
> **Regra de estrelas:** só listar campos permitidos pode não garantir que `starredBy` mude apenas para o UID autenticado. Reforce antes de produção.

---

## Testes

Testes unitários cobrem, entre outros:

- conversão artigo → card;
- formatação de nomes;
- preparação de Markdown para voz.

```bash
./gradlew :app:testDebugUnitTest
```

UI e integração Firebase ainda têm cobertura limitada.

> **Escopo acadêmico:** prioriza fluxos principais e arquitetura clara.

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
│   │   │   ├── favorite/
│   │   │   ├── reading/
│   │   │   ├── settings/
│   │   │   └── star/
│   │   ├── di/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   ├── ui/
│   │   │   ├── articles/
│   │   │   │   ├── community/
│   │   │   │   ├── detail/
│   │   │   │   ├── editor/
│   │   │   │   └── components/
│   │   │   ├── auth/
│   │   │   ├── components/
│   │   │   ├── home/
│   │   │   ├── profile/
│   │   │   ├── settings/
│   │   │   ├── splash/
│   │   │   └── theme/
│   │   ├── MainActivity.kt
│   │   └── MindInkApplication.kt
│   └── res/
├── test/
└── androidTest/
```

---

## Equipe

**Orientador:** Prof. Daniel Henrique Matos de Paiva

| Nome | RA | Função |
| --- | --- | --- |
| Eliseu Pereira da Silva | 124220479 | Desenvolvedor |
| Maycon Soares De Resende | 125111404445 | Usuário de Teste |
| Fabricio Rocha Souza Neves | 125111399975 | Usuário de Teste |
| Gustavo Silva Lourenço | 125111410319 | Usuário de Teste |
| Gustavo Morais de Oliveira Siqueira | 1242022304 | Vídeo Maker |
| Bruno Resende Ribeiro | 124220140 | Criador do PPTX |
| Ana Júlia Brum | 124222016 | Criadora do Relatório |
