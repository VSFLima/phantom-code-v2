# Phantom-Code V2

Rebuild separado do Phantom-Code: editor mobile inspirado no fluxo do Spck e
runtime Linux próprio, sem copiar o APK proprietário do Spck.

## Primeiro milestone

- App Android separado: `com.phantomcode.v2`
- Estado único inicial do Linux
- Fluxo visual mínimo: preparar distro, iniciar e parar
- Editor como próximo módulo, antes de Git/IA/backup

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
5. Git e recursos opcionais.
