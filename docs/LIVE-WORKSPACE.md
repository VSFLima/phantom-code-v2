# Live Workspace Contract

## Regra Principal

O editor, o Linux e as IAs nunca mantêm cópias independentes do projeto. O
workspace é a fonte única de verdade. Toda alteração passa pelo
`WorkspaceService` e produz um evento identificável.

```text
Editor ----\
Linux ------> WorkspaceService -> FileChangeEvent -> UI/Editor/Activity
IA --------/
```

## Evento De Alteração

Cada alteração deve carregar:

```text
eventId
projectId
path
operation: create | modify | rename | delete
source: editor | linux | ai | git | restore
actorId
taskId opcional
revision
timestamp
diff opcional
```

O editor usa `revision` para saber se o arquivo aberto ainda corresponde ao
disco. Não deve recarregar silenciosamente uma edição local do usuário.

## Arquivo Aberto

- Arquivo limpo + alteração externa: recarregar e mostrar aviso breve.
- Arquivo editado localmente + alteração externa: abrir resolução de conflito
  com versão local, versão do disco e diff.
- Alteração feita pela IA: mostrar `IA <nome> editou este arquivo` e o task ID.
- Alteração feita pelo Linux: mostrar `Linux alterou este arquivo` e permitir
  abrir o diff.
- Nunca sobrescrever texto local sem confirmação.

## IA No Linux

A IA dentro do Linux não acessa a UI diretamente. Ela conversa com um
`AgentGateway` autenticado:

```text
IA no guest -> phantom-agent -> AgentGateway -> WorkspaceService
                                      -> ActivityFeed
                                      -> EditorState
```

Antes de escrever, a IA precisa:

1. Identificar a tarefa e o agente.
2. Reservar os arquivos do escopo.
3. Enviar operações ou patch, não uma sobrescrita cega.
4. Receber a revisão atual do arquivo.
5. Publicar resultado, diff e erro.

## Visibilidade Em Tempo Real

A interface terá:

- Indicador global `Linux ativo`.
- Indicador `IA trabalhando` com nome e tarefa.
- Badge no arquivo alterado externamente.
- Painel de atividade com agente, arquivo, operação e horário.
- Diff antes de aceitar alterações conflitantes.
- Locks visíveis no arquivo e no projeto.
- Botão para pausar ou cancelar a tarefa da IA.

O transporte pode ser `SharedFlow` dentro do app. Alterações vindas do guest
chegam pelo canal de controle autenticado; a UI nunca lê diretamente o socket.

O primeiro núcleo foi iniciado em `workspace/WorkspaceService.kt`. Nesta fase
ele publica eventos do editor; os adaptadores Linux, Git e IA serão conectados
ao mesmo serviço, sem criar barramentos paralelos.

## Benefício

Assim o usuário sempre sabe:

- quem alterou o arquivo;
- quando alterou;
- qual foi o diff;
- se a alteração foi aceita, bloqueada ou entrou em conflito;
- qual tarefa da IA pode ser pausada.
