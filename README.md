# MindInk

Aplicativo Android para publicação e leitura de artigos sobre tecnologia. O MindInk reúne conteúdos de desenvolvimento, inteligência artificial, dispositivos móveis, cloud e temas relacionados, com autenticação social, perfis de autores e leitura em voz alta.

> **Projeto acadêmico:** A3 de Usabilidade, Desenvolvimento Web, Mobile e Jogos.

## Autores

- **Nome:** Eliseu Pereira da Silva | **RA:** 124220479
- **Nome:** Maycon Soares De Resende | **RA:** 125111404445
- **Nome:** Fabricio Rocha Souza Neves | **RA:** 125111399975
- **Nome:** Gustavo Silva Lourenço | **RA:** 125111410319
- **Nome:** Gustavo Morais de Oliveira Siqueira | **RA:** 1242022304
- **Nome:** Bruno Resende Ribeiro | **RA:** 124220140
- **Nome:** Ana Júlia Brum | **RA:** 124222016


## Funcionalidades

- Login com Google ou GitHub via Firebase Authentication. Quando os provedores entram em conflito, o app oferece o fluxo para vincular as contas.
- Página inicial com artigos recentes, autores e uma lista paginada para explorar mais artigos.
- Criação e edição de artigos com título, subtítulo, categoria, conteúdo Markdown e pré-visualização.
- Leitura de artigos renderizados em Markdown.
- Avaliação por estrelas. A interface destaca a estrela para o usuário que curtiu; a contagem e a lista de usuários são atualizadas no Firestore.
- Perfil público de autor com artigos publicados, total de estrelas e links opcionais para GitHub e Instagram.
- Configurações da própria conta para editar nome e links sociais. O e-mail, obtido do provedor de autenticação, é somente leitura.
- Ao salvar um novo nome, o app atualiza o perfil Firebase e sincroniza `authorName` nos artigos do autor em lotes paginados.
- Leitura em voz alta do título, subtítulo e conteúdo em português, com remoção de sintaxe Markdown e controle para iniciar ou parar.
- Menu do avatar com acesso separado ao perfil, às configurações e à opção de sair.
- Exclusão da conta Firebase com confirmação e tratamento da exigência de autenticação recente.

## Tecnologias

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose, Material 3 e Compose BOM
- **Arquitetura:** MVVM com separação entre `ui`, `domain`, `data` e `core`
- **Injeção de dependência:** Hilt e KSP
- **Autenticação:** Firebase Authentication, Credential Manager e provedores Google/GitHub
- **Persistência:** Cloud Firestore
- **Imagens:** Coil 3
- **Markdown:** multiplatform-markdown-renderer
- **Acessibilidade por voz:** Android TextToSpeech
- **Testes locais:** JUnit 4

## Arquitetura

O app é um único módulo Android (`:app`). O `MainActivity` controla os destinos principais com estado Compose salvo e callbacks de navegação. As telas observam o estado de seus ViewModels por `StateFlow`.

O fluxo de dependências usado nas operações segue esta direção:

```text
Compose Screen -> ViewModel -> UseCase -> Repository interface
											 ^
											 |
								   Repository implementation
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
| `ui/components/` e `ui/theme/` | Componentes compartilhados e tokens visuais. |
| `di/` | Bindings e providers usados pelo Hilt. |

Os listeners do Firestore são expostos como `Flow` com `callbackFlow`. Operações pontuais usam coroutines e aguardam as tarefas assíncronas do Firebase. A camada `data` não depende dos modelos de apresentação.

### Fluxo do nome do autor

Ao salvar as configurações, o perfil do usuário é salvo primeiro. Depois o repositório consulta todos os artigos por `authorUid`, em páginas, e atualiza `authorName` em lotes de até 450 documentos. O app só mostra sucesso depois que todos os lotes e a atualização do nome do Firebase terminarem. Se uma falha ocorrer após alguns lotes, a sincronização pode ficar parcial; salvar novamente repete a atualização para os artigos restantes.

## Persistência no Firestore

### `articles/{articleId}`

Os artigos usam os campos `authorUid`, `authorName`, `authorUsername`, `authorPhotoUrl`, `category`, `title`, `subtitle`, `content`, `linkUrl`, `starredBy`, `starCount`, `createdAt` e `updatedAt`.

- `authorUid` identifica o proprietário e é usado para carregar o perfil e sincronizar o nome.
- `starredBy` contém os UIDs que avaliaram o artigo; `starCount` é atualizado na mesma transação.
- A lista de exploração combina observação em tempo real com carregamento paginado.

### `user_profiles/{uid}`

As configurações opcionais são armazenadas por UID com os campos `name`, `github` e `instagram`. O e-mail não é salvo nesse documento nem editado pela tela; ele vem do Firebase Authentication. Campos antigos de idade e LinkedIn são removidos quando as configurações são salvas.

As regras do Firestore não estão versionadas neste repositório. Configure-as no Firebase Console para que um usuário autenticado só possa ler e gravar o documento `user_profiles` cujo ID seja o próprio UID, além das regras necessárias para artigos e estrelas.

> **Exclusão de conta:** a operação atual exclui a identidade do Firebase Authentication. Ela não remove automaticamente o documento `user_profiles/{uid}` nem os artigos já publicados. Para apagar esses dados também, implemente a limpeza em um backend confiável, como uma Cloud Function acionada pela exclusão da conta; não conceda ao cliente permissões amplas de exclusão.

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
│   │   │   └── theme/
│   │   ├── MainActivity.kt
│   │   └── MindInkApplication.kt
│   ├── test/                 # Testes unitários JVM
│   └── androidTest/          # Testes instrumentados Android
└── res/
```

