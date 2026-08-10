# UI Contract

Este contrato define o visual comum para todas as funções, mesmo as que ainda
serão implementadas.

## Navegação

- Menu lateral compacto é a navegação principal.
- Cada item abre uma área estável; não criar menus diferentes por tela.
- A área ativa sempre fica destacada.
- O conteúdo nunca deve iniciar processo automaticamente sem feedback visível.
- A tela deve sobreviver a voltar, rotação/recriação e reabertura do app.
- Ações destrutivas ficam em menu contextual ou diálogo de confirmação.

Áreas previstas:

```text
Home
Explorer
Search
Git
Linux
Terminal
Settings
```

## Estados De Ação

Toda ação assíncrona precisa apresentar:

```text
idle -> running -> success
                  \-> error -> retry
```

Enquanto `running`, o botão deve impedir duplicação. `error` precisa mostrar a
causa em linguagem útil e oferecer retry ou cancelamento.

## Tema

Todas as telas usam tokens centralizados, nunca cores soltas:

- `background`
- `surface`
- `surfaceVariant`
- `textPrimary`
- `textSecondary`
- `accent`
- `success`
- `warning`
- `error`
- `border`

O tema futuro deve controlar também:

- raio dos cards e botões;
- espessura/estilo de bordas;
- tipografia do editor e terminal;
- densidade do menu lateral;
- estados selected, pressed, disabled e loading.

## Componentes Obrigatórios

- `V2Scaffold`: menu lateral, conteúdo e estado global.
- `V2ActionButton`: estados normal, loading, erro e desabilitado.
- `V2Card`: surface, borda e destaque ativo.
- `V2EmptyState`: mensagem e próxima ação.
- `V2ErrorState`: causa, retry e detalhes técnicos opcionais.
- `V2TerminalSurface`: saída, entrada e sessão identificada.

Nenhuma tela nova deve criar uma variação local desses componentes sem registrar
o motivo neste documento.
