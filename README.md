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
    - アップロードした画像をサーバーから削除(ImgurとCatboxのみ)
- Catboxにアップロードした画像の削除方法
    1. Catboxのサイトで会員登録。
    2. Catboxのユーザーページから自身の`userhash`を取得。
    3. 本アプリの設定画面にて`userhash`を入力。
    4. 以降、Catboxにアップロードした画像を削除可能になる。  
       ※削除可能なのはアップロード時と同一ユーザーの`userhash`を登録している場合のみ。
- マッシュルームアプリとしての動作
    - 呼び出し元へ、アップロードした画像URLの挿入
    - 呼び出し元へ、アップロード履歴から画像URLの挿入
- ユーザー独自のImgur Client ID の設定項目(API制限回避用)
    - Client ID全体での制限にひっかかりにくくなる
    - ユーザー個人への制限量は変わらない
- 他アプリのシェアボタンからの起動
- ~~Imgurへのログイン機能~~ 廃止
    - ~~ログイン後、アカウントに紐つけたアップロードが可能~~ 廃止
    - 該当画像は履歴欄で「Authed」と表示される

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











