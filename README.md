
# ChatIA Mobile
ChatIA Mobile é uma aplicação Android desenvolvida como atividade da disciplina de Programação para Dispositivos Móveis. O projeto consiste num chatbot inteligente que utiliza a API da OpenAI (GPT-3.5) para responder a perguntas do utilizador e manter um histórico de conversas localmente.

## 👥 Integrantes da Equipa
Vagner Alves (Desenvolvedor)

## 📝 Descrição do Problema e Solução
No contexto atual, o acesso rápido à informação e a assistentes virtuais é essencial. O ChatIA resolve a necessidade de ter um assistente pessoal acessível num dispositivo móvel.

*A solução proposta é uma aplicação nativa Android que:

*Permite ao utilizador enviar mensagens de texto para uma Inteligência Artificial.

*Recebe e exibe respostas em tempo real, formatadas (suporte a Markdown).

*Armazena todo o histórico de mensagens num banco de dados local para consulta posterior, mesmo offline.


## 📝 Descrição do Problema e Solução
No contexto atual, o acesso rápido à informação e a assistentes virtuais é essencial. O ChatIA resolve a necessidade de ter um assistente pessoal acessível num dispositivo móvel.

A solução proposta é uma aplicação nativa Android que:

Permite ao utilizador enviar mensagens de texto para uma Inteligência Artificial.

Recebe e exibe respostas em tempo real, formatadas (suporte a Markdown).

Armazena todo o histórico de mensagens num banco de dados local para consulta posterior, mesmo offline.

## 🛠️ Tecnologias Utilizadas
  O projeto foi desenvolvido inteiramente em Kotlin, utilizando as mais recentes bibliotecas do ecossistema Android:
  
  Interface (UI): Jetpack Compose com Material Design 3.
  
  Arquitetura: MVVM (Model-View-ViewModel).
  
  Navegação: Navigation Compose para transição entre telas (Chat, Histórico, Sobre).
  
  Consumo de API: Retrofit com Gson e OkHttp.
  
  Integração IA: OpenAI API (modelo gpt-3.5-turbo).
  
  Persistência de Dados: Room Database (SQLite) para salvar o histórico de mensagens.
  
  Concorrência: Kotlin Coroutines e StateFlow para gestão de estados assíncronos.
  
  Formatação de Texto: Biblioteca compose-markdown para renderizar as respostas da IA.

## 🚀 Instruções de Instalação e Execução
Pré-requisitos
Android Studio Ladybug ou superior (recomendado).

JDK 11 ou superior configurado.

Uma chave de API da OpenAI (OPENAI_API_KEY).

Passos
Clone o repositório:

Bash

git clone https://github.com/seu-usuario/chatbot-mobile.git
Abra o projeto no Android Studio.

Configurar a Chave da API:

Crie ou abra o arquivo local.properties na raiz do projeto (este ficheiro é ignorado pelo Git).

Adicione a seguinte linha com a sua chave da OpenAI:

Properties

OPENAI_API_KEY=sk-sua-chave-aqui-123456...
Sincronize o projeto (Sync Project with Gradle Files).

Execute a aplicação num emulador ou dispositivo físico com Android 8.0 (API 26) ou superior.
