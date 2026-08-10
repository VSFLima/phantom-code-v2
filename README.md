# Phantom-Code V2

Rebuild separado do Phantom-Code: editor mobile inspirado no fluxo do Spck e
runtime Linux próprio, sem copiar o APK proprietário do Spck.

## Escopo completo

A V2 deve cobrir as funções documentadas do Spck Editor, além do Linux local:

- Projetos múltiplos e workspace persistente
- Explorador de arquivos e operações criar, renomear, mover, copiar e excluir
- Editor com abas, syntax highlighting, snippets, cursor, seleção e preview
- Busca, substituição e busca no projeto
- Git: init, clone, status, diff, commit, pull, push e branches
- Preview local de projetos web
- Terminal interativo local/Linux com sessões persistentes
- Transferência de arquivos e conexão remota inspirada no Spck CLI
- Configurações de tema, fonte, atalhos e permissões
- Integração opcional com agentes via ACP, sempre depois do núcleo estável
- Linux local via QEMU: instalar, iniciar, parar, reiniciar, trocar distro e
  executar comandos no guest

Nenhuma função será apenas um botão visual: cada ação precisa ter estado,
feedback de sucesso/erro e teste de reabertura do app.

## Primeiro milestone

- App Android separado: `com.phantomcode.v2`
- Estado único inicial do Linux
- Fluxo visual mínimo: preparar distro, iniciar e parar
- Editor/workspace real como próximo módulo, antes de Git/IA/backup

## Regras

- A UI não cria processos nem acessa sockets.
- O runtime Linux será testado antes de receber recursos extras.
- `spckio/spck-embed` e `spck-io/spck-cli` só podem ser usados dentro das
  respectivas licenças MIT.
- O Phantom-Code atual não é alterado por este projeto.

## Milestones

1. Contrato do runtime e tela de diagnóstico.
2. Instalação transacional de uma distro Phantom.
3. QEMU nativo ARM64 e terminal interativo.
4. Workspace e editor com arquivos reais.
5. Explorador, abas, busca, snippets e preview.
6. Git completo e transferência remota compatível com o fluxo Spck CLI.
7. Temas, configurações, snippets avançados e ACP.
