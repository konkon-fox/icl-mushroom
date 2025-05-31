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

## ビルド時の注意

`local.properties`にImgurにて登録した`IMGUR_CLIENT_ID`を入力してください。

```local.properties
IMGUR_CLIENT_ID={{YOUR_CLIENT_ID}}
```











