# MindInk

[English](en/README.md)

Aplicativo Android para publicação e leitura de artigos sobre tecnologia. O MindInk reúne conteúdos de desenvolvimento, inteligência artificial, dispositivos móveis, cloud e temas relacionados, com autenticação social, perfis de autores e leitura em voz alta.

> **Projeto acadêmico:** A3 de Usabilidade, Desenvolvimento Web, Mobile e Jogos.

## Sumário

- [MindInk](#mindink)
  - [Sumário](#sumário)
  - [Autores](#autores)
  - [Funcionalidades](#funcionalidades)
  - [Tecnologias](#tecnologias)
  - [Arquitetura](#arquitetura)
    - [Fluxo do nome do autor](#fluxo-do-nome-do-autor)
  - [Persistência no Firestore](#persistência-no-firestore)
    - [`articles/{articleId}`](#articlesarticleid)
    - [`user_profiles/{uid}`](#user_profilesuid)
    - [Segurança](#segurança)
  - [Estrutura do projeto](#estrutura-do-projeto)
  - [Configuração e execução](#configuração-e-execução)
    - [Requisitos](#requisitos)
    - [Passo a passo](#passo-a-passo)
    - [Comandos úteis](#comandos-úteis)
  - [Testes](#testes)
  - [Escopo acadêmico](#escopo-acadêmico)

## Autores

| Nome | RA |
| --- | --- |
| Eliseu Pereira da Silva | 124220479 |
| Maycon Soares De Resende | 125111404445 |
| Fabricio Rocha Souza Neves | 125111399975 |
| Gustavo Silva Lourenço | 125111410319 |
| Gustavo Morais de Oliveira Siqueira | 1242022304 |
| Bruno Resende Ribeiro | 124220140 |
| Ana Júlia Brum | 124222016 |

## Funcionalidades

- **Autenticação:** login com Google ou GitHub via Firebase Authentication. Quando os provedores entram em conflito, o app oferece o fluxo para vincular as contas.
- **Página inicial:** artigos recentes, autores e uma lista paginada para explorar mais artigos.
- **Criação e edição de artigos:** título, subtítulo, categoria, conteúdo em Markdown e pré-visualização.
- **Leitura:** artigos renderizados em Markdown.
- **Avaliação por estrelas:** a interface destaca a estrela para o usuário que curtiu; a contagem e a lista de usuários são atualizadas no Firestore.
- **Perfil público de autor:** artigos publicados, total de estrelas e links opcionais para GitHub e Instagram.
- **Configurações da conta:** edição de nome e links sociais. O e-mail, obtido do provedor de autenticação, é somente leitura.
- **Sincronização de nome:** ao salvar um novo nome, o app atualiza o perfil Firebase e sincroniza `authorName` nos artigos do autor em lotes paginados.
- **Leitura em voz alta:** título, subtítulo e conteúdo em português, com remoção de sintaxe Markdown e controle para iniciar ou parar.
- **Menu do avatar:** acesso separado ao perfil, às configurações e à opção de sair.
- **Exclusão de conta:** remoção da conta Firebase com confirmação e tratamento da exigência de autenticação recente.

## Tecnologias

| Categoria | Tecnologia |
| --- | --- |
| Linguagem | Kotlin |
| Interface | Jetpack Compose, Material 3 e Compose BOM |
| Arquitetura | MVVM com separação entre `ui`, `domain`, `data` e `core` |
| Injeção de dependência | Hilt e KSP |
| Autenticação | Firebase Authentication, Credential Manager e provedores Google/GitHub |
| Persistência | Cloud Firestore |
| Imagens | Coil 3 |
| Markdown | multiplatform-markdown-renderer |
| Acessibilidade por voz | Android TextToSpeech |
| Testes locais | JUnit 4 |

## Arquitetura

O app é um único módulo Android (`:app`). O `MainActivity` controla os destinos principais com estado Compose salvo e callbacks de navegação. As telas observam o estado de seus ViewModels por `StateFlow`, enquanto listeners do Firestore mantêm artigos, avaliações e informações de perfil atualizados em tempo real.

O fluxo de dependências nas operações segue esta direção:

```text
Compose Screen -> ViewModel -> UseCase -> Repository (interface)
                                                ^
                                                |
                                   Repository (implementação)
```

| Pacote | Responsabilidade |
| --- | --- |
| `core/` | Formatação de nomes, preparação de texto Markdown para voz e suporte a TextToSpeech. |
| `domain/model/` | Modelos independentes das APIs do Firebase e da interface. |
| `domain/repository/` | Contratos de persistência/autenticação e resultados tipados das operações. |
| `domain/usecase/` | Operações de aplicação para artigos, autenticação e configurações. |
| `data/article/` | DTOs, conversão Firestore-domínio e implementação do repositório de artigos. |
| `data/auth/` | Integração com Firebase Authentication, Google Credential Manager e GitHub OAuth. |
| `data/settings/` | DTO e persistência das configurações em `user_profiles`. |
| `ui/articles/` | Lista de artigos, detalhes, editor, componentes e modelos de apresentação. |
| `ui/articles/mapper/` | Conversão de modelos de domínio para modelos apresentados nos cards. |
| `ui/auth/` | Login, estado de sessão e vinculação de provedores. |
| `ui/profile/` | Perfil público do autor e links sociais. |
| `ui/settings/` | Tela, estado e ViewModel das configurações da própria conta. |
| `ui/splash/` | Tela de abertura. |
| `ui/components/` e `ui/theme/` | Componentes compartilhados e tokens visuais. |
| `di/` | Bindings e providers usados pelo Hilt. |

Os listeners do Firestore são expostos como `Flow` com `callbackFlow`. Operações pontuais usam coroutines e aguardam as tarefas assíncronas do Firebase. A camada `data` não depende dos modelos de apresentação.

### Fluxo do nome do autor

1. O perfil do usuário é salvo primeiro.
2. O repositório consulta todos os artigos por `authorUid`, em páginas.
3. `authorName` é atualizado em lotes de até 450 documentos.
4. O nome no Firebase Authentication é atualizado.
5. O app só mostra sucesso depois que todos os lotes e a atualização do nome terminarem.

Se uma falha ocorrer após alguns lotes, a sincronização pode ficar parcial. Salvar novamente repete a atualização para os artigos restantes.

## Persistência no Firestore

### `articles/{articleId}`

| Campo | Descrição |
| --- | --- |
| `authorUid` | UID do proprietário; usado para carregar o perfil e sincronizar o nome. |
| `authorName`, `authorUsername`, `authorPhotoUrl` | Dados de exibição do autor. |
| `category`, `title`, `subtitle`, `content`, `linkUrl` | Conteúdo do artigo. |
| `starredBy` | UIDs dos usuários que avaliaram o artigo. |
| `starCount` | Total de estrelas, atualizado na mesma transação de `starredBy`. |
| `createdAt`, `updatedAt` | Datas de criação e atualização. |

A lista de exploração combina observação em tempo real com carregamento paginado.

### `user_profiles/{uid}`

As configurações opcionais são armazenadas por UID com os campos `name`, `github` e `instagram`. O e-mail não é salvo nesse documento nem editado pela tela; ele vem do Firebase Authentication. Campos antigos de idade e LinkedIn são removidos quando as configurações são salvas.

### Segurança

As regras do Firestore não estão versionadas neste repositório. Configure-as no Firebase Console para que:

- um usuário autenticado só possa ler e gravar o documento `user_profiles` cujo ID seja o próprio UID;
- as escritas em `articles` fiquem restritas ao autor autenticado, com as exceções necessárias para as estrelas;
- a política de leitura dos artigos (pública ou exigindo autenticação) seja definida de acordo com o projeto.

> **Exclusão de conta:** a operação atual exclui a identidade do Firebase Authentication. Ela não remove automaticamente o documento `user_profiles/{uid}` nem os artigos já publicados. Para apagar esses dados também, implemente a limpeza em um backend confiável, como uma Cloud Function acionada pela exclusão da conta. Não conceda ao cliente permissões amplas de exclusão.

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
├── test/                     # Testes unitários JVM
└── androidTest/              # Testes instrumentados Android
```

## Configuração e execução

### Requisitos

- Android Studio compatível com o Android Gradle Plugin configurado.
- JDK 21 para o daemon Gradle (definido em `gradle/gradle-daemon-jvm.properties`).
- Dispositivo ou emulador com Android API 24 ou superior.

O código Java/Kotlin é configurado com compatibilidade Java 11. O projeto compila contra a API 37 e define `targetSdk` 35.

### Passo a passo

1. Adicione o arquivo `google-services.json` ao módulo `app` (ele é ignorado pelo Git).
2. No Firebase Authentication, habilite os provedores Google e GitHub e configure as credenciais OAuth exigidas por cada um.
3. Cadastre no Firebase o SHA-1 do keystore de debug usado no ambiente.
4. Configure o Cloud Firestore e publique regras que autorizem somente as operações previstas para o usuário autenticado.
5. Sincronize o Gradle e execute o app em um dispositivo ou emulador.

### Comandos úteis

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Confira `app/build.gradle.kts` e `gradle/libs.versions.toml` para os SDKs, plugins e versões de bibliotecas efetivamente configurados.

## Testes

Os testes unitários atuais cobrem a conversão de artigos para cards, a formatação de nomes e a preparação de texto Markdown para leitura em voz alta. A pasta `app/src/test` é versionada.

```bash
./gradlew :app:testDebugUnitTest
```

O teste instrumentado de exemplo foi removido. Os fluxos de interface e a integração com o Firebase ainda precisam de testes instrumentados ou de integração próprios.

## Escopo acadêmico

Esta versão prioriza a entrega dos fluxos principais e uma arquitetura clara. A cobertura de testes automatizados se limita à lógica de apoio descrita acima e deve ser ampliada em etapas futuras.
