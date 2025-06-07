# ICL Mushroom について

## 概要

Android対象アプリ。

## 機能

- 対応アップローダーへの画像アップロード
    - Imgur
    - Catbox
    - Litterbox
- 履歴管理
    - アップロードした画像URLのコピー
    - アップロードした画像をサーバーから削除(Imgurのみ)
- マッシュルームアプリとしての動作
    - 呼び出し元へ、アップロードした画像URLの挿入
    - 呼び出し元へ、アップロード履歴から画像URLの挿入
- ユーザー独自のImgur Client ID の設定項目(API制限回避用)
    - Client ID全体での制限にひっかかりにくくなる
    - ユーザー個人への制限量は変わらない
- 他アプリのシェアボタンからの起動
- Imgurへのログイン機能
    - ログイン後、アカウントに紐つけたアップロードが可能
    - 該当画像は履歴欄で「Authed」と表示される(削除時には同アカウントでのログインが必要)

## ビルド時の注意

### Imgur Client ID の設定

`local.properties`にImgurにて登録した`IMGUR_CLIENT_ID`を入力してください。

```local.properties
IMGUR_CLIENT_ID={{YOUR_CLIENT_ID}}
```

### Imgur OAuth の設定

Imgur API の設定画面にてコールバックを設定してください。  
パッケージ名を変更する場合は適宜修正してください。

```
io.github.konkonfox.iclmushroom://callback
```