## Testes

Os testes unitários atuais cobrem a conversão de artigos para cards, a formatação de nomes e a preparação de texto Markdown para leitura em voz alta. A pasta `app/src/test` é versionada.

```bash
./gradlew :app:testDebugUnitTest
```

O teste instrumentado de exemplo foi removido; os fluxos de interface e integração Firebase ainda precisam de testes instrumentados/integrados próprios.

## Configuração e execução

Requisitos principais: Android Studio compatível com o Android Gradle Plugin configurado, JDK 21 para o daemon Gradle (definido em `gradle/gradle-daemon-jvm.properties`) e um dispositivo/emulador com Android API 24 ou superior. O código Java/Kotlin é configurado com compatibilidade Java 11; o projeto compila contra API 37 e define `targetSdk` 35.

1. Adicione `google-services.json` ao módulo `app` (o arquivo é ignorado pelo Git).
2. No Firebase Authentication, habilite Google e GitHub e configure as credenciais OAuth exigidas por cada provedor.
3. Configure o Cloud Firestore e publique regras que autorizem somente as operações previstas para o usuário autenticado.
4. Sincronize o Gradle e execute o app em um dispositivo ou emulador.

Comandos úteis:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Confira `app/build.gradle.kts` e `gradle/libs.versions.toml` para os SDKs, plugins e versões de bibliotecas efetivamente configurados.

## Arquitetura MVVM

O projeto organiza responsabilidades em camadas:

| Camada | Responsabilidade |
| --- | --- |
| `domain` | Modelos, contratos de repositório e casos de uso |
| `data` | Integrações com Firestore e autenticação, DTOs e mappers |
| `ui` | Telas, ViewModels e componentes Compose |
| `core` | Utilitários, formatação de nomes e suporte à leitura em voz |
| `di` | Módulos de injeção de dependência do Hilt |

A interface utiliza `StateFlow` para observar o estado, enquanto listeners do Firestore mantêm artigos, avaliações e informações de perfil atualizados em tempo real.

### Estrutura principal

```text
app/src/main/java/com/mindInk/app/
├── core/
├── data/
├── di/
├── domain/
├── ui/
│   ├── articles/
│   ├── auth/
│   ├── components/
│   ├── profile/
│   ├── splash/
│   └── theme/
├── MainActivity.kt
└── MindInkApplication.kt
```

## Dados no Firestore

A coleção principal é `articles`. Entre os campos utilizados estão `authorUid`, `authorName`, `authorUsername`, `authorPhotoUrl`, `category`, `title`, `subtitle`, `content`, `starredBy`, `starCount`, `createdAt` e `updatedAt`.

> **Segurança:** configure as regras do Firestore de acordo com a política do projeto. As operações de escrita devem ser restritas ao autor autenticado; defina também se a leitura será pública ou exigirá autenticação.

## Como executar

1. Configure o arquivo `google-services.json` no módulo `app`.
2. No Firebase Authentication, habilite os provedores Google e GitHub.
3. Cadastre no Firebase o SHA-1 do keystore de debug usado no ambiente.
4. Sincronize o projeto com o Gradle e execute o aplicativo em um dispositivo ou emulador compatível.

Consulte `app/build.gradle.kts` para verificar o `minSdk` e as demais configurações do aplicativo.

## Escopo acadêmico

Esta versão prioriza a entrega dos fluxos principais e uma arquitetura clara. A cobertura automatizada de testes não foi o foco desta etapa do projeto.
