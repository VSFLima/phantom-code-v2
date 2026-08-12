# Assinatura de Release (APK)

## Resumo

O APK de release é assinado com uma **chave privada própria** (não a chave de
debug do Android). A assinatura garante que o apk instalado é autêntico e que
atualizações futuras podem ser aplicadas por cima (mesma assinatura).

- Alias da chave: `phantom`
- Algoritmo: RSA 2048 / SHA256withRSA
- Validade: 30 anos (até 2056)
- Keystore: `phantom-release.jks` (PKCS12, gerado com JDK 17)

## Onde está a chave (BACKUP OBRIGATÓRIO)

| Cópia | Caminho |
|-------|---------|
| Keystore (cópia de trabalho) | `/tmp/opencode/phantom-release.jks` |
| Keystore (backup permanente) | `/root/phantom-keystore-backup/phantom-release.jks` |
| Senha do keystore (store == key) | `/root/phantom-keystore-backup/KEYSTORE_PASSWORD.txt` |

> **IMPORTANTE:** `/tmp` é apagado ao reiniciar a máquina. O backup permanente
> em `/root/phantom-keystore-backup/` precisa existir e ser guardado com
> segurança. Se o keystore for perdido, **não é possível assinar atualizações
> futuras** — o app instalado não aceitaria uma nova assinatura.

## Como o build assina (GitHub Actions)

O workflow `.github/workflows/build.yml`:

1. Lê os secrets do repositório:
   - `KEYSTORE_BASE64` — keystore codificado em base64
   - `KEYSTORE_PASSWORD` — senha do keystore (usada para store E key,
     porque o formato PKCS12 ignora keypass separado)
   - `KEY_ALIAS` — `phantom`
2. `app/build.gradle.kts` decodifica o keystore para `java.io.tmpdir` e o usa no
   `signingConfigs.create("release")`.
3. `./gradlew :app:assembleRelease` gera `app-release.apk` assinado.
4. O release é publicado com a tag `v2-release-<run_number>`.

Se os secrets não existirem, o build roda sem assinatura (release não assinado).

## Recriar / substituir a chave

Apenas se o keystore for perdido ou por política de segurança (isso invalida
atualizações para quem já instalou):

```bash
# gerar novo keystore (defina STOREPASS)
keytool -genkeypair -v \
  -keystore phantom-release.jks \
  -alias phantom \
  -keyalg RSA -keysize 2048 -validity 10950 \
  -storepass "$STOREPASS" \
  -dname "CN=Phantom Code V2, OU=Mobile, O=Phantom, L=Brasil, C=BR"

# publicar como secret
gh secret set KEYSTORE_BASE64 --repo VSFLima/phantom-code-v2 --body "$(base64 -w0 phantom-release.jks)"
gh secret set KEYSTORE_PASSWORD --repo VSFLima/phantom-code-v2 --body "$STOREPASS"
gh secret set KEY_ALIAS --repo VSFLima/phantom-code-v2 --body "phantom"
```

> Atenção: PKCS12 (padrão do JDK 17) **não suporta storepass != keypass**.
> Use a mesma senha para ambos (o `build.gradle.kts` já faz isso).

## Verificar a assinatura do APK

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
# esperado: V2 Signer: certificate DN: CN=Phantom Code V2, ...
```

## Instalar no aparelho

- O APK release tem assinatura diferente do APK debug antigo → **desinstalar a
  versão anterior antes de instalar** (senão o Android recusa).
- URL do último release:
  `https://github.com/VSFLima/phantom-code-v2/releases/download/v2-release-<n>/app-release.apk`

## Secrets do GitHub usados

| Secret | Conteúdo |
|--------|----------|
| `KEYSTORE_BASE64` | keystore `phantom-release.jks` em base64 |
| `KEYSTORE_PASSWORD` | senha do keystore (store == key) |
| `KEY_ALIAS` | `phantom` |
