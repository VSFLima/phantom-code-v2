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
| WorkspaceService/eventos | em andamento | Editor publica eventos; Linux/IA ainda não conectados |
| Feed de atividade | em andamento | Mostra a última alteração no projeto |
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
| Contrato de estado do runtime | feito | Esqueleto publicado e validado |
| Instalação transacional | feito | Download, SHA-256 e troca atômica validados |
| Verificação SHA-256 | feito | Validada para o pacote Phantom |
| QEMU ARM64 | feito | Empacotado no APK e validado no aparelho |
| Iniciar/parar VM | feito | Fluxo validado |
| Terminal Linux interativo | feito | Entrada/saída validada no aparelho |
| Comandos no guest | feito | Comandos executados no guest (distro v6) |
| Autologin no console | feito | Getty `--autologin root` (v6) + senha fallback `phantom` |
| Teclado/IME no terminal | feito | `adjustResize` + `imePadding`; barra sempre visível |
| Rede no guest | feito | DHCP via systemd-networkd + QEMU user |
| Trocar distro | pendente | Uma distro ativa por vez inicialmente |
| Desinstalar distro | pendente | Confirmar e selecionar fallback |

## Build E Assinatura

| Função | Status | Observação |
| --- | --- | --- |
| Build distro via GitHub Actions | feito | `build-distro.yml`; release `distro-phantom` |
| Build APK via GitHub Actions | feito | `build.yml`; release `v2-release-<n>` |
| Assinatura privada do APK | feito | Keystore próprio; ver `docs/SIGNING.md` |
| Keystore versionado no backup | feito | `/root/phantom-keystore-backup/` |
| Secrets do GitHub | feito | `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` |

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

## Alterações 2026-08-12

- **Distro v6** (`build-distro.yml`, commit `09c92b5`): getty com
  `--autologin root` sem `-o` (corrige prompt de senha) + senha root `phantom`
  como fallback. Testado no aparelho: boot completo, rede DHCP e comandos no
  guest OK.
- **Terminal Linux** (commit `65a9061`): barra de digitação sempre visível,
  toque no console foca o campo e abre o teclado; `imePadding()` +
  `adjustResize` fazem a tela encolher em vez de fugir com o teclado. Testado
  no aparelho.
- **Assinatura privada** (commits `65a9061`..`5e62f5e`): keystore próprio
  (alias `phantom`), secrets `KEYSTORE_BASE64`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`,
  release `v2-release-47` assinado e verificado com apksigner. Limitação:
  keystore PKCS12 exige storepass == keypass.
- **Documentação**: `docs/SIGNING.md` com backup da chave em
  `/root/phantom-keystore-backup/`.
