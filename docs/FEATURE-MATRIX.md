# V2 Feature Matrix

Status permitidos:

- `feito`: implementado, testado no fluxo principal e validado no GitHub.
- `em andamento`: código iniciado, mas ainda falta completar ou testar.
- `pendente`: ainda não iniciado.
- `bloqueado`: depende de uma decisão técnica ou recurso externo.

Uma função só pode virar `feito` quando tiver ação real, estado de carregamento,
erro visível, recuperação/repetição e teste de reabertura do app.

## Editor E Workspace

| Função | Status | Observação |
| --- | --- | --- |
| Criar projeto | em andamento | Implementado; aguardando build/teste GitHub |
| Listar projetos | em andamento | Implementado; aguardando build/teste GitHub |
| Explorar arquivos | em andamento | Implementado; primeiro nível |
| Criar arquivo | em andamento | Implementado; aguardando build/teste GitHub |
| Abrir arquivo | em andamento | Implementado; editor básico |
| Editar texto | em andamento | Implementado; aguardando teste no APK |
| Salvar arquivo | em andamento | Implementado; aguardando teste no APK |
| Pastas aninhadas | pendente | Próxima melhoria do explorer |
| Abas de editor | pendente | Requer modelo de sessão |
| Restaurar sessão | pendente | Persistir projeto, arquivos e cursor |
| Syntax highlighting | pendente | Definir engine e linguagens |
| Autocomplete/LSP | pendente | Depois do editor estável |
| Snippets | pendente | Compatibilidade com fluxo Spck |
| Busca no arquivo | pendente | Inclui substituir |
| Busca no projeto | pendente | Respeitar limites do workspace |
| Undo/redo persistente | pendente | Não perder alterações ao trocar arquivo |
| Preview web | pendente | Servidor e navegador isolados |

## Git E Remoto

| Função | Status | Observação |
| --- | --- | --- |
| Git status/diff | pendente | Adaptador isolado |
| Init/clone | pendente | Fluxo com erros recuperáveis |
| Commit | pendente | Confirmação e mensagem obrigatória |
| Pull/push | pendente | Resolver conflitos sem apagar trabalho |
| Branches | pendente | Seletor explícito |
| Workspace remoto | pendente | Basear protocolo no Spck CLI MIT |
| Transferência de arquivos | pendente | Upload/download com progresso |
| Terminal remoto | pendente | Sessões e autenticação separadas |

## Linux

| Função | Status | Observação |
| --- | --- | --- |
| Contrato de estado do runtime | em andamento | Primeiro esqueleto publicado |
| Instalação transacional | pendente | Sem diretório parcialmente válido |
| Verificação SHA-256 | pendente | Antes de publicar distro |
| QEMU ARM64 | pendente | Validar no dispositivo real |
| Iniciar/parar VM | pendente | Máquina de estados única |
| Terminal Linux interativo | pendente | Entrada e saída verificáveis |
| Comandos no guest | pendente | Sem scanner concorrendo com console |
| Trocar distro | pendente | Uma distro ativa por vez inicialmente |
| Desinstalar distro | pendente | Confirmar e selecionar fallback |

## Configurações E IA

| Função | Status | Observação |
| --- | --- | --- |
| Tema base | pendente | Centralizar tokens antes das telas |
| Menu lateral compacto | pendente | Contrato visual definido em `UI-CONTRACT.md` |
| Preferências de editor | pendente | Fonte, tabulação, wrap e tema |
| Secrets | pendente | Keystore e escopo explícito |
| ACP/IA | pendente | Somente depois do núcleo Linux/editor |

## Registro De Alterações

Cada mudança deve atualizar esta matriz e registrar:

- função afetada;
- status anterior e novo;
- commit;
- workflow do GitHub;
- teste manual ou automatizado executado;
- limitações conhecidas.
