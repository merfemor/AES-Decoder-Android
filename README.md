# OpenSSL AES Decoder

Simple Android app for decoding text encoded with OpenSSL AES (aes-256-cbc and aes-128-cbc modes supported).

Example of encoding
```
$ openssl version
LibreSSL 2.6.5
$ openssl enc -salt -aes-256-cbc -a -in <file with original text> -pass pass:<password text>
```