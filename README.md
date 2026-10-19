# MindInk

Aplicativo Android para publicação e leitura de artigos sobre tecnologia. O MindInk reúne conteúdos de desenvolvimento, inteligência artificial, dispositivos móveis, cloud e temas relacionados em uma experiência com autenticação, perfis de autores e leitura acessível por voz.

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

- Autenticação com Google e GitHub
- Página inicial com artigos recentes e autores
- Criação e edição de artigos com título, subtítulo, categoria e conteúdo em Markdown
- Leitura de artigos com renderização Markdown
- Avaliação de artigos por estrelas, com atualização otimista
- Perfil de autor com seus artigos publicados
- Leitura em voz alta do título, subtítulo e conteúdo em português
- Menu de usuário com acesso ao perfil e opção de sair

## Tecnologias

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose com Material 3
- **Arquitetura:** MVVM e Clean Architecture
- **Injeção de dependência:** Hilt
- **Autenticação:** Firebase Authentication (Google e GitHub)
- **Banco de dados:** Cloud Firestore
- **Renderização Markdown:** multiplatform-markdown-renderer
- **Leitura em voz alta:** Android TextToSpeech

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
