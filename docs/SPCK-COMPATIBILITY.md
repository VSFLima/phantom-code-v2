# Compatibilidade de Funções

Esta lista transforma o escopo do Spck Editor em requisitos verificáveis da V2.

## Editor

- Projetos múltiplos
- Arquivos e pastas
- Abas e restauração da sessão
- Syntax highlighting
- Autocompletar e snippets
- Busca/substituição no arquivo e no projeto
- Seleção, copiar, colar, desfazer e refazer
- Preview web

## Git

- Inicializar e clonar
- Status e diff
- Commit
- Pull e push
- Branches
- Conflitos com confirmação explícita

## Terminal E Linux

- Sessão local
- Sessão Linux/QEMU
- Entrada e saída interativas
- Múltiplas sessões sem compartilhar streams
- Persistência e encerramento seguro
- Instalação, troca e remoção de distro
- Execução de comandos no guest

## Remoto

- Workspace remoto via protocolo seguro
- Transferência de arquivos e pastas
- Git remoto
- Terminal remoto
- Preview/proxy

## Segurança E Confiabilidade

- Licenças MIT preservadas para componentes utilizados
- Nenhum código do APK proprietário do Spck
- Operações de arquivo confinadas ao workspace
- Comandos destrutivos exigem confirmação
- Cada ação apresenta erro real e pode ser repetida
- Testes de reinício, rotação, interrupção e falta de rede
